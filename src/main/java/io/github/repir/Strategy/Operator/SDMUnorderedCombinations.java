package io.github.repir.Strategy.Operator;

import io.github.repir.Strategy.Operator.QTerm;
import io.github.repir.Strategy.Operator.ProximityOperator;
import io.github.repir.Strategy.Operator.Operator;
import io.github.repir.Strategy.Operator.ProximityOperatorUnordered;
import io.github.repir.Strategy.Operator.OperatorExpander;
import io.github.repir.Strategy.GraphRoot;
import io.github.repir.Strategy.GraphRoot;
import io.github.htools.lib.Log;
import java.util.ArrayList;

/**
 * This feature expands itself into all possible phrase combinations of minimal
 * two contained features of the type FeaturePhraseDist.
 * <p/>
 * @author jeroen
 */
public class SDMUnorderedCombinations extends OperatorExpander {

   public static Log log = new Log(SDMUnorderedCombinations.class);
   public static final double default_weight = 0.05;
   public double weight_unorderedphrases = repository.configuredDouble("sdm.unorderedphrases", default_weight);

   public SDMUnorderedCombinations(GraphRoot im, ArrayList<Operator> list) {
      super(im, list);
      //log.info("SDMUnorderedphrases.weight=%f", weight_unorderedphrases);
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
         ProximityOperator phrase = new ProximityOperatorUnordered(root, nodes);
         phrase.setspan( (long) 8 ); 
         phrase.setweight( weight_unorderedphrases );
         list.add(phrase);
      }
      return list;
   }
}
