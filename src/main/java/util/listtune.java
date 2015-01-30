package util;

import io.github.repir.Repository.ModelParameters;
import io.github.repir.Repository.ModelParameters.Record;
import io.github.repir.Repository.Repository;
import io.github.repir.tools.collection.ArrayMap;
import io.github.repir.tools.collection.ArrayMap.Entry;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.lib.PrintTools;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class listtune {

   public static Log log = new Log(listtune.class);
   
   public static void main(String[] args) {
      Repository repository = new Repository(args, "{settings}");
      ModelParameters f = ModelParameters.get(repository, repository.configurationName());
      list(f.load(), repository.configuredStrings("settings"));
   }
   
   public static void list(HashMap<Record, Record> list, String settings[]) {
      ArrayMap<Double, String> sorted = new ArrayMap();
      NOTOK:
      for (Record r : list.values()) {
         String map = PrintTools.sprintf("%.6f", r.map);
         OK:
         for (String s : settings) {
            if (map.equalsIgnoreCase(s)) {
               continue OK;
            }
            for (Map.Entry<String, String> e : r.parameters.entrySet()) {
               if (e.getKey().equals(s) || e.getValue().equals(s)) {
                  continue OK;
               }
            }
            continue NOTOK;
         }
         sorted.add(r.map, r.parameters.toString());
      }
      for (Map.Entry<Double,String> e : sorted.descending()) {
         log.printf("%f list %s", e.getKey(), e.getValue());
      }
   }
}
