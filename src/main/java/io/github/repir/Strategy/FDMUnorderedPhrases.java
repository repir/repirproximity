package io.github.repir.Strategy;

import io.github.repir.Strategy.FeatureProximityUnordered;
import io.github.repir.Strategy.FeatureExpander;
import io.github.repir.Strategy.FeatureProximity;
import io.github.repir.Strategy.GraphNode;
import io.github.repir.Strategy.GraphRoot;
import io.github.repir.Strategy.Term;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 * This feature expands itself into all possible phrase combinations of minimal
 * two contained features of the type FeaturePhraseDist.
 * <p/>
 * @author jeroen
 */
public class FDMUnorderedPhrases extends FeatureExpander {

   public static Log log = new Log(FDMUnorderedPhrases.class);
   public static final double default_weight = 0.1;
   public double weight_unorderedphrases = repository.getConfigurationDouble("fdm.unorderedphrases", default_weight);

   public FDMUnorderedPhrases(GraphRoot im, ArrayList<GraphNode> list) {
      super(im, list);
   }

   @Override
   public ArrayList<GraphNode> replaceWith() {
      ArrayList<GraphNode> list = new ArrayList<GraphNode>();
      for (int i = containedfeatures.size() - 1; i >= 0; i--) {
         if (containedfeatures.get(i) instanceof Term) {
            Term t = (Term)containedfeatures.get(i);
            if (root.isStemmedStopWord(t.stemmedterm)) {
               containedfeatures.remove(i);
            } else if (!t.exists()) {
               containedfeatures.remove(i);
            }
         }
      }
      for (long i = 0; i < (1l << containedfeatures.size()); i++) {
         if (io.github.repir.tools.Lib.MathTools.numberOfSetBits(i) > 1) {
            FeatureProximity phrase = new FeatureProximityUnordered(root);
            for (int j = 0; j < containedfeatures.size(); j++) {
               if ((i & (1l << j)) != 0) {
                  phrase.add(containedfeatures.get(j).clone(root));
               }
            }
            phrase.setspan( (long) 4 * phrase.containedfeatures.size() );
            phrase.setweight( weight_unorderedphrases );
            list.add(phrase);
         }
      }
      return list;
   }
}
