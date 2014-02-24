package io.github.repir.apps.TermDistance;

import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.DataTypes.Tuple2M;
import io.github.repir.tools.Excel.ExcelDoc;
import io.github.repir.tools.Excel.ExcelSheet;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Strategy.PhraseFile;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class AnaDist {

   public static Log log = new Log(AnaDist.class);

   public static void main(String[] args) throws Exception {
      int spanf = Integer.parseInt(args[0]);
      HashMap<Integer, Double> relevant = new HashMap<Integer, Double>();
      HashMap<Integer, Double> notrelevant = new HashMap<Integer, Double>();
      HashSet<Tuple2M<Integer, String>> skipphrases = readSkipPhrases();
      ExcelDoc doc = new ExcelDoc("dist.xlsx");
      ExcelSheet sheet = doc.getSheet("sheet");
      int row = 0;
      int count1 = 0, count2 = 0;
      Tuple2M<Integer, String> topicphrase = new Tuple2M<Integer, String>();;
      for (String file : io.github.repir.tools.Lib.ArrayTools.subArray(args, 1)) {
         log.info("file %s", file);
         DistanceFile df = new DistanceFile(new Datafile(file));
         df.openRead();
         while (df.next()) {
            topicphrase.value1 = df.topic.value;
            topicphrase.value2 = df.phrase.value;
            //if (!skipphrases.contains(topicphrase)) {
               int terms = io.github.repir.tools.Lib.StrTools.countIndexOf(topicphrase.value2, ' ') + 1;
               int span = df.span.value;
               int rel = df.relevant.value;
               if (spanf == 0 || terms == spanf) {
                  if (rel == 1) {
                     add(relevant, span - terms);
                  } else {
                     add(notrelevant, span - terms);
                  }
               }
            //}
         }
         df.closeRead();
      }
      log.info("count1 %d count2 %d", count1, count2);
      relevant = smooth(relevant);
      notrelevant = smooth(notrelevant);
      for (int i = 0; i < 1000; i++) {
         //log.info("i %d", i);
         sheet.createCell(0, i + 1).set( i);
         sheet.createCell(1, i + 1).set( (double) ((relevant.get(i) == null) ? 0 : relevant.get(i)));
         sheet.createCell(2, i + 1).set( (double) ((notrelevant.get(i) == null) ? 0 : notrelevant.get(i)));
      }
      doc.write();
   }

   public static HashSet<Tuple2M<Integer, String>> readSkipPhrases() {
      HashSet<Tuple2M<Integer, String>> phrases = new HashSet<Tuple2M<Integer, String>>();
      Datafile df = new Datafile("phrases");
      if (df.exists()) {
         PhraseFile pf = new PhraseFile(df);
         pf.openRead();
         while (pf.next()) {
            phrases.add(new Tuple2M<Integer, String>(pf.topic.value, pf.phrase.value));
         }
         pf.closeRead();
      }
      return phrases;
   }

   public static void add(Map<Integer, Double> dest, int separation) {
      Double v = dest.get(separation);
      if (v != null) {
         dest.put(separation, v + 1);
      } else {
         dest.put(separation, 1.0);
      }
   }

   public static HashMap<Integer, Double> smooth(Map<Integer, Double> m) {
      HashMap<Integer, Double> d = new HashMap<Integer, Double>();
      double p[] = new double[2000];
      for (Map.Entry<Integer, Double> entry : m.entrySet()) {
         if (entry.getKey() < 1000) {
            double h = 0.5 * (entry.getKey() + 1);
            double sump = 0;
            for (int i = 0; i < 2000; i++) {
               p[i] = (1.0 / (h * Math.sqrt(2 * Math.PI))) * Math.pow(Math.E, -Math.pow(i, 2) / (2 * Math.pow(h, 2)));
               sump += p[i];
            }
            sump = 2 * (sump - p[0] / 2);
            for (int i = 0; i < 2000; i++) {
               p[i] /= sump;
            }
            for (int i = 0; i < 1000; i++) {
               Double e = d.get(i);
               if (e == null) {
                  e = 0.0;
               }
               e += entry.getValue() * p[ Math.abs(i - entry.getKey())];
               d.put(i, e);
            }
         }
      }
      return d;
   }
}
