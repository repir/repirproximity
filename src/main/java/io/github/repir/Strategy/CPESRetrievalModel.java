package io.github.repir.Strategy;

import io.github.repir.Strategy.Operator.CPESProximityModel;
import io.github.repir.Retriever.Retriever;
import io.github.htools.lib.Log;

/**
 * Implementation of CPE by Vuurens and de Vries (2013) parameters:
 *
 * @author jeroen
 */
public class CPESRetrievalModel extends RetrievalModelExpander {

   public static Log log = new Log(CPESRetrievalModel.class);

   public CPESRetrievalModel(Retriever retriever) {
      super(retriever);
   }

   @Override
   public String expandQuery() {
      if (query.query.indexOf(' ') > 0) {
         return query.query + " " + GraphRoot.reformulate(CPESProximityModel.class, query.query);
      } else
         return query.query;
   }
}
