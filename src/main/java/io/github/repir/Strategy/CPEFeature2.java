package io.github.repir.Strategy;

import io.github.repir.Strategy.Term;
import io.github.repir.Strategy.GraphNode;
import io.github.repir.Strategy.GraphRoot;
import io.github.repir.Retriever.Document;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Repository.DocTF;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import io.github.repir.Repository.TermDocumentFeature;
import io.github.repir.Strategy.Tools.MatchSetLength;
import io.github.repir.Strategy.Tools.MatchSetPositional;
import io.github.repir.Strategy.Tools.ProximityOccurrence;
import io.github.repir.Strategy.Tools.ProximityPartialSet;
import io.github.repir.Strategy.Tools.ProximitySet;
import io.github.repir.Strategy.Tools.ProximitySet.ProximityTerm;
import io.github.repir.tools.Lib.MathTools;

/**
 * Implementation of CPE as described by Vuurens & de Vries (2013) parameters:
 *
 * <p/>
 * @author jeroen
 */
public class CPEFeature2 extends CPEFeature {

   public static Log log = new Log(CPEFeature2.class);
   ArrayList<GraphNode> originalfeatures;


   public CPEFeature2(GraphRoot root, ArrayList<GraphNode> list) {
      super(root, list);
      originalfeatures = list;
   }

   public void setupTermScores(HashMap<Long, MatchSetPositional> occurrences) {
      for (GraphNode feature : containedfeatures) {
         feature.featurevalues.frequencylist = new ArrayList<Double>();
         feature.featurevalues.frequency = 0;
      }

      if (occurrences != null) {
         // map key is the phrase id, value contains the occurrences
         for (Map.Entry<Long, MatchSetPositional> entry : occurrences.entrySet()) {
            long id = entry.getKey();
            phrase phrase = phrases.get(id);
            int numberofterms = phrase.termid.length;
            double phraseprox = 0;

            // calculate normalized sumprox for occurrences, i.e. only use occurrences
            // that score better than independent occurrence of terms.
            for (ProximityOccurrence m : entry.getValue()) {
               phraseprox += phrase.weight * relevance(numberofterms, m.span);
            }

            // score for all unigrams in the phrase
            // KLD( normalizedsumprox )/|Q|
            for (int termid : phrase.termid) {
               Term t = (Term) containedfeatures.get(termid);
               containedfeatures.get(termid).featurevalues.frequencylist.add(phraseprox);
            }
         }
      }
   }

   protected HashMap<Long, phrase> constructPhrases() {
      HashMap<Long, phrase> phrases = new HashMap<Long, phrase>();
      for (int i = 0; i < originalfeatures.size(); i++)
         originalfeatures.get(i).sequence = i;
      long maxid = (1l << containedfeatures.size());
      ID:
      for (int pattern = 2; pattern < maxid; pattern++) {

         int phrase_number_of_terms = MathTools.numberOfSetBits(pattern);
         
         if (phrase_number_of_terms > 1) {
            int originalpattern = 0;
            for (int t = 0; t < containedfeatures.size(); t++) {
               if ((pattern & (1l << t)) > 0) {
                  originalpattern |= (1 << containedfeatures.get(t).sequence);
               }
            }
            double weight = relevance(phrase_number_of_terms, MathTools.numberCoverBits(originalpattern));

            long modifiedpattern = termpositions.convertDuplicatesInPattern(pattern);
            phrase p = phrases.get(modifiedpattern);
            if (p == null) {
               p = new phrase(modifiedpattern);
               p.weight = weight;
               phrases.put(modifiedpattern, p);
            } else {
               p.weight = Math.max(weight, p.weight);
            }
         }
      }
      for (phrase p : phrases.values()) {
         log.info("phrase %s %f", p, p.weight);
      }
      return phrases;
   }
}
