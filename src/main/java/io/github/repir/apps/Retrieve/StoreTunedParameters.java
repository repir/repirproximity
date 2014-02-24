package io.github.repir.apps.Retrieve;
import io.github.repir.Repository.Repository;
import io.github.repir.tools.Lib.HDTools;
import io.github.repir.tools.Lib.Log; 

/**
 *
 * @author Jeroen Vuurens
 */
public class StoreTunedParameters {
  public static Log log = new Log( StoreTunedParameters.class ); 

   public static void main(String[] args) {
      Repository repository = new Repository(HDTools.readConfig(args[0]));
      String cross = repository.getConfigurationString("testset.crossevaluate");
      String type = repository.getConfigurationString("testset.crossevaluatetype", "grid");
      if (cross == null || cross.isEmpty()) {
         log.exit("Please set testset.crossevaluate for this testset");
      }else if (type.equalsIgnoreCase("bisect")) {
         if (cross.equalsIgnoreCase("fold")) {
            new TunerFold5(repository).storeSolution(true);
         } else {
            new TunerCross5(repository).storeSolution(true);
         }
      } else if (type.equalsIgnoreCase("grid")) {
         if (cross.equalsIgnoreCase("fold")) {
            new TunerFoldGrid(repository).storeSolution(true);
         } else {
            new TunerCrossGrid(repository).storeSolution(true);
         }
      }
   }

}
