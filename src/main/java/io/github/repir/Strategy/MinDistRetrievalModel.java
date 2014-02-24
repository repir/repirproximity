package io.github.repir.Strategy;

import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.Retriever.Retriever;
import io.github.repir.tools.Lib.Log;

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
      query.setScorefunctionClass("MinDistScoreFunction");
      return io.github.repir.tools.Lib.PrintTools.sprintf("MinDistFeature:(%s) %s ", query.stemmedquery, query.stemmedquery);
   }
}
