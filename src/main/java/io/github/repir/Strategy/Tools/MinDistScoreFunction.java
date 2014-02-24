package io.github.repir.Strategy.Tools;

import io.github.repir.Strategy.Tools.ScoreFunctionKLD;
import io.github.repir.Retriever.Document;
import io.github.repir.Strategy.Tools.ScoreFunctionKLD.Scorable;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Repository.Repository;
import io.github.repir.Strategy.Tools.ScoreFunctionKLD;

/**
 * Implementation of the MinDist proximity measure by Tao & Zhai
 * <p/>
 * @author jeroen
 */
public class MinDistScoreFunction extends ScoreFunctionKLD {

   public static Log log = new Log(MinDistScoreFunction.class);
   public double alpha;

   public MinDistScoreFunction(Repository repository) {
      super(repository);
   }

   @Override
   public void prepareRetrieve() {
      super.prepareRetrieve();
      alpha = repository.getConfigurationDouble("mindist.alpha", 0.3);
   }

   @Override
   public double score(Document doc) {
      score = super.score(doc);
      for (Scorable s : scorables)
         if (s.feature.getFeatureValues().secondaryfrequency > 0) {
            double featurescore = Math.log( alpha + Math.exp( -s.feature.getFeatureValues().secondaryfrequency) );
            if (report) {
               doc.addReport("[%s] exp %e mindist=%e score=%f\n", s.feature.toTermString(), 
                       Math.exp( -s.feature.getFeatureValues().secondaryfrequency), 
                       s.feature.getFeatureValues().secondaryfrequency, featurescore);
            }
            score += featurescore;
         }
      return score;
   }
}
