package io.github.repir.apps.Retrieve;

import io.github.repir.tools.Lib.HDTools;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Query;
import io.github.repir.RetrieverMR.RetrieverMRInputFormat;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.DataTypes.Configuration;
import io.github.repir.RetrieverSpeed.RetrieverSpeed;
import io.github.repir.TestSet.TestSet;

/**
 * Retrieve all topics from the RunTestSet, and store in an output file.
 * arguments: <configfile> <outputfileextension>
 *
 * @author jeroen
 */
public class RunSpeedTest {

   public static Log log = new Log(RunSpeedTest.class);

   public static void main(String[] args) throws Exception {
      Configuration conf = HDTools.readConfig(args, "{strategy}");
      HDTools.setPriorityHigh(conf);
      Repository repository = new Repository(conf);
      RetrieverSpeed retriever = new RetrieverSpeed(repository);
      TestSet testset = new TestSet(repository);
      for (String s : repository.getConfigurationSubStrings("strategy")) {
         for (Query q : testset.getQueries(retriever)) {
            Query n = new Query(q);
            n.setStrategyClass(s);
            retriever.addQueue(n);
         }
      }
      log.info("queue size %d", retriever.queue.size());
      RetrieverMRInputFormat.setSplitable(true);
      RetrieverMRInputFormat.setIndex(repository);
      retriever.doJob(retriever.queue);
      log.info("%s started", conf.get("iref.conf"));
   }


}
