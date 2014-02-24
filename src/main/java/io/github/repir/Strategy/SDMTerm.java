package io.github.repir.Strategy;

import io.github.repir.Strategy.GraphNode;
import io.github.repir.Strategy.FeatureExpander;
import io.github.repir.Strategy.GraphRoot;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 * This feature expands itself into the Full Independence set (MRF), and is only used
 * to set the correct weight to the terms.
 * <p/>
 * @author jeroen
 */
public class SDMTerm extends FeatureExpander {

   public static Log log = new Log(SDMTerm.class);
   double weight_term = 1 - repository.getConfigurationDouble("sdm.orderedphrases", SDMOrderedPhrases.default_weight) 
                        - repository.getConfigurationDouble("sdm.unorderedphrases", SDMUnorderedPhrases.default_weight);

   public SDMTerm(GraphRoot im, ArrayList<GraphNode> list) {
      super(im, list);
      //log.info("SDMTerm.weight=%f", weight_term);
   }

   @Override
   public ArrayList<GraphNode> replaceWith() {
      ArrayList<GraphNode> list = new ArrayList<GraphNode>();
      for (GraphNode f : containedfeatures ) {
         f.setweight(weight_term);
         list.add(f);
      }
      return list;
   }
}
