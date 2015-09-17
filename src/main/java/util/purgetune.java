package util;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import io.github.repir.Repository.ModelParameters;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.ModelParameters.Record;
import io.github.repir.Retriever.Tuner.Parameter;
import io.github.repir.Retriever.Query;
import io.github.repir.Retriever.Tuner.Retriever;
import io.github.htools.lib.ArgsParser;
import io.github.htools.lib.Log;
import io.github.htools.lib.PrintTools;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class purgetune {

   public static Log log = new Log(purgetune.class);
   
   public static void main(String[] args) {
      ArgsParser parsedargs = new ArgsParser(args, "configfile {settings}");
      Repository repository = new Repository(parsedargs.get("configfile"));
      Retriever retriever = new Retriever(repository);
      
      ModelParameters f = ModelParameters.get(repository, repository.configurationName());
      ArrayList<Parameter> parameters = retriever.getParameters();
      ArrayList<String> settings = retriever.generatePoints(parameters);
      settings = retriever.removeKnownSettingsFold(repository, settings);
      HashMap<Record, Record> list = f.load();
      ArrayList<Record> remove = new ArrayList<Record>();
      String[] storedparameters = repository.getStoredFreeParameters();
      for (String setting : settings) {
         repository.addConfiguration(setting);
         for (int i = 0; i < 10; i++) {
            repository.getConf().setInt("fold", i);
            Record newRecord = f.newRecord(storedparameters);
            Record found = list.get(newRecord);
            if (found != null) {
               remove.add(found);
               //log.info("remove %s", found.parameters.toString());
            }
         }
      }
      settings = null;
      list = null;
      f.remove(remove);
   }
}
