package io.github.repir.apps.Oracle;

import io.github.repir.TestSet.TestSet;
import io.github.repir.tools.Content.FSFile;
import io.github.repir.tools.Lib.HDTools;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Document;
import io.github.repir.RetrieverMR.RetrieverMR;
import io.github.repir.RetrieverMR.RetrieverMRInputFormat;
import io.github.repir.RetrieverMR.QueryIterator;
import io.github.repir.RetrieverMR.QueueIterator;
import io.github.repir.tools.Lib.Log;
import java.util.HashMap;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.mapred.JobPriority;
import org.apache.hadoop.util.Tool;
import io.github.repir.tools.DataTypes.Configuration;

public class TestCopolas extends Configured implements Tool {

   public static Log log = new Log(TestCopolas.class);

   public static void main(String[] args) throws Exception {
      Configuration conf = HDTools.readConfig("trec4cr");
      conf.set("mapred.job.priority", JobPriority.HIGH.toString());
      System.exit(HDTools.run(conf, new TestCopolas()));
   }

   @Override
   public int run(String[] args) throws Exception {
      Repository repository = new Repository(getConf());
      repository.getConfiguration().setInt("retriever.limit", Integer.MAX_VALUE);
      repository.getConfiguration().set("retriever.strategy", "RetrievalModelC1");
      repository.getConfiguration().setInt("mapred.task.timeout", 60 * 60 * 1000);
      RetrieverMR retriever = new RetrieverMR(repository);
      TestSet bm = new TestSet(repository);
      RetrieverMRInputFormat.setIndex(repository);
      RetrieverMRInputFormat.setSplitable(true);
      TestSet testset = new TestSet( repository );
      QueueIterator qi = retriever.retrieveQueueIterator(testset.getQueries(retriever));
      FSFile out = new FSFile("copola");
      for (QueryIterator q : qi.next()) {
         HashMap<String, Integer> qrels = bm.getQrels().get(q.query.id);
         for (Document d : q) {
            String r[] = d.report.toString().split(" ");
            d.report = new StringBuilder();
            Integer qrel = qrels.get(r[1]);
            if (qrel == null) {
               qrel = 0;
            }
            d.report.append(r[0]).append(" ").append(r[1]).append(" ").append(qrel);
            for (int s = 2; s < r.length; s++) {
               d.report.append(" ").append(r[s]);
            }
            d.report.append("\n");
            out.print(d.report.toString());
         }
      }
      out.close();
      qi.close();
      return 0;
   }
}
