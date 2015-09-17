package io.github.repir.Strategy.Operator;

import io.github.htools.lib.Log;
import java.util.ArrayList;
import io.github.repir.Strategy.GraphRoot;

/**
 * The list op terms is expanded to all possible ProximityOperatorOrdered 
 * contained two or more contiguous contiguous query terms.
 * <p/>
 * @author jeroen
 */
public class FDMOrderedCombinations extends OperatorExpander {

   public static Log log = new Log(FDMOrderedCombinations.class);
   public static final double default_weight = 0.1;
   public double weight_orderedphrases = repository.configuredDouble("fdm.orderedphrases", default_weight);

   public FDMOrderedCombinations(GraphRoot im, ArrayList<Operator> list) {
      super(im, list);
   }

   @Override
   public ArrayList<Operator> replaceWith() {
      ArrayList<Operator> list = new ArrayList<Operator>();
      for (int i = containednodes.size() - 1; i >= 0; i--) {
         if (containednodes.get(i) instanceof QTerm) {
            QTerm t = (QTerm) containednodes.get(i);
            if (t.isStopword() || !t.exists()) {
               containednodes.remove(i);
            }
         }
      }
      for (int i = 0; i < containednodes.size() - 1; i++) {
         for (int j = i + 2; j <= containednodes.size(); j++) {
            ArrayList<Operator> nodes = new ArrayList<Operator>();
            for (int f = i; f < j; f++) {
               nodes.add(containednodes.get(f).clone(root));
            }
            ProximityOperator phrase = new ProximityOperatorOrdered(root, nodes);
            phrase.setspan((long) phrase.containednodes.size());
            phrase.setweight(weight_orderedphrases);
            list.add(phrase);
         }
      }
      return list;
   }
}
