//package io.github.repir.apps.TermDistance;
//
//import io.github.repir.apps.TermDistance.AnalyzeDistanceFeature.Occurrence;
//import io.github.repir.tools.Content.Datafile;
//import io.github.repir.Retriever.Document;
//import io.github.repir.Retriever.Query;
//import io.github.repir.Retriever.MapReduce.Retriever;
//import io.github.repir.Retriever.MapReduce.RetrieverMRInputFormat;
//import io.github.repir.Retriever.MapReduce.QueryIterator;
//import io.github.repir.Retriever.MapReduce.QueueIterator;
//import io.github.repir.tools.Lib.HDTools;
//import io.github.repir.tools.Lib.Log;
//import io.github.repir.Repository.Repository;
//import io.github.repir.TestSet.TestSet;
//import java.util.ArrayList;
//import java.util.HashMap;
//import io.github.repir.tools.DataTypes.Configuration;
//import org.apache.hadoop.conf.Configured;
//import org.apache.hadoop.mapred.JobPriority;
//import org.apache.hadoop.util.Tool;
//
//public class TestDist extends Configured implements Tool {
//
//   public static Log log = new Log(TestDist.class);
//
//   public static void main(String[] args) throws Exception {
//      Configuration conf = HDTools.readConfig( args, "[topicid]" );
//      conf.set("mapreduce.job.priority", JobPriority.NORMAL.toString());
//      System.exit( HDTools.run(conf, new TestDist()));
//   }
//
//   @Override
//   public int run(String[] args) throws Exception {
//      Repository repository = new Repository(getConf());
//      repository.getConfiguration().setInt("retriever.limit", Integer.MAX_VALUE);
//      repository.getConfiguration().set("retriever.strategy", "RetrievalModel");
//      repository.getConfiguration().setInt("mapreduce.task.timeout", 60 * 60 * 1000);
//      int topic = repository.configuredInt("topicid", -1);
//      Retriever retriever = new Retriever(repository);
//      TestSet bm = new TestSet(repository);
//      RetrieverMRInputFormat.setIndex(repository);
//      RetrieverMRInputFormat.setSplitable(true);
//      TestSet testset = new TestSet( repository );
//      ArrayList<Query> queries = testset.getQueries(retriever);
//      ArrayList<Query> queries1 = new ArrayList<Query>();
//      AnalyzeDistanceFeature distanceanalyzer = (AnalyzeDistanceFeature)repository.getFeature(AnalyzeDistanceFeature.class);
//      for (Query q : queries) {
//         if ((topic == -1 || topic == q.id) && q.stemmedquery.contains(" ")) {
//            q.addFeature(distanceanalyzer);
//            q.addFeature(repository.getCollectionIDFeature());
//            q.stemmedquery = io.github.repir.tools.Lib.PrintTools.sprintf("ProximityFeature:(%s)", q.stemmedquery);
//            queries1.add(q);
//         }
//      }
//      QueueIterator qi = retriever.retrieveQueueIterator(queries1);
//      DistanceFile out = new DistanceFile(new Datafile("distance_" + repository.getTestsetName()));
//      out.openWrite();
//      for (QueryIterator q : qi.next()) {
//         HashMap<String, Integer> qrels = bm.getQrels().get(q.query.id);
//         for (Document d : q) {
//            ArrayList<Occurrence> list = (ArrayList<Occurrence>)distanceanalyzer.valueReported(d, 0);
//            //log.info("doc %d occ %d", d.docid, list.size());
//            String docid = d.getString(repository.getCollectionIDFeature());
//            Integer relevant = qrels.get(docid);
//            for (Occurrence o : list) {
//              out.docid.write(docid);
//              out.relevant.write((relevant==null)?0:1);
//              out.topic.write( q.query.id );
//              out.phrase.write(o.phrase);
//              out.span.write(o.span);
//              out.position.write(o.pos);
//            }
//         }
//      }
//      out.closeWrite();
//      qi.close();
//      return 0;
//   }
//}
