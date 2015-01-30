package io.github.repir.RetrieverSpeed;

import io.github.repir.Retriever.PostingIteratorReusable;
import java.io.IOException;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.MapReduce.Retriever;
import io.github.repir.tools.lib.Log;

/**
 * Obsolete
 * <p/>
 */
public class RetrieverSpeed extends Retriever {

   public static Log log = new Log(RetrieverSpeed.class);
   protected PostingIteratorReusable postingiterator;
   

   public RetrieverSpeed(Repository repository) {
      super(repository);
   }

    public RetrieverSpeed(Repository repository, org.apache.hadoop.mapreduce.Mapper.Context mappercontext) {
      super(repository, mappercontext);
   }

   public RetrieverSpeed(Repository repository, org.apache.hadoop.mapreduce.Reducer.Context reducercontext) {
      super(repository, reducercontext);
   }
   
   @Override
   public IRHDJobSpeed createJob(String path) throws IOException {
      return new IRHDJobSpeed( this, path );  
   }
}
