package io.github.repir.apps.Retrieve;

import io.github.repir.Repository.Tools.Parameter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import io.github.repir.tools.DataTypes.Configuration;
import io.github.repir.Repository.ModelParameters;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Query;
import io.github.repir.RetrieverM.RetrieverMInputFormat;
import io.github.repir.RetrieverMR.RetrieverMR;
import io.github.repir.TestSet.TestSet;
import io.github.repir.tools.Lib.ArrayTools;
import io.github.repir.tools.Lib.HDTools;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.MathTools;

/**
 *
 * @author Jeroen Vuurens
 */
public abstract class Solution<P extends Parameter> {

   public static Log log = new Log(Solution.class);
   Repository repository;
   TestSet testset;
   RetrieverMR retriever;
   int MAX;
   ModelParameters modelparameters;
   HashMap<point, point> metric;
   ArrayList<ModelParameters.Record> newrecords = new ArrayList<ModelParameters.Record>();
   protected final Tuner tuner;
   final ArrayList<P> parameters;
   int topicstart;
   int topicend;

   public Solution(final Tuner tuner) {
      this.tuner = tuner;
      parameters = tuner.parameters;
   }

   public Solution(Repository repository, final Tuner tuner) {
      this(tuner);
      this.repository = repository;
      MAX = repository.getConfigurationInt("tuner.max", 2);
      testset = new TestSet(repository);
      testset.purgeTopics();
      retriever = new RetrieverMR(repository);
      modelparameters = (ModelParameters) repository.getFeature("ModelParameters");
      modelparameters.setBufferSize(1000000);
      TreeSet<Integer> topics = new TreeSet<Integer>(testset.topics.keySet());
      topicstart = topics.first();
      topicend = topics.last() + 1;
      RetrieverMInputFormat.setSplitable(true);
   }

   public void reset() {
      metric = new HashMap<point, point>();
   }

   ModelParameters.Record createRecord() {
      ModelParameters.Record newRecord = modelparameters.newRecord(tuner.storedparameters);
      return newRecord;
   }

   public ModelParameters getModelParameters() {
      return modelparameters;
   }

   public ArrayList<point> getPoints() {
      ArrayList<point> points = new ArrayList<point>();
      for (point p : metric.values()) {
         if (p.getValue() < 0) {
            points.add(p);
            if (points.size() > MAX)
               break;
         }
      }
      return points;
   }
   
   public Collection<point> allPoints() {
      return metric.values();
   }
   
   public point findPoint( point p ) {
      return metric.get(p);
   }

   public ArrayList<Query> getQueries() {
      ArrayList<Query> queries = testset.getQueries(retriever);
      for (Query q : queries) {
         q.setConfiguration(null);
      }
      return queries;
   }

   public abstract point findMax();

   public void addComputedPoint(ModelParameters.Record record) {
      newrecords.add(record);
   }

   public void removeFailedRetriever(point p) {
      for (int i = retrievers.size() - 1; i >= 0; i--) {
         RetrievePoint r = retrievers.get(i);
            if (r.getPoint() == p) {
               retrievers.remove(r);
            }
         }
   }

   ArrayList<RetrievePoint> retrievers;
   
   public void computePoints() {
      retrievers = new ArrayList<RetrievePoint>();
      int turn = 0;
      while (retrievers.size() > 0 || (getPoints().size() > 0 && MAX > 0)) {
         turn = (turn + 1) % 20;
         for (int i = retrievers.size() - 1; i >= 0; i--) {
            if (retrievers.get(i).getPoint().getRecord().map >= 0) {
               retrievers.remove(i);
            }
         }
         ArrayList<point> points = getPoints();
         if (retrievers.size() < MAX && retrievers.size() < points.size()) {
            //log.info("aap");
            NEXTPOINT:
            for (point p : points) {
               //log.info("noot");
               for (int i = retrievers.size() - 1; i >= 0; i--) {
                  if (p == retrievers.get(i).getPoint()) {
                     continue NEXTPOINT;
                  }
               }
               log.info("start point %s", p);
               retrievers.add(computePoint(p));
               if (retrievers.size() >= MAX) {
                  break;
               }
            }
         }
         if (turn == 0 && newrecords.size() > 0) {
            writePoints();
         }
         try {
            Thread.sleep(500);
         } catch (InterruptedException ex) {
            Logger.getLogger(Solution.class.getName()).log(Level.SEVERE, null, ex);
         }
      }
      if (newrecords.size() > 0) {
         writePoints();
      }
   }

   public RetrievePoint computePoint(point p) {
      Configuration conf = HDTools.readConfig(this.repository.getConfigurationString("repir.conf"));
      if (conf.getInt("tuner.max", MAX) != MAX) {
         MAX = conf.getInt("tuner.max", MAX);
         log.info("new max tuners %s %d", this.repository.getConfigurationString("repir.conf"), MAX);
      }
      p.setParameters(conf);
      return new RetrievePoint(this, p, conf);
   }

   public void writePoints() {
      log.info("writepoints %s %d", repository.getConfigurationString("repir.conf"), newrecords.size());
      int size = newrecords.size();
      modelparameters.closeRead();
      modelparameters.openWrite();
      for (int i = size - 1; i >= 0; i--) {
         modelparameters.write(newrecords.get(i));
      }
      modelparameters.closeWrite();
      for (int i = size - 1; i >= 0; i--) {
         newrecords.remove(i);
      }
   }

   public abstract void generateFirstPoints();

   public boolean needsCompute() {
      ArrayList<point> list = getPoints();
      for (point p : list)
         log.info("needsCompute %s", p.toString());
      //if (getPoints().size() > 0) 
      //log.info("%d %s", list.size(), list);
      return list.size() > 0;
   }

   public final class point {

      BigDecimal[] v = new BigDecimal[parameters.size()];
      double point3 = -1;
      ModelParameters.Record record;

      public point() {
      }

      public point(point other) {
         ArrayTools.copy(other.v, v);
      }

      public HashMap<String, String> getSettings() {
         HashMap<String, String> r = new HashMap<String, String>();
         for (P p : parameters) {
            r.put(p.parameter, v[p.index].toString());
         }
         return r;
      }

      @Override
      public int hashCode() {
         int h = 31;
         for (BigDecimal i : v) {
            h = MathTools.combineHash(h, i.hashCode());
         }
         return MathTools.finishHash(h);
      }

      @Override
      public String toString() {
         return getParameters() + " value=" + getValue();
      }

      public void setParameters(Configuration configuration) {
         HDTools.addToConfiguration(configuration, getParameters());
      }

      public String getParameters() {
         ArrayList<String> list = new ArrayList<String>();
         for (P p : parameters) {
            String pstr = p.parameter + "=" + v[p.index].toString();
            list.add(pstr);
         }
         return ArrayTools.toString(",", list);
      }

      @Override
      public boolean equals(Object obj) {
         return Arrays.deepEquals(this.v, ((point) obj).v);
      }

      public ModelParameters.Record getRecord() {
         if (record == null) {
            record = createRecord();
            for (int i = 0; i < parameters.size(); i++) {
               record.parameters.put(parameters.get(i).parameter, v[i].toString());
            }
            record = getModelParameters().read(record);
         }
         return record;
      }

      public double getValue() {
         return getRecord().map;
      }

      public boolean onEdge() {
         for (int i = 0; i < tuner.parameters.size(); i++) {
            P currentparameter = parameters.get(i);
            if (currentparameter.onEdge(v[i])) {
               return true;
            }
         }
         return false;
      }

      public point below(int param) {
         point p = new point(this);
         p.v[param] = parameters.get(param).below(v[param]);
         return p;
      }

      public point above(int param) {
         point p = new point(this);
         p.v[param] = parameters.get(param).above(v[param]);
         return p;
      }
   }
}
