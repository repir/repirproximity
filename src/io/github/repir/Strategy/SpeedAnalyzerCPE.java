package io.github.repir.Strategy;

import io.github.repir.Strategy.Operator.Analyzer;
import io.github.repir.Retriever.Query;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Strategy.Collector.CollectorDocumentSpeed;
import io.github.repir.Strategy.Collector.SpeedCollector;
import io.github.repir.tools.Lib.Log;

/**
 * Measures the retrieval speed by setting up a SpeedCollector. 
 * <p/>
 * @author jeroen
 */
public class SpeedAnalyzerCPE extends CPERetrievalModel implements Analyzer {

   public static Log log = new Log(SpeedAnalyzerCPE.class);
   public SpeedCollector collector;

   public SpeedAnalyzerCPE(Retriever retriever) {
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
