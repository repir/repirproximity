package io.github.repir.Strategy;

import io.github.repir.Strategy.Term;
import io.github.repir.Strategy.FeatureProximity;
import io.github.repir.Strategy.GraphNode;
import io.github.repir.Strategy.FeatureProximityUnordered;
import io.github.repir.Strategy.FeatureExpander;
import io.github.repir.Strategy.GraphRoot;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 * This feature expands itself into all possible phrase combinations of minimal
 * two contained features of the type FeaturePhraseDist.
 * <p/>
 * @author jeroen
 */
public class SDMUnorderedPhrases extends FeatureExpander {

   public static Log log = new Log(SDMUnorderedPhrases.class);
   public static final double default_weight = 0.05;
   public double weight_unorderedphrases = repository.getConfigurationDouble("sdm.unorderedphrases", default_weight);

   public SDMUnorderedPhrases(GraphRoot im, ArrayList<GraphNode> list) {
      super(im, list);
      //log.info("SDMUnorderedphrases.weight=%f", weight_unorderedphrases);
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
      for (int i = 0; i < containedfeatures.size() - 1; i++) {
         FeatureProximity phrase = new FeatureProximityUnordered(root);
         phrase.add( containedfeatures.get(i).clone(root) );
         phrase.add( containedfeatures.get(i+1).clone(root) );
         phrase.setspan( (long) 8 ); 
         phrase.setweight( weight_unorderedphrases );
         list.add(phrase);
      }
      return list;
   }
}
