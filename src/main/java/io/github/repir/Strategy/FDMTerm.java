package io.github.repir.Strategy;

import io.github.repir.Strategy.GraphNode;
import io.github.repir.Strategy.FeatureExpander;
import io.github.repir.Strategy.GraphRoot;
import io.github.repir.Retriever.Document;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 * This feature expands itself into the Full Independence set (MRF), and is only used
 * to set the correct weight to the terms.
 * <p/>
 * @author jeroen
 */
public class FDMTerm extends FeatureExpander {

   public static Log log = new Log(FDMTerm.class);
   double weight_term = 1 - repository.getConfigurationDouble("fdm.orderedphrases", FDMOrderedPhrases.default_weight) 
                        - repository.getConfigurationDouble("fdm.unorderedphrases", FDMUnorderedPhrases.default_weight);

   public FDMTerm(GraphRoot im, ArrayList<GraphNode> list) {
      super(im, list);
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
