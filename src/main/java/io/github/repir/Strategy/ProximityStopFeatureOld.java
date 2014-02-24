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
//public class ProximityStopFeatureOld extends ProximityFeatureOld {
//
//   public static Log log = new Log(ProximityStopFeatureOld.class);
//
//   public ProximityStopFeatureOld(GraphRoot im, ArrayList<GraphNode> list) {
//      super(im, list);
//   }
//
//   @Override
//   public void announce(ANNOUNCEKEY type, GraphNode node) {
//      if (type != ANNOUNCEKEY.STOPWORD) {
//         super.announce(type, node);
//      }
//   }
//   
//   /**
//    * inly use combinations that contain at least one non-stopword
//    * @return 
//    */
//   protected HashMap<Integer, phrase> constructPhrases() {
//      HashMap<Integer, phrase> phrases = new HashMap<Integer, phrase>();
//      int maxid = (1 << containedfeatures.size());
//      ID: for (int id = 2; id < maxid; id++) {
//         int phrase_number_of_terms = vu.nl.iref.tools.Lib.MathTools.numberOfSetBits(id);
//         if (phrase_number_of_terms > 1) {
//            boolean is_valid = false;
//            for (int i = 0; !is_valid && i < containedfeatures.size(); i++) {
//               int bit = 1 << i;
//               if ((id & bit) > 0) {
//                  if (containedfeatures.get(i) instanceof Term && !((Term) containedfeatures.get(i)).isstopword) {
//                     is_valid = true;
//                  }
//               }
//            }
//            if (is_valid)
//               phrases.put(id, new phrase(id));
//         }
//      }
//      return phrases;
//   }
//}
