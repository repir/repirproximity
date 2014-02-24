package io.github.repir.apps.Retrieve;

import io.github.repir.tools.Lib.HDTools;
import io.github.repir.Repository.Repository;
import io.github.repir.tools.Lib.ArgsParser;
import io.github.repir.tools.Lib.ArrayTools;
import io.github.repir.tools.Lib.Log;

/**
 * Retrieve all topics from the TestSet, and store in an output file. arguments:
 * <configfile> <outputfileextension>
 *
 * @author jeroen
 */
public class TestSetTune {

   public static Log log = new Log(TestSetTune.class);

   public static void main(String[] args) throws Exception {
      Repository repository = new Repository(HDTools.readConfig(args, ""));
      String cross = repository.getConfigurationString("testset.crossevaluate");
      String type = repository.getConfigurationString("testset.crossevaluatetype", "grid");
      Tuner tuner = null;
      if (cross == null || cross.isEmpty()) {
         log.exit("Please set testset.crossevaluate for this testset");
      } else if (type.equalsIgnoreCase("bisect")) {
         if (cross.equalsIgnoreCase("fold")) {
            tuner = new TunerFold5(repository);
         } else {
            tuner = new TunerCross5(repository);
         }
      } else if (type.equalsIgnoreCase("grid")) {
         if (cross.equalsIgnoreCase("fold")) {
            tuner = new TunerFoldGrid(repository);
         } else {
            tuner = new TunerCrossGrid(repository);
         }
      }
      if (tuner == null) {
         log.exit("Please set testset.crossevaluatetype to a correct type");
      }
      tuner.computePoints();
   }
   
}
