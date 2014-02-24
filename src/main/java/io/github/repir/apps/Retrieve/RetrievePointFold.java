package io.github.repir.apps.Retrieve;
import java.util.ArrayList;
import io.github.repir.tools.DataTypes.Configuration;
import io.github.repir.Repository.ModelParameters;
import io.github.repir.Retriever.Query;
import io.github.repir.RetrieverMR.IRHDJobThread;
import io.github.repir.TestSet.QueryMetricAP;
import io.github.repir.TestSet.ResultSet;
import io.github.repir.apps.Retrieve.Solution.point;
import io.github.repir.tools.Lib.Log; 

/**
 *
 * @author Jeroen Vuurens
 */
public class RetrievePointFold extends RetrievePoint {
  public static Log log = new Log( RetrievePointFold.class ); 

  public RetrievePointFold( Solution solution, point p, Configuration conf ) {
     super(solution, p, conf);
  }
  
  @Override
   public void jobWasSuccesful(ArrayList<Query> queries) {
      log.info("succes %s", point);
      ResultSet resultset = new ResultSet(new QueryMetricAP(), solution.testset, queries);
      resultset.calulateMeasure();
      for (Solution s : solution.tuner.sol) {
         SolutionFold sfold = (SolutionFold) s;
         ModelParameters.Record record = sfold.storePoints(point, resultset);
         solution.addComputedPoint(record);
      }
   }
}
