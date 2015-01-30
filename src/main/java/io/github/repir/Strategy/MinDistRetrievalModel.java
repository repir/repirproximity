package io.github.repir.Strategy;

import io.github.repir.Strategy.Operator.MinDistProximityModel;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Strategy.ScoreFunction.MinDistScoreFunction;
import io.github.repir.tools.lib.Log;

/**
 * Implementation of Tao & Zhai's MinDIst proximity measure
 * @author jeroen
 */
public class MinDistRetrievalModel extends RetrievalModel {

   public static Log log = new Log(MinDistRetrievalModel.class);

   public MinDistRetrievalModel(Retriever retriever) {
      super(retriever);
   }
   
   @Override
   public String getQueryToRetrieve() {
      query.setScorefunctionClassname(MinDistScoreFunction.class.getSimpleName());
      return io.github.repir.tools.lib.PrintTools.sprintf("%s:(%s) %s ",
              MinDistProximityModel.class.getSimpleName(), query.query, query.query );
   }
}
