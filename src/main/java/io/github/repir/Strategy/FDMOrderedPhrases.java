package io.github.repir.Strategy;

import io.github.repir.Strategy.FeatureProximityOrdered;
import io.github.repir.Strategy.GraphNode;
import io.github.repir.Strategy.Term;
import io.github.repir.Strategy.FeatureExpander;
import io.github.repir.Strategy.FeatureProximity;
import io.github.repir.Strategy.GraphRoot;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;
import io.github.repir.Repository.TermDocumentFeature;

/**
 * This feature expands itself into all possible phrase combinations of minimal
 * two contained features of the type FeaturePhraseDist.
 * <p/>
 * @author jeroen
 */
public class FDMOrderedPhrases extends FeatureExpander {

   public static Log log = new Log(FDMOrderedPhrases.class);
   public static final double default_weight = 0.1;
   public double weight_orderedphrases = repository.getConfigurationDouble("fdm.orderedphrases", default_weight);

   public FDMOrderedPhrases(GraphRoot im, ArrayList<GraphNode> list) {
      super(im, list);
   }

   @Override
   public ArrayList<GraphNode> replaceWith() {
      ArrayList<GraphNode> list = new ArrayList<GraphNode>();
      for (int i = containedfeatures.size() - 1; i >= 0; i--) {
         if (containedfeatures.get(i) instanceof Term) {
            Term t = (Term) containedfeatures.get(i);
            if (root.isStemmedStopWord(t.stemmedterm)) {
               containedfeatures.remove(i);
            } else if (!t.exists()) {
               containedfeatures.remove(i);
            }
         }
      }
      for (int i = 0; i < containedfeatures.size() - 1; i++) {
         for (int j = i + 2; j <= containedfeatures.size(); j++) {
            log.info("i %d j %d", i, j);
            FeatureProximity phrase = new FeatureProximityOrdered(root);
            for (int f = i; f < j; f++) {
               phrase.add(containedfeatures.get(f).clone(root));
            }
            phrase.setspan((long) phrase.containedfeatures.size());
            phrase.setweight(weight_orderedphrases);
            list.add(phrase);
         }
      }
      return list;
   }
}
