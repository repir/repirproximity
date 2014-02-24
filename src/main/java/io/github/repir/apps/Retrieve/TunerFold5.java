package io.github.repir.apps.Retrieve;

import io.github.repir.Repository.Repository;
import io.github.repir.apps.Retrieve.Solution.point;
import io.github.repir.tools.Lib.Log;

public class TunerFold5 extends TunerFold<SolutionFold5, Parameter5> {

   public static Log log = new Log(TunerFold5.class);

   public TunerFold5(Repository repository) {
      super( repository );
   }
 
   public Parameter5 createParameter( String p ) {
      return new Parameter5( p );  
   }
   
   @Override
   public void createSolutionArray() {
      sol = new SolutionFold5[ 10 ];
      for (int fold = 0; fold < 10; fold++) 
         sol[fold] = new SolutionFold5( repository, fold, this );
   }
   
      public boolean notFinished() {
         while (!needsCompute()) {
            log.info("notFinished() %s", sol[0].getPoints());
            SolutionFold5 starget = sol[0];
            int oldmax = starget.metric.size();
            point max = starget.findMax();
            for (int p = 0; p < parameters.size(); p++) {
               parameters.get(p).addMargin(max);
            }
            for (SolutionFold5 s : sol)
               s.generateMorePoints(0, max);
            if (!needsCompute()) {
               Parameter5 p = starget.findMaxVariance(max);
               if (p != null) {
                  p.moreDetail(max);
               }
               for (SolutionFold5 s : sol)
                  s.generateMorePoints(0, max);
            }
            if (oldmax == starget.metric.size())
               break;
         }
         return needsCompute();
      }

}
