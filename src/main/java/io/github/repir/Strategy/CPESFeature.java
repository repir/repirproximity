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
public class CPESFeature extends CPEFeature {

   public static Log log = new Log(CPESFeature.class);
   //public ArrayList<Integer> removelists[];
   private ProximityTerm dependentterms[], independentterms[], possibledt[];
   private int countdt, countit;

   public CPESFeature(GraphRoot im, ArrayList<GraphNode> list) {
      super(im, list);
   }

   @Override
   public void announce(ANNOUNCEKEY type, GraphNode node) {
      if (type != ANNOUNCEKEY.STOPWORD) {
         super.announce(type, node);
      }
   }

   /**
    * valid combinations can only contain stop words that are either linked both
    * on the left and right by the query beginning/end or a non stop word.
    *
    * @return
    */
   @Override
   protected HashMap<Long, phrase> constructPhrases() {
      HashSet<Long> stopwdep = new HashSet<Long>();
      for (int i = 0; i < containedfeatures.size(); i++)
         containedfeatures.get(i).sequence = i;
      for (Term t : getStopWords()) {
         stopwdep.add(termpositions.dependency[t.sequence] | (1l << t.sequence));
      }
      long stopdep[] = ArrayTools.toLongArray(stopwdep);
      
      ArrayList<Term> nsw = getNonStopWords();
      HashMap<Long, phrase> phrases = new HashMap<Long, phrase>( (1 << nsw.size()) * (1 << stopdep.length) );
      for (int comb = (1 << nsw.size())-1; comb >= 2; comb--) {
         long pattern = 0;
         if (MathTools.numberOfSetBits(comb) > 1) {
            for (int i = nsw.size()-1; i>= 0; i--)
               if ((comb & (1 << i)) != 0)
                  pattern |= 1l << nsw.get(i).sequence;
            pattern = termpositions.convertDuplicatesInPattern(pattern);
            phrases.put(pattern, new phrase(pattern));
         }
      }
      
      for (int comb = (1 << stopdep.length)-1; comb > 0; comb--) {
         long pattern = 0;
         for (int i = stopdep.length-1; i>= 0; i--)
            if ((comb & (1 << i)) != 0)
               pattern |= stopdep[i];
         long pattern2 = termpositions.convertDuplicatesInPattern(pattern);
         phrases.put(pattern2, new phrase(pattern2));
         ArrayList<Term> other = new ArrayList<Term>();
         for (Term t : nsw)
            if ((pattern & (1l << t.sequence)) == 0)
               other.add(t);
         for (int comb2 = (1 << other.size())-1; comb2 > 0; comb2--) {
            pattern2 = pattern;
            for (int i = other.size()-1; i >= 0; i--) {
               if ((comb2 & (1 << i)) != 0)
                  pattern2 |= 1 << other.get(i).sequence;
            }
            pattern2 = termpositions.convertDuplicatesInPattern(pattern2);
            phrases.put(pattern2, new phrase(pattern2));
         }
      }
      return phrases;
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
         ProximityTerm min;
         do {
            // each iteration we match all possible phrases that can be created by starting
            // with the term that appears first
            min = termpositions.first;
            // if the next position for the first term is still smaller than the first position
            // of the next term, we can skip this, the next position will result in smaller
            // spans.

            // try all possible phrases we can create from the terms in the proximitytermlist
            // that contains the first term

            long minid = min.bitsequence;
            long termspresent = minid;
            int nextpos = 0;
            countit = countdt = 0;
            for (ProximityTerm max : termpositions.proximitytermlist) {
               long maxid = minid | max.bitsequence;
               termspresent |= max.bitsequence;
               if ((min.dependency == 0 || min.satisfiesDependency(termspresent)) &&
                   (max.dependency == 0 || max.satisfiesDependency(termspresent))) {
                  int span = max.current + max.span - min.current;
                  if (min.peek() < max.current || this.span < span) {
                     break;
                  }
                  for (long comb = (1l << countit) - 1; comb >= 0; comb--) {
                     long midid = maxid;
                     long p = 1;
                     for (int pos = 0; p <= comb; p <<= 1, pos++) {
                        if ((comb & p) != 0) {
                           midid |= independentterms[pos].bitsequence;
                        }
                     }
                     addOccurrence(midid, min.current, span);
                  }
                  int possible = 0;
                  int covered = 0;
                  for (int i = 0; i < countdt; i++) {
                     if (dependentterms[i].satisfiesDependency(termspresent) &&
                         (covered & dependentterms[i].bitsequence) == 0) {
                        possibledt[possible++] = dependentterms[i];
                        covered |= dependentterms[i].alldependency;
                     }
                  }
                  
                  for (long comb = (1l << possible) - 1; comb > 0; comb--) {
                     long midid = maxid;
                     for (int p = 1, pos = 0; p <= comb; p <<= 1, pos++) {
                        if ((comb & p) != 0) {
                           midid |= possibledt[pos].alldependency;
                        }
                     }
                     addOccurrence(midid, min.current, span);
                     SKIPIT:
                     for (long comb2 = (1l << countit) - 1; comb2 > 0; comb2--) {
                        long midid2 = midid;
                        for (int p = 1, pos = 0; p <= comb2; p <<= 1, pos++) {
                           if ((comb2 & p) != 0) {
                              if ((midid2 & independentterms[pos].bitsequence) != 0)
                                 continue SKIPIT;
                              midid2 |= independentterms[pos].bitsequence;
                           }
                        }
                        addOccurrence(midid2, min.current, span);
                     }
                  }
               }
               if (max.dependency == 0)
                  independentterms[countit++] = max;
               else
                  dependentterms[countdt++] = max;
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
      }  else {
         clearTermScores();
      }
   }

   @Override
   public void prepareRetrieval() {

      // construct the phrases contained in the proximity model. By default these
      // are all possible term combinations of two or more terms. Two variants
      // override this to allow stop words in the constructed phrases.
      termpositions = new ProximityStopwordsSet(containedfeatures);
      phrases = constructPhrases();
      for (GraphNode f : containedfeatures) {
         // weight that balances the proximity model with respect to the independent term model
         f.featurevalues.queryweight = proximityModelWeight();
         f.featurevalues.documentprior = 0;
         if (f instanceof Term && ((Term) f).isstopword) {
            countdt++;
         } else {
            countit++;
         }
      }
      dependentterms = new ProximityTerm[countdt];
      independentterms = new ProximityTerm[countit];
      possibledt = new ProximityTerm[countdt];
      middlepos = new ProximityTerm[containedfeatures.size() - 1];
   }

   @Override
   public double proximityModelWeight() {
      int count = 0;
      for (GraphNode g : containedfeatures) {
         if (g instanceof Term && !((Term) g).isstopword) {
            count++;
         }
      }
      return 1.0 / Math.pow(count, gamma);
   }
   
   @Override
   public void setTDFDependencies() {
      ArrayList<TermDocumentFeature> list = new ArrayList<TermDocumentFeature>();
      for (GraphNode g : containedfeatures) {
         list.addAll(g.getRequiredTDF());
      }
      for (ProximityTerm t : termpositions.tpi) {
         if (t.dependency == 0) { // independent term, can be scored with any other independent term
            TermDocumentFeature f = containedfeatures.get(t.sequence).getRequiredTDF().get(0);
            for (ProximityTerm t2 : termpositions.tpi) {
               if (t != t2 && t2.dependency == 0) {
                  TermDocumentFeature s = containedfeatures.get(t.sequence).getRequiredTDF().get(0);
                  f.setDependencies(new TermDocumentFeature[]{s});
               }
            }
         }
      }
      for (ProximityTerm t : termpositions.tpi) {
         if (t.dependency == 0) { // dependent term, can only be scored with dependent terms
            TermDocumentFeature d[] = new TermDocumentFeature[ MathTools.numberOfSetBits(t.dependency) ];
            int counttdf = 0;
            long b = 1;
            for (int j = 0; b <= t.dependency; j++, b <<= 1) {
               if ((t.dependency & b) != 0) {
                  d[counttdf++] = containedfeatures.get(j).getRequiredTDF().get(0);
               }
            }
            containedfeatures.get(t.sequence).getRequiredTDF().get(0).setDependencies(d);
         }
      }
   }
}
