package io.github.repir.Strategy;

import io.github.repir.Strategy.Term;
import io.github.repir.Strategy.GraphNode;
import io.github.repir.Strategy.GraphRoot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import io.github.repir.Repository.TermDocumentFeature;
import io.github.repir.Retriever.Document;
import io.github.repir.Strategy.Tools.MatchSetLength;
import io.github.repir.Strategy.Tools.MatchSetPositional;
import io.github.repir.Strategy.Tools.ProximitySet;
import io.github.repir.Strategy.Tools.ProximitySet.ProximityTerm;
import io.github.repir.Strategy.Tools.ProximityStopwordsSet;
import io.github.repir.tools.Lib.ArrayTools;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.MathTools;

/**
 * This variant to FeaturePhrase
 * <p/>
 * @author jeroen
 */
public class CPXFeature extends CPEFeature {

   public static Log log = new Log(CPXFeature.class);
   public ArrayList<GraphNode> originalterms = new ArrayList<GraphNode>();
   
   public CPXFeature(GraphRoot im, ArrayList<GraphNode> list) {
      super(im, list);
      originalterms.addAll(list);
   }

   /**
    * valid combinations can only contain stop words that are either linked both
    * on the left and right by the query beginning/end or a non stop word.
    *
    * @return
    */
   @Override
   protected HashMap<Long, phrase> constructPhrases() {
      HashMap<Long, phrase> phrases = new HashMap<Long, phrase>();
      ArrayList<GraphNode[]> groups = new ArrayList<GraphNode[]>();
      ArrayList<GraphNode> currentgroup = new ArrayList<GraphNode>();

      for (GraphNode g : originalterms) {
         if (g instanceof Term && ((Term) g).isstopword) {
            if (currentgroup.size() > 0) {
               groups.add(currentgroup.toArray(new GraphNode[currentgroup.size()]));
               currentgroup = new ArrayList<GraphNode>();
            }
         } else {
            currentgroup.add(g);
         }
      }
      if (currentgroup.size() > 0) {
         groups.add(currentgroup.toArray(new GraphNode[currentgroup.size()]));
      }

      for (GraphNode[] group : groups) {
         long maxid = (1l << group.length);
         ID:
         for (int pattern = 2; pattern < maxid; pattern++) {
            int phrase_number_of_terms = io.github.repir.tools.Lib.MathTools.numberOfSetBits(pattern);
            if (phrase_number_of_terms > 1) {
               long modifiedpattern = termpositions.convertDuplicatesInPattern(pattern);
               if (!phrases.containsKey(modifiedpattern)) {
                  phrases.put(modifiedpattern, new phrase(modifiedpattern));
               }
            }
         }
      }

      int groupshift = 0;
      GraphNode pgroup[] = null;
      for (GraphNode group[] : groups) {
         if (pgroup != null) {
            for (long pattern = (1l << group.length) - 1; pattern > 0; pattern--) {
               for (long ppattern = (1l << pgroup.length); ppattern > 0; ppattern--) {
                  long p = (ppattern << groupshift) + (pattern << (groupshift + pgroup.length));
                  long modifiedpattern = termpositions.convertDuplicatesInPattern(p);
                  if (!phrases.containsKey(modifiedpattern)) {
                     phrases.put(modifiedpattern, new phrase(modifiedpattern));
                  }
               }
            }
            groupshift += pgroup.length;
         }
         pgroup = group;
      }

      return phrases;
   }
}
