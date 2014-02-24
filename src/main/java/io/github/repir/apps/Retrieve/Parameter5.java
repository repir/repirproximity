package io.github.repir.apps.Retrieve;
import java.math.BigDecimal; 
import java.util.ArrayList;
import java.util.TreeSet;
import io.github.repir.Repository.Tools.Parameter;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.MathTools;

/**
 *
 * @author Jeroen Vuurens
 */
public class Parameter5 extends Parameter {
   public static Log log = new Log(Parameter5.class);
   public double benefit;
   
  public Parameter5(String setting) {
      super(setting);
  }
  
  public void reset() {
     points = null;
     set = new TreeSet<BigDecimal>();
     int steps = width.divide(targetstep.multiply(new BigDecimal(4)), 0, BigDecimal.ROUND_CEILING).intValue();
     step = targetstep.multiply(new BigDecimal(MathTools.nextHighestPower2(steps)));
     optimum = lower.add(step).add(step);
     generatePoints();
  }

   public void generatePoints() {
      add(optimum);
      add(optimum.subtract(step));
      add(optimum.subtract(step).subtract(step));
      add(optimum.add(step));
      add(optimum.add(step).add(step));
   }

   public boolean targetReached(Solution.point optimum) {
      int i = getPoints().indexOf(optimum.v[index]);
      if (i > 0 && optimum.v[index].subtract(points.get(i - 1)).compareTo(targetstep) > 0) {
         return false;
      }
      if (i == points.size() - 1 || points.get(i + 1).subtract(optimum.v[index]).compareTo(targetstep) > 0) {
         return false;
      }
      return true;
   }

   public ArrayList<BigDecimal> point5(Solution.point optimum) {
      ArrayList<BigDecimal> list = new ArrayList<BigDecimal>();
      BigDecimal gap = getGap(optimum.v[index]);
      //tune5.log.info("point5 optimum=%s gap=%s", optimum.v[index], gap);
      for (int i : new int[]{-2, -1, 0, 1, 2}) {
         BigDecimal v = optimum.v[index].add(gap.multiply(new BigDecimal(i)));
         if (points.contains(v))
            list.add(v);
      }
      return list;
   }

   private BigDecimal getGap(BigDecimal v) {
      int i = pointIndex(v);
      if (i < 1) {
         Tuner.log.fatal("lower margin holds maximum");
      }
      BigDecimal gap = v.subtract(points.get(i - 1));
      if (i < points.size() - 1) {
         BigDecimal gap2 = points.get(i + 1).subtract(v);
         if (gap2.compareTo(gap) < 0) {
            gap = gap2;
         }
      }
      return gap;
   }

   public void moreDetail(Solution.point optimum) {
      BigDecimal gap = getGap(optimum.v[index]).divide(new BigDecimal(2));
      if (gap.compareTo(targetstep) >= 0) {
         add(optimum.v[index].add(gap));
         add(optimum.v[index].subtract(gap));
      }
   }

   public void addMargin(Solution.point optimum) {
      BigDecimal gap = getGap(optimum.v[index]);
      for (int m : new int[]{-2, -1, 1, 2}) {
         BigDecimal n = optimum.v[index].add(gap.multiply(new BigDecimal(m)));
         add(n);
      }
   }
}
