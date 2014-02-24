package util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.ModelParameters.Record;
import io.github.repir.tools.Lib.Log;

public class combinetunefold extends combinetune {

   public static Log log = new Log(combinetunefold.class);
   int MAXFOLDS = 10;
   
   public combinetunefold(Repository repo) {
      super( repo );
   }

   public HashMap<TreeMap<String, String>, Double> list() {
      int fold = repository.getConfigurationInt("testset.fold", -1);
      if (fold < 0)
         log.fatal("Set testset.fold");
      modelparameters.openRead();
      HashMap<TreeMap<String, String>, Double> sorted = new HashMap<TreeMap<String, String>, Double>();
      DONTSTORE:
      for (Record r : modelparameters.getKeys()) {
         int ifold = Integer.parseInt(r.parameters.get("fold"));
         if (ifold != fold) {
            r.parameters.remove("fold");
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
            r.parameters = new TreeMap<String, String>(r.parameters);
            //String key = r.parameters.toString();
            Double map = sorted.get(r.parameters);
            if (map == null) {
               sorted.put(r.parameters, r.map / (MAXFOLDS-1));
            } else {
               sorted.put(r.parameters, map + r.map / (MAXFOLDS-1));
            }
         }
      }
      modelparameters.closeRead();
      return sorted;
   }
}
