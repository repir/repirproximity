package io.github.repir.apps.Retrieve;

import io.github.repir.Repository.Tools.ParameterGrid;
import io.github.repir.Repository.Repository;
import io.github.repir.tools.Lib.Log;

public class TunerFoldGrid extends TunerFold<SolutionFoldGrid, ParameterGrid> {

   public static Log log = new Log(TunerFoldGrid.class);

   public TunerFoldGrid(Repository repository) {
      super( repository );
   }
 
   public ParameterGrid createParameter( String p ) {
      return new ParameterGrid( p );  
   }
   
   @Override
   public void createSolutionArray() {
      sol = new SolutionFoldGrid[ 10 ];
      for (int fold = 0; fold < 10; fold++) 
         sol[fold] = new SolutionFoldGrid( repository, fold, this );
   }
}
