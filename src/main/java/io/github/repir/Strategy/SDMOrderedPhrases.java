package io.github.repir.Strategy;

import io.github.repir.Strategy.Term;
import io.github.repir.Strategy.FeatureProximity;
import io.github.repir.Strategy.FeatureProximityOrdered;
import io.github.repir.Strategy.GraphNode;
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
public class SDMOrderedPhrases extends FeatureExpander {

   public static Log log = new Log(SDMOrderedPhrases.class);
   public static final double default_weight = 0.1;
   public double weight_orderedphrases = repository.getConfigurationDouble("sdm.orderedphrases", default_weight);

   public SDMOrderedPhrases(GraphRoot im, ArrayList<GraphNode> list) {
      super(im, list);
      //log.info("SDMOrderedphrases.weight=%f", weight_orderedphrases);
   }

   @Override
   public ArrayList<GraphNode> replaceWith() {
      ArrayList<GraphNode> list = new ArrayList<GraphNode>();
      for (int i = containedfeatures.size() - 1; i >= 0; i--) {
         if (containedfeatures.get(i) instanceof Term) {
            Term t = (Term)containedfeatures.get(i);
            if (t.isstopword || !t.exists()) {
               containedfeatures.remove(i);
            }
         }
      }
      for (int i = 0; i < containedfeatures.size() - 1; i++) {
         FeatureProximity phrase = new FeatureProximityOrdered(root);
         phrase.add( containedfeatures.get(i).clone(root) );
         phrase.add( containedfeatures.get(i+1).clone(root) );
         phrase.setspan( (long) 2 );
         phrase.setweight( weight_orderedphrases );
         list.add(phrase);
      }
      return list;
   }
}
