package io.github.repir.apps.Oracle;

import io.github.repir.Retriever.Query;
import io.github.repir.Retriever.MapReduce.Retriever;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.Stopwords.StopWords;
import io.github.repir.tools.Stemmer.englishStemmer;
import io.github.repir.TestSet.Metric.QueryMetricAP;
import io.github.repir.TestSet.TestSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import io.github.repir.tools.MapReduce.Configuration;
import org.apache.hadoop.mapred.JobPriority;
import io.github.repir.Retriever.MapReduce.QueryIterator;
import io.github.repir.Retriever.MapReduce.QueueIterator;
import io.github.repir.TestSet.ResultSet;
import io.github.repir.tools.Lib.MathTools;

/**
 * Breadth first search to expand a query with a set of (sub) optimal proximity 
 * operators that maximizes mean average precision. From the best query, variations are made that add, remove
 * or modify one expansion phrase. All new variations are then tried out using
 * MapReduce. The results are evaluated using the QRELS. After each cycle, only
 * the phrase(s) with the highest mean average precision are used to seed the
 * next round of variations.
 * <p/>
 * Note, by default a phrase uses it's phrase occurrence statistics to be scored
 * like a unigram. If the phrase occurrence statistic is not present in the
 * Repository, IREF will solve it doing a 2-pass retrieval plan, therefor, 2
 * passes are performed for each cycle.
 * <p/>
 * Will resolve only one topic at a startTime. Parameters: <configfile> <topicid>
 * [alternative query]
 * <p/>
 * @author jeroen
 */
public class RunOracle {

   public static Log log = new Log(RunOracle.class);
   public englishStemmer stemmer = englishStemmer.get();
   public static double wweights[] = {1.0, 0.5, 0.25};
   public static int spans[] = {2, 3, 4, 5, 10, 20, 50, 100, 200, 500};
   public static HashMap<term, term> termvariants = new HashMap<term, term>();
   public static String[][] components;
   public static String[] terms;
   public StopWords stopwords;

   public static void main(String[] args) {
      new RunOracle(args);
   }
   
   public RunOracle(String args[]) {
      Configuration conf = new Configuration(args, "topicid {query}");
      Repository repository = new Repository(conf);
      conf.set("mapred.job.priority", JobPriority.HIGH.toString());
      conf.setBoolean("retriever.removestopwords", false);
      conf.setBoolean("inputformat.canslit", true);
      stopwords = StopWords.get(repository);
      TestSet testset = new TestSet(repository);
      QueryMetricAP ap = new QueryMetricAP();
      ResultSet resultstat = null;
      int topic = repository.configuredInt("topicid", 0);
      double maxmap = 0;
      String maxquery = null;
      if (conf.getStrings("query").length > 0) {
         terms = conf.getStrings("query");
      } else {
         terms = getTerms(testset, topic);
      }
      generateComponents(terms);
      HashSet<ProximityVariant> evaluated = new HashSet<ProximityVariant>();
      HashSet<ProximityVariant> newvariants = new HashSet<ProximityVariant>();
      ArrayList<ProximityVariant> best = new ArrayList<ProximityVariant>();
      ArrayList<ProximityVariant> evaluatedbest = new ArrayList<ProximityVariant>();
      best.add(getOriginal());
      newvariants.add(getOriginal());
      while (best.size() > 0) {
         createVariants(evaluated, newvariants, best, evaluatedbest);
         showbest( evaluatedbest );
         Retriever retriever = new Retriever(repository);
         retriever.addQueue(generateQueries(retriever, newvariants));
         ArrayList<ProximityVariant> newbest = new ArrayList<ProximityVariant>();
         int mincomponents = Integer.MAX_VALUE;
         
         // to read the retrieved results we use an iterator to read the queries
         // as a stream to prevent memory problems when a cycle contained many queries.
         QueueIterator queueiterator = retriever.retrieveQueueIterator();
         for ( QueryIterator queryiterator : queueiterator.next() ) {
            final Query q = queryiterator.readAll(); // a single query will fit fine, so no need to iterate over the documents
            int mutationnumber = q.id;
            q.id = topic;
            if (resultstat == null) 
               resultstat = new ResultSet(ap, testset, q);
            else
               resultstat.setQuery(q);
            double map = resultstat.queryresult[0];
            ProximityVariant m = getVariantByNumber(newvariants, mutationnumber);
            m.score = map;
            log.info("%f %s", map, q.originalquery);
            if (map >= maxmap) {
               maxmap = map;
               maxquery = q.originalquery;
               newbest.add(m);
            }
         }
         purge(newbest, evaluatedbest);
         best.addAll(newbest);
         evaluated.addAll(newvariants);
         newvariants.clear();
      }
      showbest( evaluatedbest );
   }

   public void purge(ArrayList<ProximityVariant> list, ArrayList<ProximityVariant> evaluatedlist) {
      double max = Double.MIN_VALUE;
      double mincomponents = Integer.MAX_VALUE;
      for (ProximityVariant v : list) {
         if (v.score > max) {
             max = v.score;
             mincomponents = MathTools.numberOfSetBits( v.id );
         } else if (max == v.score) {
             mincomponents = Math.min(mincomponents, MathTools.numberOfSetBits( v.id ));
         }
      }
      for (ProximityVariant v : evaluatedlist) {
         if (v.score > max) {
             max = v.score;
             mincomponents = MathTools.numberOfSetBits( v.id );
         } else if (max == v.score) {
             mincomponents = Math.min(mincomponents, MathTools.numberOfSetBits( v.id ));
         }
      }
      Iterator<ProximityVariant> iter = list.iterator();
      while (iter.hasNext()) {
         ProximityVariant v = iter.next();
         if (v.score < max || mincomponents < MathTools.numberOfSetBits( v.id ))
            iter.remove();
      }
   }

   public void showbest( ArrayList<ProximityVariant> list ) {
     double max = Double.MIN_VALUE;
      for (ProximityVariant v : list)
         max = Math.max(max, v.score);
      for (ProximityVariant v : list)
         if (max == v.score)
            log.printf("MAX %6f %s", v.score, v.toQuery());
   }
   
   public ArrayList<Query> generateQueries(Retriever retriever, HashSet<ProximityVariant> newvariants) {
      ArrayList<Query> list = new ArrayList<Query>();
      for (ProximityVariant v : newvariants) {
         Query q = retriever.constructQueryRequest(list.size(), v.toQuery());
         q.setStrategyClassname("RetrievalModel");
         q.addCollectionID();
         list.add(q);
      }
      return list;
   }

   public ProximityVariant getVariantByNumber(HashSet<ProximityVariant> variants, int id) {
      for (ProximityVariant m : variants) {
         if (id-- == 0) {
            return m;
         }
      }
      return null;
   }

   public String[] getTerms(TestSet ts, int topic) {
      String query = ts.filterString(ts.topics.get(topic).query);
      ArrayList<String> usedwords = new ArrayList<String>();
      String terms[] = query.split("\\s+");
      for (String t : terms) {
         if (!stopwords.isStemmedStopWord(stemmer.stem(t.toLowerCase()))) {
            usedwords.add(t);
         }
      }
      return usedwords.toArray(new String[usedwords.size()]);
   }

   public void generateComponents(String terms[]) {
      components = new String[(int) Math.pow(2, terms.length)][];
      for (int i = 1; i < Math.pow(2, terms.length); i++) {
         components[i] = new String[MathTools.numberOfSetBits(i)];
         int termpos = 0;
         for (int j = 0; j < terms.length; j++) {
            if ((i & (int) Math.pow(2, j)) > 0) {
               components[i][termpos++] = terms[j];
            }
         }
         if (MathTools.numberOfSetBits(i) > 1) {
            for (int span : spans) {
               for (double weight : wweights) {
                  term.createTermVariant(i, span, weight);
               }
            }
         } else {
            term.createTermVariant(i, 0, 1.0);
         }
      }
   }

   public ProximityVariant getOriginal() {
      ProximityVariant m = new ProximityVariant();
      for (int i = 0; i < terms.length; i++) {
         if (!stopwords.isStemmedStopWord(stemmer.stem(terms[i].toLowerCase()))) {
            term t = term.createTermVariant((int) Math.pow(2, i), 0, 1.0);
            m.add(t);
         }
      }
      return m;
   }

   public void createVariants(HashSet<ProximityVariant> evaluated, HashSet<ProximityVariant> newvariants, ArrayList<ProximityVariant> best, ArrayList<ProximityVariant> evaluatedbest) {
      ArrayList<ProximityVariant> best1 = new ArrayList<ProximityVariant>( best );
      for (ProximityVariant v : best1) {
         evaluatedbest.add(v);
         best.remove(v);
         for (term t : termvariants.values()) {
            if (components[t.componentid].length > 1) {
               ProximityVariant clone = v.clone();
               clone.add(t);
               if (!evaluated.contains(clone)) {
                  newvariants.add(clone);
               }
            }
         }
         for (int i = 1; i < components.length; i++) {
            if (components[i].length > 1) {
               ProximityVariant clone = v.clone();
               if (clone.terms[i] != null) {
                  clone.removeTerm(i);
                  if (!evaluated.contains(clone)) {
                     newvariants.add(clone);
                  }
               }
            }
         }
         if (newvariants.size() > 10000)
            break;
      }
   }

   class ProximityVariant implements Comparable<ProximityVariant> {

      int id = 0;
      term terms[] = new term[components.length];
      int hashcode = 0;
      double score;

      @Override
      public ProximityVariant clone() {
         ProximityVariant m = new ProximityVariant();
         System.arraycopy(terms, 0, m.terms, 0, terms.length);
         m.id = id;
         return m;
      }

      public void add(term t) {
         if (terms[ t.componentid] == null) {
            id += Math.pow(2, t.componentid);
         }
         terms[ t.componentid] = t;
      }

      public boolean removeTerm(int t) {
         if (terms[t] == null) {
            return true;
         }
         id -= (int) Math.pow(2, t);
         terms[t] = null;
         return (id > 0);
      }

      public String toQuery() {
         StringBuilder sb = new StringBuilder();
         for (term t : terms) {
            if (t != null) {
               if (components[ t.componentid].length == 1) {
                  sb.append(components[ t.componentid][0]);
               } else {
                  sb.append("{ ");
                  for (String term : components[ t.componentid]) {
                     sb.append(term).append(" ");
                  }
                  sb.append("span=").append(t.span);
                  sb.append(" }");
               }
               if (t.weight != 1) {
                  sb.append("#").append(t.weight);
               }
               sb.append(" ");
            }
         }
         return sb.toString();
      }

      @Override
      public boolean equals(Object o) {
         ProximityVariant m = (ProximityVariant) o;
         for (int i = 0; i < terms.length; i++) {
            if (terms[i] != m.terms[i]) {
               return false;
            }
         }
         return true;
      }

      @Override
      public int hashCode() {
         if (hashcode != 0) {
            return hashcode;
         }
         hashcode = 3;
         for (int i = 0; i < terms.length; i++) {
            hashcode = hashcode * 29 + ((terms[i] == null) ? 0 : terms[i].hashCode());
         }
         return hashcode;
      }

      public int compareTo(ProximityVariant o) {
         int comp = MathTools.numberOfSetBits(id) - MathTools.numberOfSetBits(o.id);
         if (comp == 0) {
            double w1 = 0;
            double w2 = 0;
            for (term t : terms)
               w1 += t.weight;
            for (term t : terms)
               w2 += t.weight;
            comp = (w1 < w2)?-1:1;
         }
         return comp;
      }
   }

   static class term {

      int componentid;
      int span;
      Double weight;

      private term() {
      }

      public static term createTermVariant(int id, int span, double weight) {
         term newterm = new term();
         newterm.componentid = id;
         newterm.span = (components[id].length > 1) ? Math.max(components[id].length, span) : 0;
         newterm.weight = (components[id].length > 1) ? weight : 1;
         term existingterm = termvariants.get(newterm);
         if (existingterm == null) {
            termvariants.put(newterm, newterm);
            return newterm;
         }
         return existingterm;
      }

      @Override
      public int hashCode() {
         int hash = 3;
         hash = 29 * hash + this.componentid;
         hash = 29 * hash + this.span;
         hash = 29 * hash + weight.hashCode();
         return hash;
      }

      @Override
      public boolean equals(Object o) {
         term t = (term) o;
         return (componentid == t.componentid && span == t.span && weight.equals(t.weight));
      }
   }
}
