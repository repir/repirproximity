package util;

import java.util.Map;
import io.github.repir.Repository.ModelParameters;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.ModelParameters.Record;
import io.github.repir.tools.DataTypes.TreeMapComparable;
import io.github.repir.tools.DataTypes.TreeMapComparable.TYPE;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.PrintTools;
import java.util.HashMap;

public class listtune {

   public static Log log = new Log(listtune.class);
   
   public static void main(String[] args) {
      Repository repository = new Repository(args, "{settings}");
      ModelParameters f = (ModelParameters) repository.getFeature(ModelParameters.class, repository.configurationName());
      list(f.load(), repository.configuredStrings("settings"));
   }
   
   public static void list(HashMap<Record, Record> list, String settings[]) {
      TreeMapComparable<Double, String> sorted = new TreeMapComparable<Double, String>(TYPE.DUPLICATESDESCENDING);
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
         sorted.put(r.map, r.parameters.toString());
      }
      for (Map.Entry<Double,String> e : sorted.entrySet()) {
         log.printf("%f list %s", e.getKey(), e.getValue());
      }
   }
}
