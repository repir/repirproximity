package io.github.repir.Strategy.Tools;

import io.github.repir.Strategy.Tools.ScoreFunction;
import io.github.repir.Strategy.FeatureValues;
import io.github.repir.Strategy.GraphNode;
import io.github.repir.Retriever.Document;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Repository.DocTF;
import io.github.repir.Repository.Repository;
import io.github.repir.Strategy.FeatureValues;
import io.github.repir.Strategy.GraphNode;
import io.github.repir.Strategy.Tools.PLMScoreFunction.Scorable;
import io.github.repir.Strategy.Tools.ScoreFunction;

/**
 * ScoreFunction for the PLM model by Zhao & Yun. In this KLD variant proximity
 * centralities SumProxB() is scored for each term and stored in their
 * FeatureValues secondaryfrequency. In scoring this SumProxB is added to the
 * frequency and the sum of SumProxB for all terms is added to the document
 * prior.
 * <p/>
 * @author jeroen
 */
public class PLMScoreFunction extends ScoreFunction<Scorable> {

   public static Log log = new Log(PLMScoreFunction.class);
   public int mu = 2500;
   DocTF doctf;

   public PLMScoreFunction(Repository repository) {
      super(repository);
   }

   @Override
   public void prepareRetrieve() {
      mu = repository.getConfigurationInt("kld.mu", 2500);
      documentpriorweight = scorables.size();
      doctf = (DocTF) retrievalmodel.requestFeature("DocTF:all");
   }

   @Override
   public double score(Document doc) {
      double sumSumProx = 0;
      for (Scorable scorable : scorables) {
         sumSumProx += scorable.feature.getFeatureValues().secondaryfrequency;
      }
      double alphaD = scorables.size() * mu / (double) (mu + sumSumProx + doctf.getValue());
      score = Math.log(alphaD);
      for (Scorable scorable : scorables) {
         FeatureValues v = scorable.feature.getFeatureValues();
         double theta = (v.frequency + v.secondaryfrequency + mu * scorable.ptc)
                 / (doctf.getValue() + sumSumProx + mu);
         double featurescore = Math.log(theta / (scorable.ptc * alphaD));
         if (featurescore >= 0) {
            score += (1.0 / scorables.size()) * featurescore;
         }
         if (report) {
            if (featurescore >= 0) {
               doc.addReport("\n[%s] freq=%f prox=%e ptc=%e score=%f", scorable.feature.toTermString(), v.frequency, v.secondaryfrequency, scorable.ptc, featurescore);
            } else {
               if (Double.isNaN(score)) {
                  doc.addReport("\n[%s] NaN ptc=%f", scorable.feature.toTermString(), scorable.ptc);
               }
            }
         }
      }
      return score;
   }

   @Override
   public Scorable create(GraphNode feature) {
      return new Scorable(feature);
   }

   public class Scorable extends ScoreFunction.Scorable {

      double queryweight;
      double ptc; // P(t|C)

      public Scorable(GraphNode feature) {
         super(feature);
         this.queryweight = feature.getFeatureValues().queryweight;
         this.ptc = feature.getFeatureValues().corpusfrequency / (double) repository.getCorpusTF();
      }
   }
}
