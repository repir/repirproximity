package io.github.repir.Strategy;

import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.Retriever.Retriever;
import io.github.repir.tools.Lib.Log;

public class CPESensitivityRetrievalModel extends RetrievalModel {

   public static Log log = new Log(CPESensitivityRetrievalModel.class);

   public CPESensitivityRetrievalModel(Retriever retriever) {
      super(retriever);
   }

   @Override
   public String getQueryToRetrieve( ) {
      if (query.stemmedquery.indexOf(' ') > 0)
         return io.github.repir.tools.Lib.PrintTools.sprintf("%s ProximitySensitivityFeature:(%s) ", query.stemmedquery, query.stemmedquery);
      else
         return query.stemmedquery;
   }
}
