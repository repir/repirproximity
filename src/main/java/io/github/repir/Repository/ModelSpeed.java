package io.github.repir.Repository;

import java.util.ArrayList;
import io.github.repir.Repository.ModelSpeed.File;
import io.github.repir.Repository.ModelSpeed.Record;
import io.github.htools.io.Datafile;
import io.github.htools.io.struct.StructuredFileKeyValue;
import io.github.htools.io.struct.StructuredFileKeyValueRecord;
import io.github.htools.lib.Log;
import io.github.htools.lib.MathTools;
import io.github.htools.lib.PrintTools;

/**
 * A Feature to store the retrieval speed per query per configuration (strategy).
 * @author jer
 */
public class ModelSpeed extends StoredDynamicFeature<File, Record> {

   public static Log log = new Log(ModelSpeed.class);
   
   public ModelSpeed(Repository repository) {
      super(repository);
   }

   public static ModelSpeed get(Repository repository) {
       String label = canonicalName(ModelSpeed.class);
       ModelSpeed modelspeed = (ModelSpeed)repository.getStoredFeature(label);
       if (modelspeed == null) {
          modelspeed = new ModelSpeed(repository);
          repository.storeFeature(label, modelspeed);
       }
       return modelspeed;
   }
   
   @Override
   public File createFile(Datafile df) {
      return new File(df);
   }
   
   public Record newRecord( String strategy, int query ) {
      return new Record(strategy, query);
   }

   public ArrayList<Record> readAll() {
      ArrayList<Record> records = new ArrayList<Record>();
      openRead();
      records.addAll(getFile().getKeys());
      return records;
   }

   public class File extends StructuredFileKeyValue<Record> {

      public StringField strategy = this.addString("strategy");
      public IntField query = this.addInt("query");
      public DoubleField time = this.addDouble("time");

      public File(Datafile df) {
         super(df);
      }

      @Override
      public Record newRecord() {
         return new Record();
      }
      
      public Record newRecord( String strategy, int query ) {
         return new Record( strategy, query );
      }

      @Override
      public Record closingRecord() {
         Record r = new Record();
         r.query = -1;
         r.strategy = "";
         return r;
      }
   }

   public class Record implements StructuredFileKeyValueRecord<File> {
      public String strategy;
      public int query;
      public double time = -1;
      
      public Record() {}
      
      public Record( String strategy, int query ) {
         this.strategy = strategy;
         this.query = query;
      }
      
      @Override
      public String toString() {
         return PrintTools.sprintf("%3d %.3f %s", query, time, strategy); 
      }
      
      @Override
      public int hashCode() {
         int hash = 31;
         hash = MathTools.combineHash(hash, strategy.hashCode());
         hash = MathTools.combineHash(hash, query);
         return MathTools.finishHash(hash);
      }

      @Override
      public boolean equals(Object r) {
         if (r instanceof Record) {
            Record record = (Record)r;
            return strategy.equals(record.strategy) && query == record.query;
         }
         return false;
      }

      public void write(File file) {
         file.strategy.write( strategy );
         file.query.write( query );
         file.time.write(time);
      }

      public void read(File file) {
         strategy = file.strategy.value;
         query = file.query.value;
         time = file.time.value;
      }

      public void convert(StructuredFileKeyValueRecord record) {
         Record r = (Record)record;
         r.strategy = strategy;
         r.query = query;
         r.time = time;
      }
   }
   
   public Record read( String strategy, int query ) {
      this.openRead();
      Record s = (Record)newRecord( strategy, query );
      Record r = (Record) find(s);
      return r;
   }
   
   public Record read( Record record ) {
      this.openRead();
      Record found = (Record) find(record);
      return (found == null)?record:found;
   }
}
