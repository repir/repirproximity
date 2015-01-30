package io.github.repir.Strategy.Operator;

import io.github.repir.Repository.DocTF;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.TermDocumentFeature;
import io.github.repir.Retriever.Document;
import io.github.repir.Strategy.GraphRoot;
import io.github.repir.Strategy.Tools.MatchSetLength;
import io.github.repir.Strategy.Tools.MatchSetPositional;
import io.github.repir.Strategy.Tools.ProximityOccurrence;
import io.github.repir.Strategy.Tools.ProximityPartialSet;
import io.github.repir.Strategy.Tools.ProximitySet;
import io.github.repir.Strategy.Tools.ProximitySet.ProximityTerm;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.lib.MathTools;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of CPE as described by Vuurens & de Vries (2014).
 * <p/>
 * @author jeroen
 */
public class CPEProximityModel extends Operator {

   public static Log log = new Log(CPEProximityModel.class);
   int maxcombinationspan = Integer.MAX_VALUE;
   ArrayList<PositionalOperator> poper;
   public HashMap<Long, MatchSetLength> occurrences;
   public HashMap<Long, combination> validcombinations;
   public ProximitySet termpositions;
   public ProximityTerm middlepos[];

   public CPEProximityModel(GraphRoot root, ArrayList<Operator> list) {
      super(root);

      // the features in the proximity model are sorted, lowest corpus freaquency first
      // to allow estimation of distance between independent occurrence for normalization.
      add(list);

      // proximitymodel.maxspan can optionally be used to limit scoring proximity to some maximum span
      span = repository.configuredInt("proximitymodel.maxspan", Integer.MAX_VALUE);
      maxcombinationspan = repository.configuredInt("cpe.maxspan", Integer.MAX_VALUE);
   }
   
   public CPEProximityModel(Collection<? extends PositionalOperator> list) {
      super((Repository)null);
      poper = new ArrayList<PositionalOperator>(list);
      span = Integer.MAX_VALUE;
   }
   
   @Override
   public void doAnnounce() { }

   @Override
   public boolean willBeScored(boolean willbescored) {
      super.willBeScored(willbescored);
      return willbescored;
   }

   @Override
   public boolean positional(boolean positional) {
      return super.positional(true);
   }

   @Override
   public void configureFeatures() {
      if (containednodes.size() < 2) {
         root.remove(this); // if there is only 1 term, we dont need the proximity model
      }
   }

   @Override
   public void prepareRetrieval() {
       if (poper == null)
           poper = new ArrayList<PositionalOperator>(containednodes);
      // construct the phrases contained in the proximity model. By default these
      // are all possible term combinations of two or more terms. Two variants
      // override this to allow stop words in the constructed phrases.
      termpositions = new ProximityPartialSet(poper);
      validcombinations = validCombinations();
      for (PositionalOperator f : poper) {
         // weight that balances the proximity model with respect to the independent term model
         f.setQueryWeight( proximityModelWeight() );
         f.setDocumentPrior(0);
      }
      middlepos = new ProximityTerm[poper.size() - 1];
   }

   /**
    * the proximity model is weighted with respect to the independent term model
    * default is 1/|Q|.
    */
   public double proximityModelWeight() {
      return 1.0 / poper.size();
   }

   /**
    * Process the contents of Document doc. For the proximity model, this method
    * matches all possible phrases (word combinations) in the document. Two
    * occurrences of the same phrase cannot occupy the same position. Priority
    * goes to the occurrence with the smallest span or the first occurrence if
    * the span is the same.
    * <p/>
    * The results are kept in the map<Integer, ProximityOccurrence> occurrences,
    * for the score() method to use when scoring.
    */
   @Override
   public void process(Document doc) {
      occurrences = null;
      // Create an proximitytermlist of term position iterators. The top proximitytermlist is sorted
      // by the next position in each term proximitytermlist. So the term iterators are always
      // in order of their next position.

      // we will only find occurrences if there is more than one term present in the document
      if (termpositions.hasProximityMatches(doc)) {
         occurrences = new HashMap<Long, MatchSetLength>();
         ProximityTerm min;

         do {

            // each iteration we ProximityOccurrence all possible phrases that can be created by starting
            // with the term that appears first
            min = termpositions.first;
            // if the next position for the first term is still smaller than the first position
            // of the next term, we can skip this, the next position will result in smaller
            // spans.

            // try all possible phrases we can create from the terms in the proximitytermlist
            // that contains the first term. 
            long minid = min.bitsequence;
            int nextpos = 0;
            for (ProximityTerm max : termpositions.proximitytermlist) {
               int span = max.current + max.span - min.current;
               if (min.peek() < max.current || this.span < span) {
                  break;
               }
               long maxid = minid | max.bitsequence;

               // The occurrences found are represented by
               // a bit pattern, e.g. 11001 indicates that the 1st, 4th and 5th term are
               // in the occurrence. 
               addOccurrence(maxid, min.current, span);
               for (long comb = (1l << nextpos) - 1; comb > 0; comb--) {
                  long midid = maxid;
                  long p = 1;
                  for (int pos = 0; p <= comb; p <<= 1, pos++) {
                     if ((comb & p) != 0) {
                        midid |= middlepos[pos].bitsequence;
                     }
                  }
                  addOccurrence(midid, min.current, span);
               }
               middlepos[nextpos++] = max;
            }
         } while (termpositions.next());
         // It is possible that two occurrences of the same phrase were added with
         // overlapping positions. This method removes the smallest or otherwise
         // right-most.

         HashMap<Long, MatchSetPositional> results = new HashMap<Long, MatchSetPositional>();
         for (Map.Entry<Long, MatchSetLength> entry : occurrences.entrySet()) {
            results.put(entry.getKey(), entry.getValue().purgeOccurrencesOccupyingSameSpace());
         }
         setupTermScores(results);
      } else {
         clearTermScores();
      }
   }

   /*
    * Add a proximity occurrence identified by a bit pattern, position and span.
    * The occurrence is only added if it is a valid combination.
    */
   protected void addOccurrence(long pattern, int pos, int span) {
      combination p = validcombinations.get(pattern);
      if (p != null && span <= p.span) {
         MatchSetLength matches = occurrences.get(pattern);
         if (matches == null) {
            matches = new MatchSetLength();
            occurrences.put(pattern, matches);
         }
         matches.add(new ProximityOccurrence(pos, span));
      }
   }

   /**
    * Phrase occurrences of the same phrase cannot overlap, the occurrence with
    * the smallest span takes precendence or the first if the spans are equal.
    * Typically matching does not look ahead, therefore if A x B C A is
    * evaluated starting from the left, A x B C can be a valid occurrence, but
    * should in this case be dropped because B C A is the same combination and
    * has a smaller span.
    *
    * @param doc
    */
   protected void purgeOccurrencesOccupyingSameSpace(HashMap<Integer, MatchSetLength> occurrences) {
      for (Map.Entry<Integer, MatchSetLength> entry : occurrences.entrySet()) {
         if (entry.getValue().size() > 1) {
            ProximityOccurrence matches[] = entry.getValue().toArray(new ProximityOccurrence[entry.getValue().size()]);
            ArrayList<ProximityOccurrence> remove = new ArrayList<ProximityOccurrence>();
            for (int i = 0; i < matches.length; i++) {
               if (matches[i] != null) {
                  ProximityOccurrence last = matches[i];
                  int mend = last.pos + last.span - 1;
                  for (int j = i + 1; j < matches.length; j++) {
                     if (matches[j] != null) {
                        ProximityOccurrence next = matches[j];
                        if ((next.pos >= last.pos && next.pos <= mend) || (next.pos + next.span >= last.pos && next.pos + next.span <= mend)) {
                           remove.add(next);
                           matches[j] = null;
                        }
                     }
                  }
               }
            }
            for (ProximityOccurrence m : remove) {
               entry.getValue().remove(m);
            }
         }
      }
   }

   /**
    * Clear term scores, so nothing s scored.
    */
   public void clearTermScores() {
      for (PositionalOperator feature : poper) {
         feature.clearFrequencyList(); 
         feature.setFrequency(0);
      }
   }

   /**
    * returns the proximity score for a document, using the matches that were
    * counted by {@link #process(IndexReader.Document)}.
    * <p/>
    * @param doc
    * @return
    */
   public void setupTermScores(HashMap<Long, MatchSetPositional> occurrences) {
      for (PositionalOperator feature : poper) {
         feature.clearFrequencyList();
         feature.setFrequency(0);
      }

      if (occurrences != null) {
         // map key is the phrase id, value contains the occurrences
         for (Map.Entry<Long, MatchSetPositional> entry : occurrences.entrySet()) {
            long id = entry.getKey();
            combination combination = validcombinations.get(id);
            int numberofterms = combination.size;
            double phraseprox = 0;

            // calculate normalized sumprox for occurrences, i.e. only use occurrences
            // that score better than independent occurrence of terms.
            for (ProximityOccurrence m : entry.getValue()) {
               phraseprox += relevance(numberofterms, m.span);
            }

            // score for all unigrams in the phrase
            // KLD( normalizedsumprox )/|Q|
            for (PositionalOperator term : combination.terms) {
               term.getFrequencyList().add(phraseprox);
            }
         }
      }
   }

   /**
    * measures the relevance of co-occurring terms based on their distance.
    *
    * @param terms number of terms in the phrase
    * @param span number of word positions covered by a phrase occurrence in the
    * text
    * @return
    */
   protected double relevance(int terms, double span) {
      return (terms - 1) / (double) (span - 1);
   }

   /**
    * @return Map containing all valid combinations of {@link QTerm}s, which is
    * defined by the PowerSet[>1](Q), i.e. all combinations of two of more Terms.
    * The Map's Key is the bit pattern identifier (i.e. if the i-th bit is set
    * this indicates the i-th Term is part of the combination), and the Map's
    * value is a combination object.
    */
   protected HashMap<Long, combination> validCombinations() {
      HashMap<Long, combination> phrases = new HashMap<Long, combination>();
      long maxid = (1l << poper.size());
      ID:
      for (int pattern = 2; pattern < maxid; pattern++) {
         int phrase_number_of_terms = io.github.repir.tools.lib.MathTools.numberOfSetBits(pattern);
         if (phrase_number_of_terms > 1) {
            long modifiedpattern = termpositions.convertDuplicatesInPattern(pattern);
            if (!phrases.containsKey(modifiedpattern)) {
               phrases.put(modifiedpattern, new combination(modifiedpattern));
            }
         }
      }
      return phrases;
   }

   @Override
   public Operator clone(GraphRoot newmodel) {
      CPEProximityModel f = new CPEProximityModel(newmodel, new ArrayList<Operator>());
      for (Operator feature : containednodes) {
         f.add(feature.clone(newmodel));
      }
      return f;
   }

   /**
    * Settings dependencies is not really needed here, because independent terms
    * will be scored and thus all dependencies will be reset to none.
    */
   @Override
   public void setTDFDependencies() {
      ArrayList<TermDocumentFeature> list = new ArrayList<TermDocumentFeature>();
      for (Operator g : containednodes) {
         list.addAll(g.getRequiredTDF());
      }
      for (TermDocumentFeature f : list) {
         for (TermDocumentFeature g : list) {
            if (f != g) {
               f.setDependencies(new TermDocumentFeature[]{g});
            }
         }
      }
   }

   /**
    * symbolizes a set of terms that can be matched as a term combination. From
    * the id of a phrase the containing terms can be derived, as it is a
    * bit-field that indicates that feature0, feature1, etc., are element of
    * this phrase.
    */
   public class combination {
      // id is a bit fingerprint that indicates which terms are contained in the phrase
      // i.e. if the i-th bit is set, the i-th element of containednodes is
      // an element of the phrase.

      final long id;
      final PositionalOperator terms[];    // terms that are part of this combination
      final int size;                      // number of terms combined
      final int span = maxcombinationspan; // maximum allowed span for an occurrence, only used for testing
      final String phrasestring;           // for debug purposes

      public combination(long id) {
         this.id = id;
         terms = containingTerms();
         size = terms.length;
         phrasestring = (containednodes != null)?toTermString(id, containednodes).trim():"";
      }

      public PositionalOperator[] containingTerms() {
         PositionalOperator terms[] = new PositionalOperator[MathTools.numberOfSetBits(id)];
         int pos = 0;
         for (int t = 0; t < poper.size(); t++) {
            if ((id & (1l << t)) > 0) {
               terms[pos++] = poper.get(t);
            }
         }
         return terms;
      }
      
      public String toString() {
         return phrasestring;
      }
   }

   public String toTermString() {
      return "CPEFEature:(" + super.toTermString() + ")";
   }
}
