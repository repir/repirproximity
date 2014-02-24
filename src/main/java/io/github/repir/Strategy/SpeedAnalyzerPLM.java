package io.github.repir.Strategy;

import io.github.repir.Strategy.Analyzer;
import io.github.repir.Retriever.Query;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Strategy.Collector.CollectorDocumentSpeed;
import io.github.repir.Strategy.Collector.SpeedCollector;
import io.github.repir.tools.Lib.Log;

/**
 * This feature caches the context of all occurrences of a term in the
 * collection. When cached, this feature can be used by other features to
 * analyze the local context a term appears in.
 * <p/>
 * @author jeroen
 */
public class SpeedAnalyzerPLM extends PLMRetrievalModel implements Analyzer {

   public static Log log = new Log(SpeedAnalyzerPLM.class);
   public SpeedCollector collector;

   public SpeedAnalyzerPLM(Retriever retriever) {
      super(retriever);
   }

   @Override
   public void setCollector() {
         new CollectorDocumentSpeed(this);
         collector = new SpeedCollector(this);
   }
   
   @Override
   public void prepareWriteReduce(Query q) {
   }
   
   @Override
   public void writeReduce(Query q) {
   }
   
   @Override
   public void finishWriteReduce() {
   }

   @Override
   public final void prepareRetrieval() {
      log.sleepRnd(5000);
      super.prepareRetrieval();
   }
}
