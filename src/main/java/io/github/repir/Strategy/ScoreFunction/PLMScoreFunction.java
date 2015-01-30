package io.github.repir.Strategy.ScoreFunction;

import io.github.repir.Retriever.Document;
import io.github.repir.tools.lib.Log;
import io.github.repir.Repository.DocTF;
import io.github.repir.Repository.Repository;
import io.github.repir.Strategy.Operator.Operator;
import io.github.repir.Strategy.ScoreFunction.PLMScoreFunction.Scorable;

/**
 * ScoreFunction for the PLM model by Zhao & Yun. In this KLD variant proximity
 * centralities SumProxB() is scored for each term and stored in their
 * OperatorValues secondaryfrequency. In scoring this SumProxB is added to the
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
      mu = repository.configuredInt("kld.mu", 2500);
      documentpriorweight = scorables.size();
      doctf = DocTF.get(repository, "all");
      retrievalmodel.requestFeature(doctf);
   }

   @Override
   public double score(Document doc) {
      double sumSumProx = 0;
      for (Scorable scorable : scorables) {
         sumSumProx += scorable.feature.getSecondaryFrequency();
      }
      double alphaD = mu / (double) (mu + sumSumProx + doctf.getValue());
      score = Math.log(alphaD);
      for (Scorable scorable : scorables) {
         double theta = (scorable.feature.getFrequency() + scorable.feature.getSecondaryFrequency() + mu * scorable.ptc)
                 / (doctf.getValue() + sumSumProx + mu);
         double featurescore = Math.log(theta / (scorable.ptc * alphaD));
         if (featurescore >= 0) {
            score += (1.0 / scorables.size()) * featurescore;
         }
         if (report) {
            if (featurescore >= 0) {
               doc.addReport("\n[%s] freq=%f prox=%e ptc=%e score=%f", scorable.feature.toTermString(), scorable.feature.getFrequency(), scorable.feature.getSecondaryFrequency(), scorable.ptc, featurescore);
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
   public Scorable createScorable(Operator feature) {
      return new Scorable(feature);
   }

   public class Scorable extends ScoreFunction.Scorable {

      double ptc; // P(t|C)

      public Scorable(Operator feature) {
         super(feature);
         this.ptc = feature.getCF() / (double) repository.getCF();
      }
   }
}
