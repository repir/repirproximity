package io.github.repir.Strategy;

//package vu.nl.iref.Strategy;
//
//import vu.nl.iref.Retriever.Document;
//import vu.nl.iref.tools.Lib.Log;
//import vu.nl.iref.Repository.DocTF;
//import vu.nl.iref.Strategy.FeaturePhrase.TermPositions;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.TreeSet;
//import vu.nl.iref.tools.DataTypes.TreeMapComparable;
//import vu.nl.iref.tools.DataTypes.TreeMapComparable.TYPE;
//
///**
// * Implementation of CPE as described by Vuurens & de Vries (2013)
// * parameters:
// * 
// * <p/>
// * @author jeroen
// */
//public class ProximityFeatureOld extends GraphNode {
//
//   public static Log log = new Log(ProximityFeatureOld.class);
//   public DocTF doctf;
//   protected int normalize = -1;
//   protected double lambda = 1;  // controls the decay rate for the relevance over distance measure, default=1
//   protected double gamma = 1;   // controls the weight of the proximity model, which is best left at 1
//   public HashMap<Integer, TreeSet<match>> occurrences;
//   public HashMap<Integer, phrase> phrases;
//
//   public ProximityFeatureOld(GraphRoot root, ArrayList<GraphNode> list) {
//      super(root);
//
//      doctf = (DocTF) root.retrievalmodel.requestFeature("DocTF:all");
//
//      // the features in the proximity model are sorted, lowest corpus freaquency first
//      // to allow estimation of distance between independent occurrence for normalization.
//      add(list);
//
//      // proximitymodel.maxspan can optionally be used to limit scoring proximity to some maximum span
//      span = repository.getConfigurationInt("proximitymodel.maxspan", Integer.MAX_VALUE);
//
//   }
//
//   public void setnormalize(Long value) {
//      normalize = value.intValue();
//   }
//
//   public void doAnnounce() {
//   }
//   
//   @Override
//   public boolean willBeScored( boolean willbescored ) {
//      super.willBeScored(willbescored);
//      return willbescored;
//   }
//   
//   @Override
//   public boolean positional( boolean positional ) {
//      return super.positional( true );
//   }
//   
//   @Override
//   public void configureFeatures() {
//      // if normalize=true then relevance() is modified to be 0 when the span of terms
//      // equals estimated independent occurrence and sum(relevance()) is 0 when it does not
//      // exceed the sum of relevance() for estimated independent occurrences.
//      if (normalize == -1) {
//         normalize = repository.getConfigurationBoolean("proximitymodel.normalize", true) ? 1 : 0;
//      }
//      lambda = repository.getConfigurationDouble("proximitymodel.lambda", 1.0);
//      gamma = repository.getConfigurationDouble("proximitymodel.gamma", 1.0);
//
//      if (normalize == 1) {
//         containedfeatures = sortTerms(containedfeatures);
//      }
//   }
//   
//   @Override
//   public void finalSetup() {
//      
//      // construct the phrases contained in the proximity model. By default these
//      // are all possible term combinations of two or more terms. Two variants
//      // override this to allow stop words in the constructed phrases.
//      phrases = constructPhrases();
//      for (GraphNode f : containedfeatures) {
//         // weight that balances the proximity model with respect to the independent term model
//         f.featurevalues.queryweight = proximityModelWeight();
//         f.featurevalues.documentprior = 0;
//      }
//   }
//   
//   /**
//    * the proximity model is weighted with respect to the independent term model
//    * default is 1/|Q|. For analysis, lambda was added to control 
//    * the weight, but experiments showed no improvement over lambda=1.
//    * @return 
//    */
//   public double proximityModelWeight() {
//      return 1.0 / Math.pow( containedfeatures.size(), gamma );
//   }
//
//   /**
//    * Process the contents of Document doc. For the proximity model, this method
//    * matches all possible phrases (word combinations) in the document. Two
//    * occurrences of the same phrase cannot occupy the same position. Priority
//    * goes to the occurrence with the smallest span or the first occurrence if
//    * the span is the same.
//    * <p/>
//    * The results are kept in the map<Integer, match> occurrences, for the
//    * score() method to use when scoring.
//    */
//   @Override
//   public void process(Document doc) {
//      occurrences = null;
//      log.time();
//
//      // Create an iterator of term position iterators. The top iterator is sorted
//      // by the next position in each term iterator. So the term iterators are always
//      // in order of their next position.
//      TreeSet<FeaturePhrase.TermPositions> list = getIterators( doc );
//
//      // we will only find phrases if there is more than one term present in the document
//      if (list.size() > 1) {
//         occurrences = new HashMap<Integer, TreeSet<match>>();
//         HashMap<Integer, match> lastoccurrence = new HashMap<Integer, match>();
//
//         // termpos is an array with all position lists in the iterator, for fast
//         // access.
//         TermPositions termpos[] = list.toArray(new TermPositions[list.size()]);
//         while (list.size() > 1) {
//
//            // each iteration we match all possible phrases that can be created by starting
//            // with the term that appears first
//            TermPositions min = list.pollFirst();
//            // if the next position for the first term is still smaller than the first position
//            // of the next term, we can skip this, the next position will result in smaller
//            // spans.
//            while (min.peek() < list.first().current) {
//               min.next();
//            }
//
//            // try all possible phrases we can create from the terms in the iterator
//            // that contains the first term
//            int minbit = 1 << vu.nl.iref.tools.Lib.ArrayTools.indexOf(termpos, min);
//            for (int combination = (int) Math.pow(2, termpos.length) - 1; combination > 2; combination--) {
//               if ((combination & minbit) == minbit && vu.nl.iref.tools.Lib.MathTools.numberOfSetBits(combination) > 1) {
//                  // built a phrase id code using there position in the containedfeatures list
//                  int id = 1 << min.sequence;
//                  int endpos = 0;
//                  for (int j = 0; j < termpos.length; j++) {
//                     int bit = 1 << j;
//                     if ((combination & bit) > 0 && termpos[j] != min) {
//                        id |= 1 << termpos[j].sequence;
//                        endpos = Math.max(endpos, termpos[j].current + termpos[j].span);
//                        if (endpos - min.current <= span && min.peek() >= endpos) {
//                           addOccurrence(id, min.current, endpos - min.current);
//                        }
//                     }
//                  }
//               }
//            }
//            if (min.next() != Integer.MAX_VALUE) {
//               // there is another position for this term, so add back to the iterator
//               list.add(min);
//            } else if (list.size() > 1) {
//               // this position iterator is out of new positions, so don't add it back
//               // to the iterator, and reassign the array of term positions.
//               termpos = list.toArray(new TermPositions[list.size()]);
//            }
//         }
//         // It is possible that two occurrences of the same phrase were added with
//         // overlapping positions. This method removes the smallest or otherwise
//         // right-most.
//         purgeOccurrencesOccupyingSameSpace(occurrences);
//         log.clock();
//      }
//      setupTermScores();
//   }
//
//   /**
//    * returns a multiple iterator for all the query terms in the document. The first
//    * element in the TreeSet contains the next term in the document. Pulling the first
//    * element off (PollFirst), gives the starting position of a term combination,
//    * going through the remainder of the list in sequence gives 
//    * @param doc
//    * @return 
//    */
//   protected TreeSet<FeaturePhrase.TermPositions> getIterators(Document doc) {
//      TreeSet<FeaturePhrase.TermPositions> list = new TreeSet<FeaturePhrase.TermPositions>();
//      for (int f = 0; f < containedfeatures.size(); f++) {
//         GraphNode feature = containedfeatures.get(f);
//         feature.process(doc);
//         if (feature.featurevalues.frequency > 0) {
//            FeaturePhrase.TermPositions t = new TermPositions(feature.featurevalues, f, feature.span);
//            list.add(t);
//         }
//      }
//      return list;
//   }
//   
//   private void addOccurrence(int id, int pos, int length) {
//      if (phrases.containsKey(id)) {
//         TreeSet<match> matches = occurrences.get(id);
//         if (matches == null) {
//            matches = new TreeSet<match>();
//            occurrences.put(id, matches);
//         }
//         matches.add(new match(pos, length));
//      }
//   }
//
//   /**
//    * Phrase occurrences of the same phrase cannot overlap, the occurrence with
//    * the smallest span takes precendence or the first if the spans are equal.
//    * @param doc 
//    */
//   private void purgeOccurrencesOccupyingSameSpace(HashMap<Integer, TreeSet<match>> occurrences) {
//      for (Map.Entry<Integer, TreeSet<match>> entry : occurrences.entrySet()) {
//         if (entry.getValue().size() > 1) {
//            match matches[] = entry.getValue().toArray(new match[entry.getValue().size()]);
//            ArrayList<match> remove = new ArrayList<match>();
//            for (int i = 0; i < matches.length; i++) {
//               if (matches[i] != null) {
//                  match last = matches[i];
//                  int mend = last.pos + last.span - 1;
//                  for (int j = i + 1; j < matches.length; j++) {
//                     if (matches[j] != null) {
//                        match next = matches[j];
//                        if ((next.pos >= last.pos && next.pos <= mend) || (next.pos + next.span >= last.pos && next.pos + next.span <= mend)) {
//                           remove.add(next);
//                           matches[j] = null;
//                        }
//                     }
//                  }
//               }
//            }
//            for (match m : remove) {
//               entry.getValue().remove(m);
//            }
//         }
//      }
//   }
//
//   /**
//    * returns the proximity score for a document, using the matches that were
//    * counted by {@link #process(IndexReader.Document)}.
//    * <p/>
//    * @param doc
//    * @return
//    */
//   public void setupTermScores() {
//      for (GraphNode feature : containedfeatures) {
//         feature.featurevalues.frequencylist = new ArrayList<Double>();
//         feature.featurevalues.frequency = 0;
//      }
//
//      if (occurrences != null) {
//         // map key is the phrase id, value contains the occurrences
//         for (Map.Entry<Integer, TreeSet<match>> entry : occurrences.entrySet()) {
//            int id = entry.getKey();
//            phrase phrase = phrases.get(id);
//            int numberofterms = phrase.termid.length;
//            double phraseprox = 0;
//
//            // calculate normalized sumprox for occurrences, i.e. only use occurrences
//            // that score better than independent occurrence of terms.
//            for (match m : entry.getValue()) {
//               double prox = relevance(numberofterms, m.span);
//               if (normalize == 1) {
//                  if (prox > phrase.independentprox) {
//                     phraseprox += (prox - phrase.independentprox) / (1 - phrase.independentprox);
//                  }
//               } else {
//                  phraseprox += prox;
//               }
//            }
//
//            // the sum of proximity when all terms would occur independently
//            // is number of phrase occurences * independentprox
//            int phrasefreq = entry.getValue().size();
//            double independentsumprox = phrasefreq * phrase.independentprox;
//
//            // if normalize=true, only score proximity evidence if it exceeds independent occurrence
//            if (normalize == 0 || phraseprox > independentsumprox) {
//               // score for all unigrams in the phrase
//               // KLD( normalizedsumprox )/|Q|
//               for (int termid : phrase.termid) {
//                  containedfeatures.get(termid).featurevalues.frequencylist.add(phraseprox);
//              }
//            }
//         }
//      }
//   }
//
//   /**
//    * measures the relevance of co-occurring terms based on their distance. The 
//    * parameter lambda was added to control speed of decay, for which the default value
//    * of 1 works best for unseen data.
//    * @param terms number of terms in the phrase
//    * @param span number of word positions covered by a phrase occurrence in the text
//    * @return 
//    */
//   protected double relevance(int terms, double span) {
//      if (span <= 1) {
//         return 1;
//      } else {
//         return (terms - 1) / ((terms-1) + (span-terms) * lambda);
//      }
//   }
//
//   protected HashMap<Integer, phrase> constructPhrases() {
//      HashMap<Integer, phrase> phrases = new HashMap<Integer, phrase>();
//      int maxid = (1 << containedfeatures.size());
//      ID: for (int id = 2; id < maxid; id++) {
//         int phrase_number_of_terms = vu.nl.iref.tools.Lib.MathTools.numberOfSetBits(id);
//         if (phrase_number_of_terms > 1) {
//            
//            phrases.put(id, new phrase(id));
//         }
//      }
//      return phrases;
//   }
//   
//   /**
//    * Needed only for model normalization, to sort the terms in order of corpus 
//    * frequency
//    * @param features
//    * @return 
//    */
//   private ArrayList<GraphNode> sortTerms(ArrayList<GraphNode> features) {
//      TreeMapComparable<Integer, GraphNode> map = new TreeMapComparable<Integer, GraphNode>(TYPE.DUPLICATESASCENDING);
//      for (GraphNode n : features) {
//         map.put((int) n.featurevalues.corpusfrequency, n);
//      }
//      return new ArrayList<GraphNode>(map.values());
//   }
//
//   @Override
//   public GraphNode clone(GraphRoot newmodel) {
//      ProximityFeatureOld f = new ProximityFeatureOld(newmodel, new ArrayList<GraphNode>());
//      for (GraphNode feature : containedfeatures) {
//         f.add(feature.clone(newmodel));
//      }
//      featurevalues.copy(f.featurevalues);
//      return f;
//   }
//
//   @Override
//   public void processCollected() { }
//
//   @Override
//   public void readStatistics() { }
//
//   /**
//    * symbolizes a set of terms that can be matched in phrasestring. From the id
//    * of a phrase the containing terms can be derived, as it is a bit-field that
//    * indicates that feature0, feature1, etc., are element of this phrase.
//    */
//   public class phrase {
//      // id is a bit fingerprint that indicates which terms are contained in the phrase
//      // i.e. if the i-th bit is set, the i-th element of containedfeatures is
//      // an element of the phrase.
//      final int id; 
//      final int termid[];
//      final String phrasestring;
//      final double independentprox;
//
//      public phrase(int id) {
//         this.id = id;
//         termid = containingTerms();
//         independentprox = (normalize == 1) ? expectedRelevance() : 0;
//         phrasestring = toTermString(id, containedfeatures).trim();
//      }
//
//      public int[] containingTerms() {
//         int terms[] = new int[vu.nl.iref.tools.Lib.MathTools.numberOfSetBits(id)];
//         int pos = 0;
//         for (int t = 0; t < containedfeatures.size(); t++) {
//            if ((id & (1 << t)) > 0) {
//               terms[pos++] = t;
//            }
//         }
//         return terms;
//      }
//
//      public String toString() {
//         return phrasestring;
//      }
//
//      /**
//       * @return the relevance score when the phrase covers a distance that indicates
//       * independent co-occurrence.
//       */
//      protected double expectedRelevance() {
//         double avg_span_S = 1;
//         int s0 = 0;
//         for (; ((1 << s0) & id) == 0; s0++);
//         for (int s1 = s0 + 1; s1 < containedfeatures.size(); s1++) {
//            if (((1 << s1) & id) > 0) {
//               long freq_S1 = containedfeatures.get(s1).featurevalues.corpusfrequency;
//               double halfslotsize = (repository.getCorpusTF() - freq_S1) / (double) (2 * (freq_S1));
//               double avgdist = (halfslotsize + 1) / 2;
//               if (avg_span_S == 0) {
//                  avg_span_S += avgdist;
//               } else {
//                  avg_span_S += avgdist / 2;
//               }
//            }
//         }
//         return relevance(termid.length, avg_span_S);
//      }
//   }
//
//   /**
//    * Represents a phrase occurrence in a document starting at position pos and
//    * with a word span
//    */
//   public class match implements Comparable<match> {
//
//      public int pos;
//      public int span;
//
//      public match(int pos, int length) {
//         this.pos = pos;
//         this.span = length;
//      }
//
//      @Override
//      public int compareTo(match o) {
//         return (span < o.span) ? -1 : (span > o.span) ? 1 : pos - o.pos;
//      }
//   }
//}
