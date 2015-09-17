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

public class retaintune {

   public static Log log = new Log(retaintune.class);
   
   public static void main(String[] args) {
      ArgsParser parsedargs = new ArgsParser(args, "configfile qualifier retainpar retainvalue");
      Repository repository = new Repository(parsedargs.get("configfile"));
      
      ModelParameters f = ModelParameters.get(repository, repository.configurationName());
      remove(f, parsedargs.get("qualifier"), parsedargs.get("retainpar"), parsedargs.get("retainvalue"));
   }
   
   public static void remove(ModelParameters f, String qualifier, String retainpar, String retainvalue) {
      f.openRead();
      ArrayList<Record> keep = new ArrayList<Record>();
      NOTOK:
      for (Record r : f.getKeys()) {
         boolean remove = false;
         boolean qualifies = false;
            for (Map.Entry<String, String> e : r.parameters.entrySet()) {
               if (e.getKey().equals(retainpar)) {
                  if (!retainvalue.equals(e.getValue()))
                     remove=true;
               }
               if (e.getValue().equals(qualifier))
                  qualifies = true;
            }
         if (!qualifies)
            keep.add(r);
         else if (!remove) {
            r.parameters.remove(retainpar);
            keep.add(r);
         }
      }
      f.openWriteNew();
      for (Record r : keep)
         f.write(r);
      f.closeWrite();
   }
}
