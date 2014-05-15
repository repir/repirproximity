   package io.github.repir.RetrieverSpeed;

import io.github.repir.Retriever.MapReduce.RetrieverMRReduce;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Reducer;
import io.github.repir.Repository.ModelSpeed.Record;
import io.github.repir.Repository.ModelSpeed;
import io.github.repir.Repository.Repository;
import io.github.repir.Strategy.Strategy;
import io.github.repir.Retriever.Query;
import io.github.repir.Repository.Configuration;
import io.github.repir.tools.Lib.Log;

/**
 * The reducer is generic, using the passed query with the name of the retrieval
 * model to aggregate the results that were collected by each mapper. Each
 * reducer reduces only a single query. The incoming Query object is used to
 * reconstruct the same retrieval model in every location (mappers and reducer),
 * so that the retrieval model can process the map and writeReduce steps similar to
 * retrieval on a single machine.
 * <p/>
 * @author jeroen
 */
public class RetrieverSpeedReduce extends Reducer<RecordedTime, NullWritable, NullWritable, NullWritable> {

   public static Log log = new Log(RetrieverMRReduce.class);
   HashMap<Integer, Query> queries;
   HashMap<Integer, Strategy> strategies = new HashMap<Integer, Strategy>();
   Configuration conf;
   Repository repository;
   ModelSpeed modelspeed;
   ArrayList<Record> records = new ArrayList<Record>();

   @Override
   protected void setup(Context context) throws IOException, InterruptedException {
      repository = new Repository(context.getConfiguration());
      conf = repository.getConfiguration();
      modelspeed = (ModelSpeed) repository.getFeature(ModelSpeed.class);
   }

   @Override
   public void reduce(RecordedTime key, Iterable<NullWritable> tfs, Context context)
           throws IOException, InterruptedException {
      Record newRecord = modelspeed.newRecord(key.strategy, key.query);
      newRecord.time = key.time;
      records.add(newRecord);
   }

   @Override
   protected void cleanup(Context context) throws IOException, InterruptedException {
      modelspeed.setBufferSize(1000000);
      modelspeed.openWrite();
      for (Record r : records) {
         modelspeed.write(r);
      }
      modelspeed.closeWrite();
   }
}
