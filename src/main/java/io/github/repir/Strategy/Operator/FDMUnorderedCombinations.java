package io.github.repir.Strategy.Operator;

import io.github.repir.Strategy.GraphRoot;
import io.github.repir.tools.lib.Log;
import java.util.ArrayList;

/**
 * The list of terms is expanded to all possible ProximityOperatorUnordered containing
 * two or more terms.
 * <p/>
 * @author jeroen
 */
public class FDMUnorderedCombinations extends OperatorExpander {

   public static Log log = new Log(FDMUnorderedCombinations.class);
   public static final double default_weight = 0.1;
   public double weight_unorderedphrases = repository.configuredDouble("fdm.unorderedphrases", default_weight);

   public FDMUnorderedCombinations(GraphRoot im, ArrayList<Operator> list) {
      super(im, list);
   }

   @Override
   public ArrayList<Operator> replaceWith() {
      ArrayList<Operator> list = new ArrayList<Operator>();
      for (int i = containednodes.size() - 1; i >= 0; i--) {
         if (containednodes.get(i) instanceof QTerm) {
            QTerm t = (QTerm)containednodes.get(i);
            if (t.isStopword() || !t.exists()) {
               containednodes.remove(i);
            }
         }
      }
      for (long i = 0; i < (1l << containednodes.size()); i++) {
         if (io.github.repir.tools.lib.MathTools.numberOfSetBits(i) > 1) {
            ArrayList<Operator> nodes = new ArrayList<Operator>();
            for (int j = 0; j < containednodes.size(); j++) {
               if ((i & (1l << j)) != 0) {
                  nodes.add(containednodes.get(j).clone(root));
               }
            }
            ProximityOperator phrase = new ProximityOperatorUnordered(root, nodes);
            phrase.setspan( (long) 4 * phrase.containednodes.size() );
            phrase.setweight( weight_unorderedphrases );
            list.add(phrase);
         }
      }
      return list;
   }
}
