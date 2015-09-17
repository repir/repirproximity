package io.github.repir.Strategy;

import io.github.repir.Strategy.Operator.CPEProximityModel;
import io.github.repir.Strategy.RetrievalModelExpander;
import io.github.repir.Strategy.GraphRoot;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Retriever.Query;
import io.github.htools.lib.Log;

/**
 * Expands a Query of independent terms with the Proximity Model described 
 * as CPE by Vuurens and de Vries (2014).
 *
 * @author jeroen
 */
public class CPERetrievalModel extends RetrievalModelExpander {

   public static Log log = new Log(CPERetrievalModel.class);

   public CPERetrievalModel(Retriever retriever) {
      super(retriever);
   }

   @Override
   public String expandQuery() {
      return query.query + " " + GraphRoot.reformulate(CPEProximityModel.class, query.query);
   }
}
