package io.github.repir.RetrieverSpeed;

import io.github.repir.Retriever.Reusable.Retriever;
import java.io.IOException;
import java.util.TreeSet;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Mapper;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Query;
import io.github.repir.Retriever.MapReduce.CollectorKey;
import io.github.repir.Retriever.MapReduce.QueryWritable;
import io.github.repir.Retriever.MapReduce.QueryInputSplit;
import io.github.repir.Strategy.Strategy;
import io.github.htools.lib.Log;
import io.github.htools.lib.Profiler;

/**
 * The mapper is generic, and collects data for a query request, using the
 * passed retrieval model, scoring function and query string. The common
 * approach is that each node processes all queries for one index partition. The
 * collected results are reshuffled to one reducer per query where all results
 * for a single query are aggregated.
 * <p/>
 * @author jeroen
 */
public class RetrieverSpeedMap extends Mapper<NullWritable, QueryWritable, RecordedTime, NullWritable> {

   public static Log log = new Log(RetrieverSpeedMap.class);
   public final int tries = 10;
   double times[] = new double[tries];
   
   Context context;
   protected Repository repository;
   protected Retriever retriever;
   QueryInputSplit split;
   protected int partition;

   @Override
   protected void setup(Context context) throws IOException, InterruptedException {
      this.context = context;
      repository = new Repository(context.getConfiguration());
      retriever = new Retriever(repository, context);
      split = (QueryInputSplit) context.getInputSplit();
      partition = split.partition;
   }
   
   public void changeCollectorKey( CollectorKey key ) {
     key.reducer = 0;
   }

   @Override
   public void map(NullWritable inkey, QueryWritable invalue, Context context) throws IOException, InterruptedException {
      Query q = invalue.getQuery(repository);
      Strategy strategy = retriever.prepareStrategy(q, partition);
      retriever.retrieveSegment(strategy); // first dummy run
      for (int i = 0; i < 10; i++) {
         Profiler.startTime("retrieveSegment");
         retriever.retrieveSegment(strategy); // collect results for query from one index partition
         times[i] = Profiler.timePassed("retrieveSegment");
      }
      TreeSet<Double> sorted = new TreeSet<Double>();
      for (double t : times) 
         sorted.add(t);
      double avg = 0;
      for (int i = 0; i < 5; i++)
         sorted.remove(sorted.last());
      for (double t : sorted)
         avg += t;
      avg /= 5;
      RecordedTime t = new RecordedTime();
      t.strategy = q.getStrategyClass();
      t.query = q.getID();
      t.time = avg;
      context.write(t, NullWritable.get());
   }
}
