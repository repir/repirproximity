package io.github.repir.Strategy.Collector;

import io.github.repir.Strategy.Collector.CollectorDocument;
import io.github.repir.Strategy.Collector.CollectorDocument;
import java.util.ArrayList;
import java.util.Collection;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.tools.Lib.Log;

/**
 * Default mastercollector to retrieve a ranked list of documents. Each document
 * is scored by the documentPrior + sum of leaf usedfeatures. The {@link Retriever#getDefaultLimit()
 * }
 * is called to determine the maximum number of documents retrieved. The
 * query.documentlimit is used to documentlimit the retrieved list.
 * <p/>
 * @author jeroen
 */
public class CollectorDocumentSpeed extends CollectorDocument {

   public static Log log = new Log(CollectorDocumentSpeed.class);

   public CollectorDocumentSpeed() {
      super();
   }

   public CollectorDocumentSpeed(RetrievalModel rm) {
      super(rm);
   }

   @Override
   public Collection<String> getReducerIDs() {
      return new ArrayList<String>();
   }

   @Override
   public void finishSegmentRetrieval() {
      this.retrievalmodel.collectors.remove(this);
   }
}
