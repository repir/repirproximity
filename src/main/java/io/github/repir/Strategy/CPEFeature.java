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

/**
 * Implementation of CPE as described by Vuurens & de Vries (2013) parameters:
 *
 * <p/>
 * @author jeroen
 */
public class CPEFeature extends GraphNode {

   public static Log log = new Log(CPEFeature.class);
   public DocTF doctf;
   protected double lambda = 1;  // controls the decay rate for the relevance over distance measure, default=1
   protected double gamma = 1;   // controls the weight of the proximity model, which is best left at 1
   public HashMap<Long, MatchSetLength> occurrences;
   public HashMap<Long, phrase> phrases;
   public ProximitySet termpositions;
   public ProximityTerm middlepos[];

   public CPEFeature(GraphRoot root, ArrayList<GraphNode> list) {
      super(root);

      doctf = (DocTF) root.retrievalmodel.requestFeature("DocTF:all");

      // the features in the proximity model are sorted, lowest corpus freaquency first
      // to allow estimation of distance between independent occurrence for normalization.
      add(list);

      // proximitymodel.maxspan can optionally be used to limit scoring proximity to some maximum span
      span = repository.getConfigurationInt("proximitymodel.maxspan", Integer.MAX_VALUE);

   }

   @Override
   public void doAnnounce() {
   }

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
      if (containedfeatures.size() < 2) {
         root.remove(this); // if there is only 1 term, we dont need the proximity model
      } else {
         lambda = repository.getConfigurationDouble("proximitymodel.lambda", 1.0);
         gamma = repository.getConfigurationDouble("proximitymodel.gamma", 1.0);
      }
   }

   @Override
   public void prepareRetrieval() {
      // construct the phrases contained in the proximity model. By default these
      // are all possible term combinations of two or more terms. Two variants
      // override this to allow stop words in the constructed phrases.
      termpositions = new ProximityPartialSet(containedfeatures);
      phrases = constructPhrases();
      for (GraphNode f : containedfeatures) {
         // weight that balances the proximity model with respect to the independent term model
         f.featurevalues.queryweight = proximityModelWeight();
         f.featurevalues.documentprior = 0;
      }
      middlepos = new ProximityTerm[containedfeatures.size() - 1];
   }

   /**
    * the proximity model is weighted with respect to the independent term model
    * default is 1/|Q|. For analysis, lambda was added to control the weight,
    * but experiments showed no improvement over lambda=1.
    *
    * @return
    */
   public double proximityModelWeight() {
      return 1.0 / (containedfeatures.size() * Math.pow(gamma, containedfeatures.size()));
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

      //log.info("docid %d f %d list %d", doc.docid, containedfeatures.size(), list.size());
      // we will only find phrases if there is more than one term present in the document
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
            // that contains the first term
            long minid = min.bitsequence;
            int nextpos = 0;
            for (ProximityTerm max : termpositions.proximitytermlist) {
               int span = max.current + max.span - min.current;
               if (min.peek() < max.current || this.span < span) {
                  break;
               }
               long maxid = minid | max.bitsequence;
               for (long comb = (1l << nextpos) - 1; comb >= 0; comb--) {
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

   protected void addOccurrence(long id, int pos, int span) {
      phrase p = phrases.get(id);
      if (p != null && span <= p.span) {
         MatchSetLength matches = occurrences.get(id);
         if (matches == null) {
            matches = new MatchSetLength();
            occurrences.put(id, matches);
         }
         matches.add(new ProximityOccurrence(pos, span));
      }
   }

   /**
    * Phrase occurrences of the same phrase cannot overlap, the occurrence with
    * the smallest span takes precendence or the first if the spans are equal.
    * Typically matching does not look ahead, therefore if A x B C A is
    * evaluated starting from the left, A x B C can be a valid occurrence, but
    * should in this case be dropped because B C A contains the same terms is
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

    public void clearTermScores() {
      for (GraphNode feature : containedfeatures) {
         feature.featurevalues.frequencylist = null;
         feature.featurevalues.frequency = 0;
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
               phraseprox += relevance(numberofterms, m.span);
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

   /**
    * measures the relevance of co-occurring terms based on their distance. The
    * parameter lambda was added to control speed of decay, for which the
    * default value of 1 works best for unseen data.
    *
    * @param terms number of terms in the phrase
    * @param span number of word positions covered by a phrase occurrence in the
    * text
    * @return
    */
   protected double relevance(int terms, double span) {
      if (span <= 1) {
         return 1;
      } else {
         return (terms - 1) / ((terms - 1) + (span - terms) * lambda);
      }
   }

   protected HashMap<Long, phrase> constructPhrases() {
      HashMap<Long, phrase> phrases = new HashMap<Long, phrase>();
      long maxid = (1l << containedfeatures.size());
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
      return phrases;
   }

   @Override
   public GraphNode clone(GraphRoot newmodel) {
      CPEFeature f = new CPEFeature(newmodel, new ArrayList<GraphNode>());
      for (GraphNode feature : containedfeatures) {
         f.add(feature.clone(newmodel));
      }
      featurevalues.copy(f.featurevalues);
      return f;
   }

   @Override
   public void setTDFDependencies() {
      ArrayList<TermDocumentFeature> list = new ArrayList<TermDocumentFeature>();
      for (GraphNode g : containedfeatures) {
         list.addAll(g.getRequiredTDF());
      }
      for (TermDocumentFeature f : list) {
         for (TermDocumentFeature g : list) {
            if (f != g)
               f.setDependencies(new TermDocumentFeature[] { g });
         }
      }
   }

//   @Override
//   public ArrayList<TermDocumentFeature> getRequiredTDF() {
//      return new ArrayList<TermDocumentFeature>();
//   }

   /**
    * symbolizes a set of terms that can be matched in phrasestring. From the id
    * of a phrase the containing terms can be derived, as it is a bit-field that
    * indicates that feature0, feature1, etc., are element of this phrase.
    */
   public class phrase {
      // id is a bit fingerprint that indicates which terms are contained in the phrase
      // i.e. if the i-th bit is set, the i-th element of containedfeatures is
      // an element of the phrase.

      final long id;
      final int termid[];
      double weight = 1;
      final int span = Integer.MAX_VALUE;
      final String phrasestring;

      public phrase(long id) {
         this.id = id;
         termid = containingTerms();
         phrasestring = toTermString(id, containedfeatures).trim();
      }

      public int[] containingTerms() {
         int terms[] = new int[io.github.repir.tools.Lib.MathTools.numberOfSetBits(id)];
         int pos = 0;
         for (int t = 0; t < containedfeatures.size(); t++) {
            if ((id & (1l << t)) > 0) {
               terms[pos++] = t;
            }
         }
         return terms;
      }

      public String toString() {
         return phrasestring;
      }
   }
   
   public String toTermString() {
      return "CPEFEature:(" + super.toTermString()+ ")";
   }
}
