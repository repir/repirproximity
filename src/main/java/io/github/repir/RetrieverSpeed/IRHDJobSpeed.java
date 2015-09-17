package io.github.repir.RetrieverSpeed;

import io.github.repir.Retriever.MapReduce.RetrieverJob;
import io.github.repir.Retriever.MapReduce.QueryInputFormat;
import io.github.repir.Retriever.Retriever;
import io.github.htools.lib.Log;
import java.io.IOException;
import java.util.Collection;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;

public class IRHDJobSpeed extends RetrieverJob {

   public static Log log = new Log(RetrieverJob.class);
   QueryInputFormat inputformat;

   public IRHDJobSpeed(Retriever retriever) throws IOException {
      super(retriever);
      inputformat = new QueryInputFormat(retriever.repository);
      setJobName("M1Retriever " + retriever.repository.configurationName());
   }
   
   public IRHDJobSpeed(Retriever retriever, String path) throws IOException {
      this(retriever);
      if (path != null) {
         this.path = path;
      }
   }

   @Override
   public void setJob() {
      setJarByClass(RetrieverSpeedMap.class);
      setMapOutputKeyClass(RecordedTime.class);
      setMapOutputValueClass(NullWritable.class);
      setOutputKeyClass(NullWritable.class);
      setOutputValueClass(NullWritable.class);
      setMapperClass(RetrieverSpeedMap.class);
      setReducerClass(RetrieverSpeedReduce.class);
      setInputFormatClass(inputformat.getClass());
      setOutputFormatClass(NullOutputFormat.class);
   }
   
   @Override
   public void setReducers(Collection<String> reducers) {
      setNumReduceTasks( 1 );
   }
}
