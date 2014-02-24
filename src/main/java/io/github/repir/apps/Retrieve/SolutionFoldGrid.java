package io.github.repir.apps.Retrieve;

import io.github.repir.Repository.Tools.Parameter;
import java.math.BigDecimal;
import java.util.Collection;
import io.github.repir.Repository.Repository;

/**
 *
 * @author Jeroen Vuurens
 */
public class SolutionFoldGrid extends SolutionFold {

   public SolutionFoldGrid(Repository repository, int fold, final Tuner tuner) {
      super(repository, fold, tuner);
   }

   public boolean isFold() {
      return true;  
   }
   
   public point findMax() {
      point max = null;
      for (point p : (Collection<point>)metric.values()) {
         p.point3 = 0;
         for (Solution s : tuner.sol) {
            if (s != this) {
               point p2 = (point)s.metric.get(p);
               if (p2 != null)
                  p.point3 += p2.getValue();
            }
         }
         if (max == null || max.point3 < p.point3) {
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
