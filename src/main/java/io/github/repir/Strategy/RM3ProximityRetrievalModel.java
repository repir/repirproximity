package io.github.repir.Strategy;

import io.github.repir.Retriever.Retriever;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;

/**
 * Combines the ProximityModel with RM3 by using the ProximityModel in the first pass
 * to improve results, in order to obtain better quality feedback documents that are
 * then used by RM3 to select candidate query expansion terms. In this version the
 * ProximityModel is not used for the second retrieval pass.
 * @author jeroen
 */
public class RM3ProximityRetrievalModel extends RetrievalModelRM {

   public static Log log = new Log(RM3ProximityRetrievalModel.class);

   public RM3ProximityRetrievalModel(Retriever retriever) {
      super(retriever);
   }

   @Override
   public String getQueryToRetrieve() {
      if (query.originalquery.indexOf(' ') > 0)
         return io.github.repir.tools.Lib.PrintTools.sprintf("%s ProximityFeature:(%s) ", query.stemmedquery, query.stemmedquery);
      else
         return query.stemmedquery;
   }
}
