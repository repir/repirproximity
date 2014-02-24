package io.github.repir.Strategy;

import io.github.repir.Strategy.GraphNode;
import io.github.repir.Strategy.Term;
import io.github.repir.Strategy.GraphRoot;
import java.util.ArrayList;
import java.util.HashMap;
import io.github.repir.tools.Lib.Log;

/**
 * This variant to FeaturePhrase
 * <p/>
 * @author jeroen
 */
public class ProximityStopFeature extends CPEFeature {

   public static Log log = new Log(ProximityStopFeature.class);

   public ProximityStopFeature(GraphRoot im, ArrayList<GraphNode> list) {
      super(im, list);
   }

   @Override
   public void announce(ANNOUNCEKEY type, GraphNode node) {
      if (type != ANNOUNCEKEY.STOPWORD) {
         super.announce(type, node);
      }
   }
   
   /**
    * inly use combinations that contain at least one non-stopword
    * @return 
    */
   protected HashMap<Long, phrase> constructPhrases() {
      HashMap<Long, phrase> phrases = new HashMap<Long, phrase>();
      long maxid = (1l << containedfeatures.size());
      ID: for (long id = 2; id < maxid; id++) {
         int phrase_number_of_terms = io.github.repir.tools.Lib.MathTools.numberOfSetBits(id);
         if (phrase_number_of_terms > 1) {
            boolean is_valid = false;
            for (int i = 0; !is_valid && i < containedfeatures.size(); i++) {
               long bit = 1l << i;
               if ((id & bit) > 0) {
                  if (containedfeatures.get(i) instanceof Term && !((Term) containedfeatures.get(i)).isstopword) {
                     is_valid = true;
                  }
               }
            }
            if (is_valid)
               phrases.put(id, new phrase(id));
         }
      }
      return phrases;
   }
}
