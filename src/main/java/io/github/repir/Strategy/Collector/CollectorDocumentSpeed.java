package io.github.repir.Strategy.Collector;

import io.github.repir.Strategy.Collector.CollectorDocument;
import io.github.repir.Strategy.Collector.CollectorDocument;
import java.util.ArrayList;
import java.util.Collection;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.tools.lib.Log;

/**
 * Replaces CollectorDocument when measuring retrieval speed, to avoid retrieved 
 * documents from being reduced.
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
