package io.github.repir.apps.TermDistance;

import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Excel.ExcelDoc;
import io.github.repir.tools.Excel.ExcelSheet;
import io.github.repir.tools.Lib.Log;
import java.io.EOFException;
import java.util.HashMap;
import java.util.Map;

public class AnaDist2 { 
 
   public static Log log = new Log(AnaDist2.class);

   public static void main(String[] args) throws Exception {
      DistanceFile df = new DistanceFile(new Datafile(args[0]));
      String phrasef = io.github.repir.tools.Lib.ArrayTools.toString(" ", args, 1, args.length);
      df.openRead();
      HashMap<Integer, Double> relevant = new HashMap<Integer, Double>();
      HashMap<Integer, Double> notrelevant = new HashMap<Integer, Double>();
      ExcelDoc doc = new ExcelDoc("dist_" + phrasef.replaceAll(" ", "_") + ".xlsx");
      ExcelSheet sheet = doc.getSheet("sheet");
      int row = 0;
      int count1 = 0, count2 = 0;
      while (df.next()) {
         String phrase = df.phrase.value;
         int terms = io.github.repir.tools.Lib.StrTools.countIndexOf(phrase, ' ') + 1;
         int span = df.span.value;
         int rel = df.relevant.value;
         if (phrase.equals(phrasef)) {
            if (rel == 1) {
               add(relevant, span - terms);
            } else {
               add(notrelevant, span - terms);
            }
         }
      }
      df.closeRead();
      //log.info("count1 %d count2 %d", count1, count2);
      relevant = smooth(relevant);
      notrelevant = smooth(notrelevant);
      for (int i = 0; i < 10000; i++) {
         //log.info("i %d", i);
         sheet.createCell(0, i + 1).set( i);
         sheet.createCell(1, i + 1).set( (double) ((relevant.get(i) == null) ? 0 : relevant.get(i)));
         sheet.createCell(2, i + 1).set( (double) ((notrelevant.get(i) == null) ? 0 : notrelevant.get(i)));
      }
      doc.write();
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
      double p[] = new double[20000];
      for (Map.Entry<Integer, Double> entry : m.entrySet()) {
         if (entry.getKey() < 10000) {
            double h = 0.5 * (entry.getKey() + 1);
            double sump = 0;
            for (int i = 0; i < 20000; i++) {
               p[i] = (1.0 / (h * Math.sqrt(2 * Math.PI))) * Math.pow(Math.E, -Math.pow(i, 2) / (2 * Math.pow(h, 2)));
               sump += p[i];
            }
            sump = 2 * (sump - p[0] / 2);
            for (int i = 0; i < 20000; i++) {
               p[i] /= sump;
            }
            for (int i = 0; i < 10000; i++) {          
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
