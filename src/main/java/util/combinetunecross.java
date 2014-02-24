package util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import io.github.repir.Repository.ModelParameters;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.ModelParameters.Record;
import io.github.repir.tools.Lib.ArgsParser;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.StrTools;

public class combinetunecross extends combinetune {

   public static Log log = new Log(combinetunecross.class);
   
   public combinetunecross(Repository repo) {
      super( repo );
   }
   
   public HashMap<TreeMap<String, String>, Double> list() {
      HashMap<TreeMap<String, String>, Double> combos[] = new HashMap[ sets.length ];
      for (int i = 0; i < sets.length; i++) {
         combos[i] = list( sets[i]);
         log.info("%s %s", sets[i], combos[i].toString());
      }
      HashMap<TreeMap<String, String>, Double> result = new HashMap<TreeMap<String, String>, Double>();
      TOOBAD:
      for (TreeMap<String, String> key : combos[0].keySet()) {
         double total = 0;
         for (HashMap<TreeMap<String, String>, Double> c : combos) {
            Double r = c.get(key);
            if (r != null) {
               total += r;
            } else {
               continue TOOBAD;
            }
         }
         total /= sets.length;
         result.put(key, total);
      }
      return result;
   }
   
   public HashMap<TreeMap<String, String>, Double> list(String set) {
      modelparameters.openRead();
      HashMap<TreeMap<String, String>, Double> sorted = new HashMap<TreeMap<String, String>, Double>();
      DONTSTORE:
      for (Record r : modelparameters.getKeys()) {
         log.info("%s", r.parameters);
         LOOP:
         for (String s : settings) {
            Iterator<Map.Entry<String, String>> e = r.parameters.entrySet().iterator();
            while (e.hasNext()) {
               Map.Entry<String, String> next = e.next();
               if (next.getKey().equals(s) || next.getValue().equals(s)) {
                  e.remove();
                  continue LOOP;
               }
            }
            continue DONTSTORE;
         }
         LOOP2:
         do {
            Iterator<Map.Entry<String, String>> e = r.parameters.entrySet().iterator();
            while (e.hasNext()) {
               Map.Entry<String, String> next = e.next();
               if (next.getKey().contains(set) || next.getValue().contains(set)) {
                  e.remove();
                  continue LOOP2;
               }
            }
            continue DONTSTORE;
         } while(false);
         r.parameters = new TreeMap<String, String>( r.parameters );
         sorted.put(r.parameters, r.map);
      }
      modelparameters.closeRead();
      return sorted;
   }
}
