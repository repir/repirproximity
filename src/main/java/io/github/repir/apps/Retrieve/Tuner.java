package io.github.repir.apps.Retrieve;

import io.github.repir.Repository.Tools.Parameter;
import edu.emory.mathcs.backport.java.util.Collections;
import java.util.ArrayList;
import java.util.HashSet;
import io.github.repir.Repository.ModelParameters;
import io.github.repir.Repository.Repository;
import io.github.repir.TestSet.ResultSet;
import io.github.repir.apps.Retrieve.Solution.point;
import io.github.repir.tools.Lib.Log;

public abstract class Tuner<S extends Solution, P extends Parameter> {

   public static Log log = new Log(Tuner.class);
   public Repository repository;
   public Repository repositories[];
   final ArrayList<P> parameters = new ArrayList<P>();
   String storedparameters[];
   public S sol[];
   int fold = 0;

   public Tuner(Repository repository) {
      this.repository = repository;
      storedparameters = repository.getStoredFreeParameters();
      for (String p : repository.getFreeParameters()) {
         parameters.add(createParameter(p));
      }
      Collections.sort(parameters);
      for (int i = 0; i < parameters.size(); i++) {
         parameters.get(i).index = i;
      }

      repositories = repository.getTuneRepositories();
      createSolutionArray();
   }
   
   public abstract P createParameter(String p);
   
   public void reset() {
      for (P p : parameters) {
         p.reset();
      }
      for (S s : sol) {
         s.reset();
         s.generateFirstPoints();
      }
   }
      
   public abstract void createSolutionArray();

   public abstract void storeSolution(boolean checkfinished);

   public abstract point getMaxSolution(boolean checkfinished);

   public void computePoints() {
      for (int target = 0; target < sol.length; target++) {
         reset();
         S t = sol[0];
         sol[0] = sol[target];
         sol[target] = t;
         while (notFinished()) {
            for (S s : sol) {
               s.computePoints();
            }
         }
      }
   }
   
   public boolean needsCompute() {
      for (S s : sol) {
         if (s.needsCompute()) {
            return true;
         }
      }
      return false;
   }

      public boolean notFinished() {
         return needsCompute();
      }
      
   public void writeRecord(S sol, ResultSet resultset) {
      sol.modelparameters.closeRead();
      sol.modelparameters.openWrite();
      ModelParameters.Record newRecord = sol.createRecord();
      newRecord.map = resultset.result[1].avg;
      sol.modelparameters.write(newRecord);
      sol.modelparameters.closeWrite();
   }

   public static ArrayList<String> getTuneParameters(Repository repository) {
      String freeparameters[] = repository.getConfigurationSubStrings("retriever.freeparameter");
      ArrayList<String> tuneparameters = new ArrayList<String>();
      for (String s : freeparameters) {
         if (s.indexOf('=') > 0) {
            tuneparameters.add(s);
         }
      }
      return tuneparameters;
   }
}
