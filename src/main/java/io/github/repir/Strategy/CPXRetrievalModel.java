package io.github.repir.Strategy;

import io.github.repir.Strategy.RetrievalModelExpander;
import io.github.repir.Strategy.GraphRoot;
import io.github.repir.Retriever.Retriever;
import io.github.repir.tools.Lib.Log;

/**
 * Implementation of CPE by Vuurens and de Vries (2013) parameters:
 *
 * @author jeroen
 */
public class CPXRetrievalModel extends RetrievalModelExpander {

   public static Log log = new Log(CPXRetrievalModel.class);

   public CPXRetrievalModel(Retriever retriever) {
      super(retriever);
   }

   @Override
   public String getQueryToRetrieve() {
      if (query.stemmedquery.indexOf(' ') > 0) {
         return query.stemmedquery + " " + GraphRoot.reformulate(CPXFeature.class, query.stemmedquery);
      } else
         return query.stemmedquery;
   }
}
