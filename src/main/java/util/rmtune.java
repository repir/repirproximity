package util;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import io.github.repir.Repository.ModelParameters;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.ModelParameters.Record;
import io.github.htools.lib.ArgsParser;
import io.github.htools.lib.Log;
import io.github.htools.lib.PrintTools;

public class rmtune {

   public static Log log = new Log(rmtune.class);
   
   public static void main(String[] args) {
      ArgsParser parsedargs = new ArgsParser(args, "configfile {settings}");
      Repository repository = new Repository(parsedargs.get("configfile"));
      
      ModelParameters f = ModelParameters.get(repository, repository.configurationName());
      remove(f, parsedargs.getStrings("settings"));
   }
   
   public static void remove(ModelParameters f, String settings[]) {
      f.openRead();
      ArrayList<Record> sorted = new ArrayList<Record>();
      NOTOK:
      for (Record r : f.getKeys()) {
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
         sorted.add(r);
      }
      f.remove(sorted);
   }
}
