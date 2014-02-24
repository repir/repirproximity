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
public class CPERetrievalModel2 extends RetrievalModelExpander {

   public static Log log = new Log(CPERetrievalModel2.class);

   public CPERetrievalModel2(Retriever retriever) {
      super(retriever);
   }

   @Override
   public String getQueryToRetrieve() {
      return query.stemmedquery + " " + GraphRoot.reformulate(CPEFeature2.class, query.stemmedquery);
   }
}
