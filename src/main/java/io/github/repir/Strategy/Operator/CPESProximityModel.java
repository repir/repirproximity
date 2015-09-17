package io.github.repir.Strategy.Operator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import io.github.repir.Repository.TermDocumentFeature;
import io.github.repir.Retriever.Document;
import io.github.repir.Strategy.GraphRoot;
import io.github.repir.Strategy.Tools.MatchSetLength;
import io.github.repir.Strategy.Tools.MatchSetPositional;
import io.github.repir.Strategy.Tools.ProximitySet.ProximityTerm;
import io.github.repir.Strategy.Tools.ProximityStopwordsSet;
import io.github.htools.lib.ArrayTools;
import io.github.htools.lib.Log;
import io.github.htools.lib.MathTools;

/**
 * This variant of CPE uses stop words to form valid combinations.
 * <p/>
 * @author jeroen
 */
public class CPESProximityModel extends CPEProximityModel {

   public static Log log = new Log(CPESProximityModel.class);
   //public ArrayList<Integer> removelists[];
   private ProximityTerm dependentterms[], independentterms[], possibledt[];
   private int countdt, countit;

   public CPESProximityModel(GraphRoot im, ArrayList<Operator> list) {
      super(im, list);
   }

   /**
    * Prevent stop words from being removed
    */
   @Override
   public void announce(ANNOUNCEKEY type, Operator node) {
      if (type != ANNOUNCEKEY.STOPWORD) {
         super.announce(type, node);
      }
   }

   /**
    * valid combinations are all combinations of two or more non stop words, and
    * combinations that contain stop words provided the stop words are combined
    * with all terms on the left and right that connect them to the nearest non
    * stop word or the query boundary.
    */
   @Override
   protected HashMap<Long, combination> validCombinations() {
      // set sequence on contained nodes
      for (int i = 0; i < containednodes.size(); i++) {
         containednodes.get(i).sequence = i;
      }

      // stopworddependencies will contain minimal bit patterns of valid combinations with
      // stop words with adjoing terms, e.g. from "the beatles on a zebra crossing",
      // the terms are sequenced the:0 beatles:1,etc, then 000011 and 011110 are minimal
      // valid patterns with stopwords.
      HashSet<Long> stopwordset = new HashSet<Long>(); // in bit patterns
      for (QTerm t : getStopWords()) {
         stopwordset.add(termpositions.dependency[t.sequence] | (1l << t.sequence));
      }
      long stopworddependencies[] = ArrayTools.toLongArray(stopwordset);

      ArrayList<QTerm> nonstopwords = getNonStopWords();
      HashMap<Long, combination> validcombinations = new HashMap<Long, combination>((1 << nonstopwords.size()) * (1 << stopworddependencies.length));

      // create all combinations of two or more non stop words
      for (int comb = (1 << nonstopwords.size()) - 1; comb >= 2; comb--) {
         if (MathTools.numberOfSetBits(comb) > 1) {
            long pattern = 0;
            for (int i = nonstopwords.size() - 1; i >= 0; i--) {
               if ((comb & (1 << i)) != 0) {
                  pattern |= 1l << nonstopwords.get(i).sequence;
               }
            }
            pattern = termpositions.convertDuplicatesInPattern(pattern);
            validcombinations.put(pattern, new combination(pattern));
         }
      }

      // create all combinations with at least one stop words 
      for (int comb = (1 << stopworddependencies.length) - 1; comb > 0; comb--) {
         long pattern = 0;
         for (int i = stopworddependencies.length - 1; i >= 0; i--) {
            if ((comb & (1 << i)) != 0) {
               pattern |= stopworddependencies[i];
            }
         }
         long pattern2 = termpositions.convertDuplicatesInPattern(pattern);
         validcombinations.put(pattern2, new combination(pattern2));
         ArrayList<QTerm> other = new ArrayList<QTerm>();
         for (QTerm t : nonstopwords) {
            if ((pattern & (1l << t.sequence)) == 0) // if the non stop word is not already in the pattern
            {
               other.add(t);
            }
         }
         // create all combinations with one or more terms that are not in the minimal pattern
         for (int comb2 = (1 << other.size()) - 1; comb2 > 0; comb2--) {
            pattern2 = pattern;
            for (int i = other.size() - 1; i >= 0; i--) {
               if ((comb2 & (1 << i)) != 0) {
                  pattern2 |= 1 << other.get(i).sequence;
               }
            }
            pattern2 = termpositions.convertDuplicatesInPattern(pattern2);
            validcombinations.put(pattern2, new combination(pattern2));
         }
      }
      return validcombinations;
   }

   @Override
   public void process(Document doc) {
      occurrences = null;
      // Create an proximitytermlist of term position iterators. The top proximitytermlist is sorted
      // by the next position in each term proximitytermlist. So the term iterators are always
      // in order of their next position.

      // we will only find phrases if there is more than one term present in the document
      if (termpositions.hasProximityMatches(doc)) {
         occurrences = new HashMap<Long, MatchSetLength>();
         ProximityTerm firstterm;
         do {
            // each iteration we match all possible phrases that can be created by starting
            // with the term that appears first
            firstterm = termpositions.first;
            // if the next position for the first term is still smaller than the first position
            // of the next term, we can skip this, the next position will result in smaller
            // spans.

            // try all possible phrases we can create from the terms in the proximitytermlist
            // that contains the first term

            long firsttermpattern = firstterm.bitsequence;
            long termspresent = firsttermpattern;
            int nextpos = 0;
            countit = countdt = 0;
            for (ProximityTerm lastterm : termpositions.proximitytermlist) {
               long firstandlasttermpattern = firsttermpattern | lastterm.bitsequence;
               termspresent |= lastterm.bitsequence;
               // if the first and last term cannot meet requirements using the terms in between, we can skip
               // otherwise we inspect all combinations we can make using first, last and 0 or more in between terms
               if ((firstterm.dependency == 0 || firstterm.satisfiesDependency(termspresent))
                       && (lastterm.dependency == 0 || lastterm.satisfiesDependency(termspresent))) {
                  int span = lastterm.current + lastterm.span - firstterm.current;
                  // if the next first term has a lower position than the last term,
                  // we can skip this ositions, because the next position for the first
                  // term will provide occurrences with a smaller span
                  // this.span is currently not used, but can be set to maximize spans used
                  if (firstterm.peek() < lastterm.current || this.span < span) {
                     break;
                  }

                  // create all combinations with 0 or more in between terms
                  for (long comb = (1l << countit) - 1; comb >= 0; comb--) {
                     long pattern = firstandlasttermpattern;
                     long p = 1;
                     for (int pos = 0; p <= comb; p <<= 1, pos++) {
                        if ((comb & p) != 0) {
                           pattern |= independentterms[pos].bitsequence;
                        }
                     }
                     addOccurrence(pattern, firstterm.current, span);
                  }
                  int possible = 0;
                  int covered = 0;
                  for (int i = 0; i < countdt; i++) {
                     if (dependentterms[i].satisfiesDependency(termspresent)
                             && (covered & dependentterms[i].bitsequence) == 0) {
                        possibledt[possible++] = dependentterms[i];
                        covered |= dependentterms[i].alldependency;
                     }
                  }

                  for (long comb = (1l << possible) - 1; comb > 0; comb--) {
                     long midid = firstandlasttermpattern;
                     for (int p = 1, pos = 0; p <= comb; p <<= 1, pos++) {
                        if ((comb & p) != 0) {
                           midid |= possibledt[pos].alldependency;
                        }
                     }
                     addOccurrence(midid, firstterm.current, span);
                     SKIPIT:
                     for (long comb2 = (1l << countit) - 1; comb2 > 0; comb2--) {
                        long midid2 = midid;
                        for (int p = 1, pos = 0; p <= comb2; p <<= 1, pos++) {
                           if ((comb2 & p) != 0) {
                              if ((midid2 & independentterms[pos].bitsequence) != 0) {
                                 continue SKIPIT;
                              }
                              midid2 |= independentterms[pos].bitsequence;
                           }
                        }
                        addOccurrence(midid2, firstterm.current, span);
                     }
                  }
               }
               if (lastterm.dependency == 0) {
                  independentterms[countit++] = lastterm;
               } else {
                  dependentterms[countdt++] = lastterm;
               }
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

   @Override
   public void prepareRetrieval() {
       if (poper == null)
           poper = new ArrayList<PositionalOperator>(containednodes);

      // construct the phrases contained in the proximity model. By default these
      // are all possible term combinations of two or more terms. Two variants
      // override this to allow stop words in the constructed phrases.
      termpositions = new ProximityStopwordsSet(poper);
      validcombinations = validCombinations();
      for (PositionalOperator f : poper) {
         // weight that balances the proximity model with respect to the independent term model
         f.setQueryWeight( proximityModelWeight() );
         f.setDocumentPrior(0);
         if (f.isStopword()) {
            countdt++;
         } else {
            countit++;
         }
      }
      dependentterms = new ProximityTerm[countdt];
      independentterms = new ProximityTerm[countit];
      possibledt = new ProximityTerm[countdt];
      middlepos = new ProximityTerm[containednodes.size() - 1];
   }

   /**
    * the proximity model is weighted with respect to the independent term model
    * default is 1/|Q|. Q being the query without stop words.
    */
   @Override
   public double proximityModelWeight() {
      int count = 0;
      for (Operator g : containednodes) {
         if (g instanceof QTerm && !((QTerm) g).isStopword()) {
            count++;
         }
      }
      return 1.0 / count;
   }

   /**
    * Set dependencies, which is especially useful for stop words, which are not
    * traversed when they cannot appear in valid combinations because the other
    * required terms are not present.
    */
   @Override
   public void setTDFDependencies() {
      ArrayList<TermDocumentFeature> list = new ArrayList<TermDocumentFeature>();
      for (Operator g : containednodes) {
         list.addAll(g.getRequiredTDF());
      }
      for (ProximityTerm t : termpositions.tpi) {
         if (t.dependency == 0) { // independent term, can be scored with any other independent term
            TermDocumentFeature f = containednodes.get(t.sequence).getRequiredTDF().get(0);
            for (ProximityTerm t2 : termpositions.tpi) {
               if (t != t2 && t2.dependency == 0) { // combine with other independent terms
                  TermDocumentFeature s = containednodes.get(t.sequence).getRequiredTDF().get(0);
                  f.setDependencies(new TermDocumentFeature[]{s});
               }
            }
         }
      }
      for (ProximityTerm t : termpositions.tpi) {
         if (t.dependency != 0) { // dependent term, can only be scored with dependent terms
            TermDocumentFeature d[] = new TermDocumentFeature[MathTools.numberOfSetBits(t.dependency)];
            int counttdf = 0;
            long b = 1;
            for (int j = 0; b <= t.dependency; j++, b <<= 1) {
               if ((t.dependency & b) != 0) {
                  d[counttdf++] = containednodes.get(j).getRequiredTDF().get(0);
               }
            }
            containednodes.get(t.sequence).getRequiredTDF().get(0).setDependencies(d);
         }
      }
   }
}
