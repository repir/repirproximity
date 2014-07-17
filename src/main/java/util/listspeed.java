package util;

import io.github.repir.Repository.ModelSpeed;
import io.github.repir.Repository.ModelSpeed.Record;
import io.github.repir.Repository.Repository;
import io.github.repir.tools.DataTypes.TreeMapComparable;
import io.github.repir.tools.DataTypes.TreeMapComparable.TYPE;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.PrintTools;
import java.util.Map;

public class listspeed {

   public static Log log = new Log(listspeed.class);
   
   public static void main(String[] args) {
      Repository repository = new Repository(args, "{settings}");
      ModelSpeed f = (ModelSpeed) repository.getFeature(ModelSpeed.class);
      list(f, repository.configuredStrings("settings"));
   }
   
   public static void list(ModelSpeed f, String settings[]) {
      f.openRead();
      TreeMapComparable<Integer, String> sorted = new TreeMapComparable<Integer, String>(TYPE.DUPLICATESASCENDING);
      NOTOK:
      for (Record r : f.getKeys()) {
         String qid = PrintTools.sprintf("%.6f", r.time);
         if (settings != null) {
         OK:
         for (String s : settings) {
            if (s.equals(Integer.toString(r.query))) {
               continue OK;
            }
            if (s.equals(r.strategy))
                  continue OK;
            }
            continue NOTOK;
         
         }
         sorted.put(r.query, r.time + " " + r.strategy);
      }
      for (Map.Entry<Integer,String> e : sorted.entrySet()) {
         log.printf("%03d %s", e.getKey(), e.getValue());
      }
   }
}
