package io.github.repir.Strategy;

import io.github.repir.Strategy.Operator.PLMProximityModel;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Strategy.ScoreFunction.PLMScoreFunction;
import io.github.repir.tools.lib.Log;

/**
 * Implementation of Zhao & Yun's Proximity Language Model
 * @author jeroen
 */
public class PLMRetrievalModel extends RetrievalModel {

   public static Log log = new Log(PLMRetrievalModel.class);

   public PLMRetrievalModel(Retriever retriever) {
      super(retriever);
   }
   
   @Override
   public String getQueryToRetrieve() {
      query.setScorefunctionClassname(PLMScoreFunction.class.getSimpleName());
      return io.github.repir.tools.lib.PrintTools.sprintf("%s:(%s)", 
              PLMProximityModel.class.getSimpleName(), query.query );
   }
}
