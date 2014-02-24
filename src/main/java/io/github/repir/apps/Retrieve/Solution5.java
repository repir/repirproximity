package io.github.repir.apps.Retrieve;
import java.math.BigDecimal; 
import io.github.repir.Repository.Repository;

/**
 *
 * @author Jeroen Vuurens
 */
public class Solution5 extends Solution<Parameter5> {

  public Solution5(Repository repository, final Tuner outer) {
      super( repository, outer );
  }
   
   public point findMax() {
      point max = null;
      for (point p : metric.values()) {
         p.point3 = -1;
         if (max == null || getPoint3(max) < getPoint3(p)) {
            max = p;
         }
      }
      return max;
   }

   public double getPoint3(point p) {
      if (p.point3 == -1) {
         p.point3 = 0;
         if (!p.onEdge()) {
            for (Solution5 s : (Solution5[])tuner.sol) {
               if (s != this) {
                  double point3 = s.getPoint3(s.metric.get(p), 0);
                  p.point3 += point3;
               }
            }
         }
      }
      return p.point3;
   }

   private double getPoint3(point p, int param) {
      if (param == parameters.size()) {
         //SolutionFold f = (SolutionFold) this;
         //log.trace("add fold=%d %s %f", f.fold, p, p.getValue());
         return p.getValue();
      }
      return getPoint3(p, param + 1) + getPoint3(p.below(param), param + 1) + getPoint3(p.above(param), param + 1);
   }

   public void generateFirstPoints() {
      point middle = new point();
      for (int p = 0; p < parameters.size(); p++) {
         middle.v[p] = parameters.get(p).getPoints().get(2);
      }
      generateMorePoints(0, middle);
   }

   public void generateMorePoints(int param, point max) {
      if (param == tuner.parameters.size()) {
         if (!metric.containsKey(max)) {
            metric.put(max, max);
         }
      } else {
         Parameter5 currentparam = parameters.get(param);
         for (BigDecimal v : currentparam.point5(max)) {
            //tune5.log.info("%s", v.toString());
            point newpoint = new point(max);
            newpoint.v[param] = v;
            generateMorePoints(param + 1, newpoint);
         }
      }
   }

   public Parameter5 findMaxVariance(point optimum) {
      Parameter5 max = null;
      for (Parameter5 p : parameters) {
         if (!p.targetReached(optimum)) {
            p.benefit = findMaxVariance(optimum, p.index, 0);
            if (max == null || max.benefit < p.benefit) {
               max = p;
            }
         }
      }
      return max;
   }

   public double findMaxVariance(point point, int p1, int p2) {
      if (p2 == parameters.size()) {
         return point.getValue();
      }
      if (p2 == p1) {
         return findMaxVariance(point, p1, p2 + 1);
      }
      return findMaxVariance(point.above(p2), p1, p2 + 1) + findMaxVariance(point, p1, p2 + 1) + findMaxVariance(point.below(p2), p1, p2 + 1);
   }
}
