package io.github.repir.apps.Retrieve;
import io.github.repir.Repository.Tools.Parameter;
import java.math.BigDecimal; 
import java.util.Collection;
import io.github.repir.Repository.Repository;

/**
 *
 * @author Jeroen Vuurens
 */
public class SolutionGrid extends Solution {

  public SolutionGrid(Repository repository, final Tuner outer) {
      super( repository, outer );
  }
   
   public point findMax() {
      point max = null;
      for (point p : (Collection<point>)metric.values()) {
         if (max == null || max.getValue() < p.getValue()) {
            max = p;
         }
      }
      return max;
   }

   public void generateFirstPoints() {
      point p = new point();
      generateMorePoints(0, p);
   }

   public void generateMorePoints(int param, point p) {
      if (param == tuner.parameters.size()) {
         if (!metric.containsKey(p)) {
            metric.put(p, p);
         }
      } else {
         Parameter currentparam = (Parameter)parameters.get(param);
         for (BigDecimal v : currentparam.getPoints()) {
            point newpoint = new point(p);
            newpoint.v[param] = v;
            generateMorePoints(param + 1, newpoint);
         }
      }
   }
}
