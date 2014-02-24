package io.github.repir.apps.Retrieve;

import io.github.repir.Repository.Tools.ParameterGrid;
import io.github.repir.Repository.Repository;

public class TunerCrossGrid extends TunerCross<SolutionGrid, ParameterGrid> {

   public TunerCrossGrid(Repository repository) {
      super(repository);
   }

   public ParameterGrid createParameter( String p ) {
      return new ParameterGrid( p );  
   }
   
   @Override
   public void createSolutionArray() {
      sol = new SolutionGrid[ repositories.length ];
      for (int rr = 0; rr < repositories.length; rr++)
         sol[rr] = new SolutionGrid( repositories[rr], this );
   }
}
