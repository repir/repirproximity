package io.github.repir.Strategy;

//package vu.nl.iref.Strategy;
//
//import vu.nl.iref.Strategy.GraphNode;
//import vu.nl.iref.Strategy.Term;
//import vu.nl.iref.Strategy.GraphRoot;
//import java.util.ArrayList;
//import java.util.HashMap;
//import vu.nl.iref.tools.Lib.Log;
//
///**
// * This variant to FeaturePhrase
// * <p/>
// * @author jeroen
// */
//public class ProximityStopAdj2FeatureOld extends ProximityStopFeatureOld {
//
//   public static Log log = new Log(ProximityStopAdj2FeatureOld.class);
//   public long stopdependency[];
//   public ArrayList<Integer> removelists[];
//
//   public ProximityStopAdj2FeatureOld(GraphRoot im, ArrayList<GraphNode> list) {
//      super(im, list);
//   }
//
//   /**
//    * valid combinations can only contain stop words that are either linked both
//    * on the left and right by the query beginning/end or a non stop word.
//    *
//    * @return
//    */
//   @Override
//   protected HashMap<Integer, phrase> constructPhrases() {
//      HashMap<Integer, phrase> phrases = new HashMap<Integer, phrase>();
// 
//      int maxid = (1 << containedfeatures.size());
//      stopdependency = new long[containedfeatures.size()];
//      removelists = new ArrayList[containedfeatures.size()];
//      for (int i = 0; i < containedfeatures.size(); i++) {
//         removelists[i] = new ArrayList<Integer>();
//         if (containedfeatures.get(i) instanceof Term && ((Term) containedfeatures.get(i)).isstopword) {
//            long pattern = 0;
//            for (int j = i - 1; j >= 0; j--) {
//               pattern |= 1 << j;
//               if (containedfeatures.get(j) instanceof Term && !((Term) containedfeatures.get(j)).isstopword)
//                  break;
//            }
//            for (int j = i + 1; j < containedfeatures.size(); j++) {
//               pattern |= 1 << j;
//               if (containedfeatures.get(j) instanceof Term && !((Term) containedfeatures.get(j)).isstopword)
//                  break;
//            }
//            stopdependency[i] = pattern;
//         }
//      }
//      for (int i = 0; i < containedfeatures.size(); i++) {
//         for (int j = 0, p = 1; j < containedfeatures.size(); j++, p <<= 1) {
//            if ((stopdependency[i] & p) != 0) {
//               removelists[j].add(i);
//            }
//         }
//      }
//      SKIPPHRASE:
//      for (int id = 2; id < maxid; id++) {
//         int phrase_number_of_terms = vu.nl.iref.tools.Lib.MathTools.numberOfSetBits(id);
//         if (phrase_number_of_terms > 1) {
//            for (int i = 0; i < containedfeatures.size(); i++) {
//               int bit = 1 << i;
//               if ((id & bit) > 0) {
//                  if (containedfeatures.get(i) instanceof Term && ((Term) containedfeatures.get(i)).isstopword) {
//                     if ( (id & stopdependency[i]) != stopdependency[i] ) {
//                        continue SKIPPHRASE;
//                     }
//                  }
//               }
//            }
//            phrases.put(id, new phrase(id));
//         }
//      }
//      for (phrase f : phrases.values()) {
//         log.info("phrase %s", f.toString());
//      }
//      return phrases;
//   }
//      
//   class group extends ArrayList<Term> {
//      boolean stopword;
//      
//      public group( Term g ) {
//         add(g);
//         stopword = g.isstopword;
//      }
//   }
//}
