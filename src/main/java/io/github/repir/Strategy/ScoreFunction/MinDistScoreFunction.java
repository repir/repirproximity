package io.github.repir.Strategy.ScoreFunction;

import io.github.repir.Strategy.ScoreFunction.ScoreFunctionKLD;
import io.github.repir.Retriever.Document;
import io.github.repir.Strategy.ScoreFunction.ScoreFunctionKLD.Scorable;
import io.github.htools.lib.Log;
import io.github.repir.Repository.Repository;
import io.github.repir.Strategy.Operator.MinDistProximityModel;
import io.github.repir.Strategy.ScoreFunction.ScoreFunctionKLD;
import java.util.Iterator;

/**
 * Implementation of the {@link ScoreFunction} described by MinDist proximity measure
 * by Tao & Zhai (2007). Document are score using KLD over the independent
 * {@link Query} terms, to which a proximity score is added: log( alpha +
 * e^(-mindist) ), where alpha is a free parameter (set using "mindist.alpha"),
 * and mindist is the minimal distance in word positions between any two
 * different query terms in the document.
 * <p/>
 * @author jeroen
 */
public class MinDistScoreFunction extends ScoreFunctionKLD {

   public static Log log = new Log(MinDistScoreFunction.class);
   public double alpha;
   public MinDistProximityModel mindistmodel;

   public MinDistScoreFunction(Repository repository) {
      super(repository);
   }

   @Override
   public void prepareRetrieve() {
      super.prepareRetrieve();
      Iterator<Scorable> iter = scorables.iterator();
      while (iter.hasNext()) { // get MinDstProximityModel from the list of Scorables
         Scorable s = iter.next();
         if (s.feature instanceof MinDistProximityModel) {
            mindistmodel = (MinDistProximityModel) s.feature;
            iter.remove(); // not used by KLD
            break;
         }
      }
      alpha = repository.configuredDouble("mindist.alpha", 0.3);
   }

   @Override
   public double score(Document doc) {
      score = super.score(doc); // KLD score
      if (mindistmodel != null) {
         double featurescore = Math.log(alpha + Math.exp(-mindistmodel.getSecondaryFrequency()));
         if (report) {
            doc.addReport("[%s] exp %e mindist=%e score=%f\n", mindistmodel.toTermString(),
                    Math.exp(-mindistmodel.getSecondaryFrequency()),
                    mindistmodel.getSecondaryFrequency(), featurescore);
         }
         score += featurescore;
      }
      return score;
   }
}
