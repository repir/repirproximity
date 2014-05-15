//package io.github.repir.apps.TermDistance;
//
//import io.github.repir.tools.Content.Datafile;
//import io.github.repir.tools.DataTypes.Tuple2;
//import io.github.repir.tools.DataTypes.Tuple2M;
//import io.github.repir.tools.Lib.Log;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;
//
//public class AnaDist3 {
//
//   public static Log log = new Log(AnaDist3.class);
//
//   public static void main(String[] args) throws Exception {
//      int rel_small = 0, nrel_small = 0, rel_large = 0, nrel_large = 0;
//      Tuple2M<String, Integer> pair = new Tuple2M<String, Integer>();
//      ArrayList<Tuple2M<String, Integer>> phrases = new ArrayList<Tuple2M<String, Integer>>();
//      for (String file : args) {
//         log.info("file %s", file);
//         HashMap<Tuple2M<String, Integer>, a> phrasestats = new HashMap<Tuple2M<String, Integer>, a>();
//
//         DistanceFile df = new DistanceFile(new Datafile(file));
//         df.openRead();
//         while (df.next()) {
//            pair.value2 = df.topic.value;
//            pair.value1 = df.phrase.value;
//            a stats = phrasestats.get(pair);
//            if (stats == null) {
//               stats = new a();
//               Tuple2M<String, Integer> newpair = new Tuple2M<String, Integer>( df.phrase.value, df.topic.value );
//               phrasestats.put(newpair, stats);
//            }
//            int terms = io.github.repir.tools.Lib.StrTools.countIndexOf(df.phrase.value, ' ') + 1;
//            //if (terms == 2) {
//               int span = df.span.value - terms;
//               int rel = df.relevant.value;
//               if (rel == 1) {
//                  if (span >= 200 && span < 500) {
//                     stats.rel_small++;
//                  } else if (span >= 500 && span < 1000) {
//                     stats.rel_large++;
//                  }
//               } else {
//                  if (span >= 200 && span < 500) {
//                     stats.nrel_small++;
//                  } else if (span >= 500 && span <= 1000) {
//                     stats.nrel_large++;
//                  }
//               }
//            //}
//         }
//         df.closeRead();
//         for (Map.Entry<Tuple2M<String, Integer>, a> entry : phrasestats.entrySet()) {
//            a stats = entry.getValue();
//            log.info("phrase %s rel small %d nrel small %d rel large %d nrel large %d", entry.getKey().value1, stats.rel_small, stats.nrel_small,stats.rel_large, stats.nrel_large);
//            if (stats.rel_small + stats.nrel_small > 0 && stats.rel_large + stats.nrel_large > 0) {
//               double prels = stats.rel_small / (double) (stats.rel_small + stats.nrel_small);
//               double prell = stats.rel_large / (double) (stats.rel_large + stats.nrel_large);
//               if (prell > prels) {
//                  phrases.add( entry.getKey() );
//               } else {
//                  rel_small += stats.rel_small;
//                  nrel_small += stats.nrel_small;
//                  rel_large += stats.rel_large;
//                  nrel_large += stats.nrel_large;
//               }
//            }
//         }
//      }
//      log.info("rel small %d nrel small %d rel large %d nrel large %d", rel_small, nrel_small, rel_large, nrel_large);
//      PhraseFile out = new PhraseFile(new Datafile("phrases"));
//      out.openWrite();
//      for (Tuple2M<String, Integer> p : phrases) {
//         out.topic.write(p.value2);
//         out.phrase.write(p.value1);
//      }
//      out.closeWrite();
//   }
//
//   static class a {
//      int rel_small;
//      int rel_large;
//      int nrel_small;
//      int nrel_large;
//   }
//}
