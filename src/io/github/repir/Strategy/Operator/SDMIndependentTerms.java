package io.github.repir.Strategy.Operator;

import io.github.repir.Strategy.Operator.Operator;
import io.github.repir.Strategy.Operator.OperatorExpander;
import io.github.repir.Strategy.GraphRoot;
import io.github.repir.Strategy.GraphRoot;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 * This feature expands itself into the Full Independence set (MRF), and is only used
 * to set the correct weight to the terms.
 * <p/>
 * @author jeroen
 */
public class SDMIndependentTerms extends OperatorExpander {

   public static Log log = new Log(SDMIndependentTerms.class);
   double weight_term = 1 - repository.configuredDouble("sdm.orderedphrases", SDMOrderedCombinations.default_weight) 
                        - repository.configuredDouble("sdm.unorderedphrases", SDMUnorderedCombinations.default_weight);

   public SDMIndependentTerms(GraphRoot im, ArrayList<Operator> list) {
      super(im, list);
      //log.info("SDMTerm.weight=%f", weight_term);
   }

   @Override
   public ArrayList<Operator> replaceWith() {
      ArrayList<Operator> list = new ArrayList<Operator>();
      for (Operator f : containednodes ) {
         f.setweight(weight_term);
         list.add(f);
      }
      return list;
   }
}
