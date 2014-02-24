package io.github.repir.apps.Retrieve;

import java.util.ArrayList;
import java.util.Collection;
import io.github.repir.Repository.Tools.Parameter;
import io.github.repir.Repository.Repository;
import io.github.repir.apps.Retrieve.Solution.point;
import static io.github.repir.apps.Retrieve.Tuner.log;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Lib.ArrayTools;
import io.github.repir.tools.Lib.HDTools;

public abstract class TunerCross<S extends Solution, P extends Parameter> extends Tuner<S, P> {

   public TunerCross(Repository repository) {
      super(repository);
   }

   @Override
   public void storeSolution(boolean checkfinished) {
      Solution stemp[] = new Solution[sol.length];
      System.arraycopy(sol, 0, stemp, 0, sol.length);
      if (checkfinished) {
         for (int target = 0; target < sol.length; target++) {
            reset();
            S t = sol[0];
            sol[0] = sol[target];
            sol[target] = t;
            if (notFinished()) {
               log.fatal("the paramaters are not yet fully tuned");
            }
         }
      } else {
         reset();
         for (Solution s : stemp) {
            s.needsCompute();
         }
      }
      for (Solution s : stemp) { 
         Datafile configfile = new Datafile(s.repository.getParameterFile());
         double maxvalue = Integer.MIN_VALUE;
         point pmax = null;
         for (point p : (Collection<point>) s.allPoints()) {
            double total = 0;
            for (Solution s2 : stemp) {
               if (s != s2) {
                  point p2 = s2.findPoint(p);
                  //log.info("%s %f %f", p2.getParameters(), p2.getValue(), total);
                  total += p2.getValue();
               }
            }
            if (total == 0) {
               log.info("warning 0 %s", p.getParameters());
            }
            if (total > maxvalue) {
               //log.info("%s %f", p.getParameters(), total);
               maxvalue = total;
               pmax = p;
            }
         }
         configfile.openWrite();
         for (int topic = s.topicstart; topic < s.topicend; topic++) {
            configfile.printf("query.%d=%s\n", topic, pmax.getParameters());
         }
         configfile.closeWrite();
      }
   }

   public point getMaxSolution(boolean checkfinished) {
      if (checkfinished) {
         for (int target = 0; target < sol.length; target++) {
            reset();
            ArrayTools.swap(sol, 0, target);
            if (notFinished()) {
               log.fatal("the paramaters are not yet fully tuned");
            }
         }
      }
      double maxvalue = Integer.MIN_VALUE;
      point pmax = null;
      for (point p : (Collection<point>) sol[0].allPoints()) {
         double total = 0;
         for (Solution s2 : sol) {
            point p2 = s2.findPoint(p);
            //log.info("%s %f %f", p2.getParameters(), p2.getValue(), total);
            total += p2.getValue();
         }
         if (total == 0) {
            log.info("warning 0 %s", p.getParameters());
         }
         if (total > maxvalue) {
            maxvalue = total;
            pmax = p;
         }
      }
      return pmax;
   }
}
