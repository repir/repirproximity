package io.github.repir.Strategy.Operator;

import io.github.repir.Strategy.GraphRoot;
import io.github.htools.lib.Log;
import java.util.ArrayList;

/**
 * This feature expands itself into the set of independent terms, whose score is
 * weighted by LambdaT = 1 - LambdaO - LambdaU.
 * <p/>
 * @author jeroen
 */
public class FDMIndependentTerms extends OperatorExpander {

   public static Log log = new Log(FDMIndependentTerms.class);
   double weight_term = 1 - repository.configuredDouble("fdm.orderedphrases", FDMOrderedCombinations.default_weight) 
                        - repository.configuredDouble("fdm.unorderedphrases", FDMUnorderedCombinations.default_weight);

   public FDMIndependentTerms(GraphRoot im, ArrayList<Operator> list) {
      super(im, list);
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
