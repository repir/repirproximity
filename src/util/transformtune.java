package util;

import java.util.Map;
import io.github.repir.Repository.ModelParameters;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.ModelParameters.Record;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Lib.Log;
import java.util.HashMap;

public class transformtune {

   public static Log log = new Log(transformtune.class);

   public static void main(String[] args) {
      Repository repository = new Repository(args);
      ModelParameters f;
      f = (ModelParameters) repository.getFeature(ModelParameters.class, repository.configurationName());
      HashMap<Record,Record> baselist = f.load();
      baselist = transform(baselist);
      Datafile df = f.getStoredFeatureFile();
      df.delete();
      f.setBufferSize(1000000);
      f.openWrite();
      for (Record r : baselist.values()) {
         f.write(r);
      }
      f.closeWrite();
   }
   
   public static HashMap<Record, Record> transform(HashMap<Record, Record> map) {
      HashMap<Record, Record> nm = new HashMap<Record, Record>();
      for (Record r : map.values()) {
         for (Map.Entry<String,String> p : r.parameters.entrySet()) {
            if (p.getKey().startsWith("repir.")) {
               r.parameters.put(p.getKey().replaceFirst("repir", "rr"), p.getValue());
               r.parameters.remove(p.getKey());
               break;
            }
         }
         nm.put(r, r);
      }
      return nm;
   }
}
