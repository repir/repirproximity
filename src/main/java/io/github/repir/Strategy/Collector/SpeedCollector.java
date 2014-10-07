package io.github.repir.Strategy.Collector;

import java.util.HashMap;
import io.github.repir.Repository.ModelSpeed;
import io.github.repir.Repository.ModelSpeed.Record;
import io.github.repir.Retriever.Document;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.tools.Content.EOCException;
import io.github.repir.tools.Structure.StructureReader;
import io.github.repir.tools.Structure.StructureWriter;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.Profiler;

/**
 * Measures to time taken to retrieve a ranked list for a query in the mapper.
 * @author jer
 */
public class SpeedCollector extends CollectorCachable<Record> {

   public static Log log = new Log(SpeedCollector.class);
   public static Profiler profiler = new Profiler(SpeedCollector.class);
   static ModelSpeed dummyfeature = new ModelSpeed(null);
   HashMap<Record, Record> records = new HashMap<Record, Record>();

   public SpeedCollector() {
      super();
   }

   public SpeedCollector(RetrievalModel f) {
      super(f);
   }

   @Override
   public boolean equals(Object o) {
      return false;
   }

   @Override
   public int hashCode() {
      return 0;
   }

   public void startAppend() {
      sdf = getStoredDynamicFeature();
      for (Record r : ((ModelSpeed) sdf).readAll()) {
         records.put(r, r);
      }
      sdf.openWrite();
   }

   @Override
   public void streamappend() {
      for (Record r : records.values()) {
         sdf.write(r);
      }
   }

   @Override
   public void streamappend(Record record) {
      sdf.write(record);
   }

   @Override
   public void streamappend(CollectorCachable c) {
      for (Record r : records.values()) {
         ((SpeedCollector)c).streamappend(r);
      }
   }

   public Record createRecord() {
      ModelSpeed sdf = (ModelSpeed) getStoredDynamicFeature();
      Record r = (Record) sdf.newRecord();
      return r;
   }

   @Override
   public ModelSpeed getStoredDynamicFeature() {
      ModelSpeed sdf = ModelSpeed.get(this.getRepository());
      return sdf;
   }

   @Override
   public void aggregate(Collector collector) {
      for (Record r : ((SpeedCollector) collector).records.values()) {
         Record existingr = records.get(r);
         if (existingr != null) {
            if (r.time < existingr.time) {
               existingr.time = r.time;
            }
         } else {
            records.put(r, r);
         }
      }
   }

   @Override
   public void aggregateDuplicatePartition(Collector collector) {
     aggregate(collector);
   }
   
   @Override
   public void writeKey(StructureWriter writer) {
   }

   @Override
   public void readKey(StructureReader reader) throws EOCException {
   }

   @Override
   public void writeValue(StructureWriter writer) {
      writer.writeC(records.size());
      for (Record r : records.values()) {
         writer.write(r.query);
         writer.write(r.strategy);
         writer.write(r.time);
      }
   }

   @Override
   public void readValue(StructureReader reader) throws EOCException {
      int count = reader.readCInt();
      for (int sense = 0; sense < count; sense++) {
         int query = reader.readInt();
         String strategy = reader.readString();
         double time = reader.readDouble();
         Record r = dummyfeature.newRecord(strategy, query);
         r.time = time;
         records.put(r, r);
      }
   }

   @Override
   public void reuse() {
      records = new HashMap<Record, Record>();
   }

   @Override
   public boolean reduceInQuery() {
      return false;
   }

   @Override
   public void setCollectedResults() {
   }

   @Override
   public void finishSegmentRetrieval() {
      Record r = createRecord();
      r.query = strategy.query.id;
      r.strategy = strategy.query.getStrategyClass();
      r.time = profiler.getTimePassed() / 1000;
      records.put(r, r);
   }

   @Override
   public void prepareRetrieval() {
     profiler.startTime();
   }

   @Override
   protected void collectDocument(Document doc) {
   }

   @Override
   public void decode() {
   }
}
