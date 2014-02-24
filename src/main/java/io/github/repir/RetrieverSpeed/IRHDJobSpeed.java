package io.github.repir.RetrieverSpeed;

import io.github.repir.RetrieverMR.IRHDJob;
import io.github.repir.RetrieverMR.RetrieverMRInputFormat;
import io.github.repir.Retriever.Retriever;
import io.github.repir.tools.Lib.Log;
import java.io.IOException;
import java.util.Collection;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;

public class IRHDJobSpeed extends IRHDJob {

   public static Log log = new Log(IRHDJob.class);

   public IRHDJobSpeed(Retriever retriever) throws IOException {
      super(retriever);
      setJobName("M1Retriever " + retriever.repository.getConfigurationName());
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
      setInputFormatClass(RetrieverMRInputFormat.class);
      setOutputFormatClass(NullOutputFormat.class);
   }
   
   @Override
   public void setReducers(Collection<String> reducers) {
      setNumReduceTasks( 1 );
   }
}
