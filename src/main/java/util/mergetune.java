package util;

import java.util.Map;
import io.github.repir.Repository.ModelParameters;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.ModelParameters.Record;
import io.github.repir.tools.Lib.Log;
import java.util.HashMap;

public class mergetune {

   public static Log log = new Log(mergetune.class);

   public static void main(String[] args) {
      Repository repository = new Repository(args, "path {settings}");
      ModelParameters f;
      f = ModelParameters.get(repository, repository.configurationName());
      HashMap<Record,Record> baselist = f.load();
      baselist = transform(baselist);
      listtune.list(baselist, repository.configuredStrings("settings"));
   }
   
   public static HashMap<Record, Record> transform(HashMap<Record, Record> map) {
      HashMap<Record, Record> nm = new HashMap<Record, Record>();
      for (Record r : map.values()) {
         for (Map.Entry<String,String> p : r.parameters.entrySet()) {
            if (p.getKey().startsWith("iref.")) {
               r.parameters.remove(p.getKey());
               r.parameters.put(p.getKey().replaceFirst("iref", "rr"), p.getValue());
               break;
            }
         }
         nm.put(r, r);
      }
      return nm;
   }

   public static void merge(HashMap<Record, Record> base, HashMap<Record, Record> add, String settings[]) {
      NOTOK:
      for (Record r : add.values()) {
         if (base.containsKey(r)) {
            continue NOTOK;
         }
         if (settings != null) {
            OK:
            for (String s : settings) {
               for (Map.Entry<String, String> e : r.parameters.entrySet()) {
                  if (e.getKey().equals(s) || e.getValue().equals(s)) {
                     continue OK;
                  }
               }
               continue NOTOK;
            }
         }
         log.info("add %s", r.toString());
         base.put(r, r);
      }
   }
}
