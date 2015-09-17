package io.github.repir.Strategy.Operator;

import io.github.repir.Strategy.GraphRoot;
import io.github.repir.Strategy.Operator.QTerm;
import io.github.repir.Strategy.Operator.ProximityOperator;
import io.github.repir.Strategy.Operator.ProximityOperatorOrdered;
import io.github.repir.Strategy.Operator.Operator;
import io.github.repir.Strategy.Operator.OperatorExpander;
import io.github.htools.lib.Log;
import java.util.ArrayList;

/**
 * This feature expands itself into all possible phrase combinations of minimal
 * two contained features of the type FeaturePhraseDist.
 * <p/>
 * @author jeroen
 */
public class SDMOrderedCombinations extends OperatorExpander {

   public static Log log = new Log(SDMOrderedCombinations.class);
   public static final double default_weight = 0.1;
   public double weight_orderedphrases = repository.configuredDouble("sdm.orderedphrases", default_weight);

   public SDMOrderedCombinations(GraphRoot im, ArrayList<Operator> list) {
      super(im, list);
      //log.info("SDMOrderedphrases.weight=%f", weight_orderedphrases);
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
      for (int i = 0; i < containednodes.size() - 1; i++) {
         ArrayList<Operator> nodes = new ArrayList<Operator>();
         nodes.add( containednodes.get(i).clone(root) );
         nodes.add( containednodes.get(i+1).clone(root) );
         ProximityOperator phrase = new ProximityOperatorOrdered(root, nodes);
         phrase.setspan( (long) 2 );
         phrase.setweight( weight_orderedphrases );
         list.add(phrase);
      }
      return list;
   }
}
