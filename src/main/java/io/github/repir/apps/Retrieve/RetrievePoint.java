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
public class RetrievePoint extends IRHDJobThread {
  public static Log log = new Log( RetrievePoint.class ); 
  Solution solution;
  point point;

  public RetrievePoint( Solution solution, point point, Configuration conf ) {
     super(conf, solution.getQueries());
     this.solution = solution;
     this.point = point;
  }

  public point getPoint() {
     return point;
  }
  
   public void jobWasSuccesful(ArrayList<Query> queries) {
      log.info("succes %s", point);
      ResultSet resultset = new ResultSet(new QueryMetricAP(), solution.testset, queries);
      resultset.calulateMeasure();
      point.getRecord().map = resultset.result[1].avg;
      solution.addComputedPoint(point.getRecord());
   }

   public void JobFailed() {
      log.info("fail %s", point);
      solution.removeFailedRetriever(point);
   }
}
