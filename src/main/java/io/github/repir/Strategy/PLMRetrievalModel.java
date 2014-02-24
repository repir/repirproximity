package io.github.repir.Strategy;

import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.Retriever.Retriever;
import io.github.repir.tools.Lib.Log;

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
      query.setScorefunctionClass("PLMScoreFunction");
      return io.github.repir.tools.Lib.PrintTools.sprintf("PLMFeature:(%s)", query.stemmedquery );
   }
}
