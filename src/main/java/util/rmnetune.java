package util;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import io.github.repir.Repository.ModelParameters;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.ModelParameters.Record;
import io.github.repir.tools.Lib.ArgsParser;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.PrintTools;

public class rmnetune {

   public static Log log = new Log(rmnetune.class);
   
   public static void main(String[] args) {
      ArgsParser parsedargs = new ArgsParser(args, "configfile present notpresent");
      Repository repository = new Repository(parsedargs.get("configfile"));
      
      ModelParameters f = (ModelParameters) repository.getFeature("ModelParameters");
      remove(f, parsedargs.get("present"), parsedargs.get("notpresent"));
   }
   
   public static void remove(ModelParameters f, String present, String notpresent) {
      f.openRead();
      ArrayList<Record> sorted = new ArrayList<Record>();
      KEEP:
      for (Record r : f.getKeys()) {
         boolean ok = false;
         String map = PrintTools.sprintf("%.6f", r.map);
         OK:
            for (Map.Entry<String, String> e : r.parameters.entrySet()) {
               if (e.getKey().equals(present) || e.getValue().equals(present)) {
                  ok = true;
                  break;
               }
            }
            if (!ok)
               continue KEEP;
            for (Map.Entry<String, String> e : r.parameters.entrySet()) {
               if (e.getKey().equals(notpresent) || e.getValue().equals(notpresent)) {
                  continue KEEP;
               }
            }
            log.info("remove %s", r);
         sorted.add(r);
      }
      f.remove(sorted);
   }
}
