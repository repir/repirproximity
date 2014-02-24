package io.github.repir.apps.Retrieve;

import io.github.repir.Repository.Repository;
import io.github.repir.apps.Retrieve.Solution.point;

public class TunerCross5 extends TunerCross<Solution5, Parameter5> {

   public TunerCross5(Repository repository) {
      super(repository);
   }

   public Parameter5 createParameter( String p ) {
      return new Parameter5( p );  
   }

   
   @Override
   public void createSolutionArray() {
      sol = new Solution5[ repositories.length ];
      for (int rr = 0; rr < repositories.length; rr++)
         sol[rr] = new Solution5( repositories[rr], this );
   }
   
   public boolean notFinished() {
         while (!needsCompute()) {
            log.info("notFinished() %s", sol[0].getPoints());
            Solution5 starget = sol[0];
            int oldmax = starget.metric.size();
            point max = starget.findMax();
            for (int p = 0; p < parameters.size(); p++) {
               parameters.get(p).addMargin(max);
            }
            for (Solution5 s : sol)
               s.generateMorePoints(0, max);
            if (!needsCompute()) {
               Parameter5 p = starget.findMaxVariance(max);
               if (p != null) {
                  p.moreDetail(max);
               }
               for (Solution5 s : sol)
                  s.generateMorePoints(0, max);
            }
            if (oldmax == starget.metric.size())
               break;
         }
         return needsCompute();
      }

}
