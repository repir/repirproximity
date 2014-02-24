package io.github.repir.apps.Retrieve;

import java.util.ArrayList;
import java.util.Collection;
import io.github.repir.Repository.Tools.Parameter;
import java.util.HashSet;
import io.github.repir.Repository.Repository;
import io.github.repir.apps.Retrieve.Solution.point;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Lib.ArrayTools;
import io.github.repir.tools.Lib.Log;

public abstract class TunerFold<S extends Solution, P extends Parameter> extends Tuner<S, P> {

   public static Log log = new Log(TunerFold.class);

   public TunerFold(Repository repository) {
      super(repository);
   }

   @Override
   public void computePoints() {
         reset();
         while (notFinished()) {
            //log.info("iter %b %b %d", notFinished(), needsCompute(), sol.length);
            sol[0].computePoints();
         }
   }
   
   @Override
   public boolean needsCompute() {
      return sol[0].needsCompute();
   }

   public void storeSolution(boolean checkfinished) {
      Solution.point psol[] = new Solution.point[10];
      S stemp[] = (S[]) ArrayTools.createArray(sol, sol.length);

      System.arraycopy(sol, 0, stemp, 0, sol.length);
      for (int target = 0; target < sol.length; target++) {
         reset();
         S t = sol[target];
         sol[target] = sol[0];
         sol[0] = t;
         if (notFinished() && checkfinished) {
            log.fatal("the parameters are not yet fully tuned");
         }
         //log.info("target %s %d %d %s", parameters.get(0).getPoints(), target, t.fold, p[target]);
      }
      sol = stemp;
      Datafile configfile = new Datafile(repository.getParameterFile());
      configfile.openWrite();
      for (int fold = 0; fold < sol.length; fold++) {
         double maxvalue = Integer.MIN_VALUE;
         point pmax = null;
         for (point p : (Collection<point>)sol[fold].allPoints()) {
            double total = 0;
            for (int fold2 = 0; fold2 < sol.length; fold2++) {
               if (fold != fold2) {
                  point p2 = sol[fold2].findPoint(p);
                  total += p2.getValue();
               }
            }
            if (total > maxvalue) {
               maxvalue = total;
               pmax = p;
            }
         }
         psol[fold] = pmax;
         for (int topic = sol[fold].topicstart; topic < sol[fold].topicend; topic++) {
            configfile.printf("query.%d=%s\n", topic, psol[fold].getParameters());
         }
      }
      configfile.closeWrite();
      //showSolution();
   }

   public point getMaxSolution(boolean checkfinished) {
      for (int target = 0; target < sol.length; target++) {
         reset();
         ArrayTools.swap(sol, 0, target);
         if (notFinished() && checkfinished) {
            log.fatal("the parameters are not yet fully tuned");
         }
      }
         double maxvalue = Integer.MIN_VALUE;
         point pmax = null;
         for (point p : (Collection<point>)sol[0].allPoints()) {
            double total = 0;
            for (int fold2 = 0; fold2 < sol.length; fold2++) {
                  point p2 = sol[fold2].findPoint(p);
                  total += p2.getValue();
            }
            if (total > maxvalue) {
               maxvalue = total;
               pmax = p;
            }
         }
         return pmax;
   }

   public void showSolution() {
      Solution.point p = sol[2].findMax();
      Solution.log.setLevel(Log.TRACE);
      p.point3 = -1;
      //double point3 = sol[2].getPoint3(p);
      //log.info("%s=%f", p, point3);
      log.info("%d %d", sol[2].topicstart, sol[2].topicend);
   }
}
