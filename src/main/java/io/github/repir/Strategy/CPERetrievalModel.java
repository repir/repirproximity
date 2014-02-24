package io.github.repir.Strategy;

import io.github.repir.Strategy.RetrievalModelExpander;
import io.github.repir.Strategy.GraphRoot;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;

/**
 * Implementation of CPE by Vuurens and de Vries (2013) parameters:
 * proximitymodel.alpha=
 *
 * @author jeroen
 */
public class CPERetrievalModel extends RetrievalModelExpander {

   public static Log log = new Log(CPERetrievalModel.class);

   public CPERetrievalModel(Retriever retriever) {
      super(retriever);
   }

   @Override
   public String getQueryToRetrieve() {
      return query.stemmedquery + " " + GraphRoot.reformulate(CPEFeature.class, query.stemmedquery);
   }
}
