package io.github.repir.Strategy;

import io.github.repir.Strategy.GraphNode;
import io.github.repir.Strategy.Term;
import io.github.repir.Strategy.GraphRoot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import io.github.repir.tools.Lib.Log;

/**
 * This variant to FeaturePhrase
 * <p/>
 * @author jeroen
 */
public class ProximityStopAdjFeature extends ProximityStopFeature {

   public static Log log = new Log(ProximityStopAdjFeature.class);

   public ProximityStopAdjFeature(GraphRoot im, ArrayList<GraphNode> list) {
      super(im, list);
   }

   /**
    * any combination containing stopwords must be linked to a non-stopword
    * @return 
    */
   protected HashMap<Long, phrase> constructPhrases() {
      HashMap<Long, phrase> phrases = new HashMap<Long, phrase>();

      long maxid = (1 << containedfeatures.size());
      ID: for (long id = 2; id < maxid; id++) {
         int phrase_number_of_terms = io.github.repir.tools.Lib.MathTools.numberOfSetBits(id);
         if (phrase_number_of_terms > 1) {
            OK: for (int i = 0; i < containedfeatures.size(); i++) {
               long bit = 1l << i;
               if ((id & bit) > 0) {
                  if (containedfeatures.get(i) instanceof Term && ((Term) containedfeatures.get(i)).isstopword) {
                     for ( int j = i + 1; j < containedfeatures.size() && (id & (1 << j)) > 0; j++ ) {
                        if (containedfeatures.get(j) instanceof Term && !((Term)containedfeatures.get(j)).isstopword)
                           continue OK;
                     }
                     for (int j = i - 1; j >= 0 && (id & (1 << (j))) > 0; j-- ) {
                        if (containedfeatures.get(j) instanceof Term && !((Term)containedfeatures.get(j)).isstopword)
                           continue OK;
                     }
                     log.info("removed %d %s", id, toTermString(id, containedfeatures));
                     continue ID;
                  }
               }
            }
            phrases.put(id, new phrase(id));
         }
      }
      return phrases;
   } 
}
