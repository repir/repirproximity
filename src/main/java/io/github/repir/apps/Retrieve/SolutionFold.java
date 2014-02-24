package io.github.repir.apps.Retrieve;

import io.github.repir.Repository.Tools.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeSet;
import io.github.repir.tools.DataTypes.Configuration;
import io.github.repir.Repository.ModelParameters;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Query;
import io.github.repir.RetrieverM.RetrieverMInputFormat;
import io.github.repir.RetrieverMR.RetrieverMR;
import io.github.repir.RetrieverMR.RetrieverMRInputFormat;
import io.github.repir.TestSet.QueryMetricAP;
import io.github.repir.TestSet.ResultSet;
import io.github.repir.TestSet.TestSet;
import io.github.repir.tools.Lib.HDTools;
import io.github.repir.tools.Lib.Log;

/**
 *
 * @author Jeroen Vuurens
 */
public abstract class SolutionFold extends Solution {
   public static Log log = new Log(SolutionFold.class);
   int fold;

   public SolutionFold(Repository repository, int fold, final Tuner tuner) {
      super(tuner);
      this.fold = fold;
      
      this.repository = repository;
      testset = new TestSet(repository);
      testset.purgeTopics();
      if (fold == 0) {
         retriever = new RetrieverMR(repository);
         modelparameters = (ModelParameters) repository.getFeature("ModelParameters");
         modelparameters.setBufferSize(1000000);
         RetrieverMInputFormat.setSplitable(true);
      } else {
         retriever = tuner.sol[0].retriever;
         modelparameters = tuner.sol[0].modelparameters;
      }
      
      MAX = repository.getConfigurationInt("tuner.max", 2);
      TreeSet<Integer> topics = new TreeSet<Integer>(testset.topics.keySet());
      int foldsize = 5;
      topicstart = topics.first() + fold * foldsize;
      topicend = topics.first() + (fold + 1) * foldsize;
   }

   @Override
   public void computePoints() {
      if (fold != 0)
         log.fatal("only fold 0");
      super.computePoints();
   }

   @Override
   public RetrievePoint computePoint(point p) {
       Configuration conf = HDTools.readConfig(this.repository.getConfigurationString("repir.conf"));
      if (conf.getInt("tuner.max", MAX) != MAX) {
         MAX = conf.getInt("tuner.max", MAX);
         log.info("new max tuners %s %d", this.repository.getConfigurationString("repir.conf"), MAX);
      }
      p.setParameters(conf);
      return new RetrievePointFold(this, p, conf);
   }

   @Override
   public ArrayList<point> getPoints() {
      HashSet<point> points = new HashSet<point>(super.getPoints());
      if (points.size() == 0) {
         for (int i = 1; i < tuner.sol.length; i++) {
            for (point p : (Collection<Solution<Parameter>.point>)tuner.sol[i].metric.values()) {
               if (p.getValue() < 0) {
                  points.add(p);
                  if (points.size() > MAX)
                     return new ArrayList<point>(points);
               }
            }

         }
      }
      return new ArrayList<point>(points);
   }

   @Override
   public ModelParameters getModelParameters() {
       return tuner.sol[0].modelparameters;
   }

   
   public ModelParameters.Record storePoints(point newpoint, ResultSet resultset) {
      double avg = 0;
      int count = 0;
      for (int topic = topicstart; topic < topicend; topic++) {
         int qid = resultset.getResultNumber(topic);
         if (qid >= 0) {
            avg += resultset.result[1].queryresult[qid];
            count++;
         }
      }
      avg /= count;

      point p = (point)metric.get(newpoint);
      p.getRecord().map = avg;
      return p.getRecord();
   }

   @Override
   ModelParameters.Record createRecord() {
      ModelParameters.Record newRecord = modelparameters.newRecord(tuner.storedparameters);
      newRecord.parameters.put("fold", Integer.toString(fold) );
      return newRecord;
   }
}
