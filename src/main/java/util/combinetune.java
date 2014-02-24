package util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import io.github.repir.Repository.ModelParameters;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.ModelParameters.Record;
import io.github.repir.tools.Lib.ArgsParser;
import io.github.repir.tools.Lib.HDTools;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.StrTools;

public abstract class combinetune {

   public static Log log = new Log(combinetune.class);
   Repository repository;
   ModelParameters modelparameters;
   String settings[];
   String sets[];
   
   public combinetune(Repository repo) {
      repository = repo;
      modelparameters = (ModelParameters) repository.getFeature("ModelParameters");
      sets = StrTools.split(repository.getConfigurationString("testset.crossevaluate"), ",");
      this.settings = repository.getConfigurationSubStrings("settings");
   }
   
   public static void main(String[] args) {
      Repository repository = new Repository( HDTools.readConfig(args, "{settings}"));
      
      combinetune tune = create( repository );
      
      TreeMap<String, Double> result = tune.listStringKey();
      for (Map.Entry<String, Double> e : result.entrySet()) {
         log.info("%f %s", e.getValue(), e.getKey());
      }
   }
   
   public static combinetune create(Repository repo) {
      String cross = repo.getConfigurationString("testset.crossevaluate");
      combinetune tune = null;
      if (cross == null || cross.isEmpty()) {
         log.exit("Please set testset.crossevaluate for this testset");
      } else if (cross.equalsIgnoreCase("fold")) {
         tune = new combinetunefold( repo );
      } else {
         tune = new combinetunecross( repo );
      }
      return tune;
   }
   
   public abstract HashMap<TreeMap<String, String>, Double> list();
   
   public TreeMap<String, Double> listStringKey ()  {
      HashMap<TreeMap<String, String>, Double> list = list();
      TreeMap<String, Double> result = new TreeMap<String, Double>();
      for (Map.Entry<TreeMap<String, String>, Double> e : list.entrySet()) {
         result.put(e.getKey().toString(), e.getValue());
      }
      return result;
   }

}
