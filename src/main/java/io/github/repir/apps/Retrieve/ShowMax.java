package io.github.repir.apps.Retrieve;
import io.github.repir.Repository.Repository;
import io.github.repir.apps.Retrieve.Solution.point;
import io.github.repir.tools.Lib.HDTools;
import io.github.repir.tools.Lib.Log; 

/**
 *
 * @author Jeroen Vuurens
 */
public class ShowMax {
  public static Log log = new Log( ShowMax.class ); 

   public static void main(String[] args) {
      Repository repository = new Repository(HDTools.readConfig(args[0]));
      String cross = repository.getConfigurationString("testset.crossevaluate");
      String type = repository.getConfigurationString("testset.crossevaluatetype", "grid");
      point max = null;
      if (cross == null || cross.isEmpty()) {
         log.exit("Please set testset.crossevaluate for this testset");
      }else if (type.equalsIgnoreCase("bisect")) {
         if (cross.equalsIgnoreCase("fold")) {
            max = new TunerFold5(repository).getMaxSolution(true);
         } else {
            max = new TunerCross5(repository).getMaxSolution(true);
         }
      } else if (type.equalsIgnoreCase("grid")) {
         if (cross.equalsIgnoreCase("fold")) {
            max = new TunerFoldGrid(repository).getMaxSolution(true);
         } else {
            max = new TunerCrossGrid(repository).getMaxSolution(true);
         }
      }
      log.printf("max %s", max.toString());
   }

}
