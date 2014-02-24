package io.github.repir.Strategy;

import io.github.repir.Strategy.Term;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.Strategy.GraphNode;
import io.github.repir.Strategy.Tools.StopWords;
import io.github.repir.Strategy.Tools.ScoreFunctionKLD;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import io.github.repir.Strategy.Collector.CollectorDocument;
import io.github.repir.Repository.DocForward;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.TermString;
import io.github.repir.Repository.TermTF;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Query;
import io.github.repir.Retriever.Retriever;
import io.github.repir.tools.Lib.Log;

public class RetrievalModelSMM extends RetrievalModel {

   public static Log log = new Log(RetrievalModelSMM.class);
   public int fbmaxdocs;
   public double lambda;
   public double alpha;
   public double mu;
   public boolean fbstopwords;
   public int fbterms;

   public RetrievalModelSMM(Retriever retriever) {
      super(retriever);
      lambda = repository.getConfiguration().getFloat("rm3.lambda", 0.95f);
      alpha = repository.getConfiguration().getFloat("rm3.alpha", 0.5f);
      fbmaxdocs = repository.getConfiguration().getInt("rm3.fbdocs", 10);
      fbstopwords = repository.getConfiguration().getBoolean("rm3.fbstopwords", false);
      fbterms = repository.getConfiguration().getInt("rm3.fbterms", 1000);
      repository.getConfiguration().setInt("kld.mu", repository.getConfiguration().getInt("rm3.mu", 2500));
   }
   
   @Override
   public String getQueryToRetrieve() {
      query.documentlimit = fbmaxdocs;
      query.setScorefunctionClass(ScoreFunctionKLD.class.getSimpleName());
      return super.getQueryToRetrieve();
   }

   @Override
   public Query finishReduceTask() {
      log.info("cascade()");
      //root.prepareTask();
      Query result = query;
      // fill fbterm with the words in the top-fbmaxdocs documents
      FBModel fbterm = new FBModel(this, retriever, fbmaxdocs, fbstopwords);
      FBModel fbmax = null;

      // EM to estimate p based on feedback
      for (int i = 0; i < 20; i++) {
         fbterm.EM(lambda);
         if (fbmax == null || fbmax.score < fbterm.score) {
            //log.info("new max %f", fbterm.collect);
            fbmax = fbterm.clone();
         }
      }
      for (GraphNode f : root.containedfeatures) {
         if (f instanceof Term) {
            Term ft = (Term) f;
            if (ft.exists()) {
               T t = fbterm.get(ft.termid);
               log.info("existing term %s mle %f", t.term, t.p);
            }
         }
      }


      root.print();

      double sumoldtermweight = root.containedfeatures.size();

      // addQueue fbmaxdocs terms to root
      TreeSet<T> sorted = new TreeSet<T>(fbmax.values());
      int expandterms = 0;
      TreeSet<T> newterms = new TreeSet<T>();
      for (T term : sorted) {
         // addQueue to querymodel: existing terms and terms above the cutoff point
         Term e = null;
         Term n = root.getTerm(term.term);
         GraphNode f = root.find(n);
         if (f != null) {
            newterms.add(new T(term.termid, term.term, (1 - alpha) * f.featurevalues.queryweight / sumoldtermweight + alpha * term.p));
         } else if (term.p >= 0.001 && expandterms++ < fbterms) {
            newterms.add(new T(term.termid, term.term, alpha * term.p));
         }
      }
      double sumweight = 0;
      for (T t : newterms) {
         sumweight += t.p;
      }
      for (T t : newterms) {
         t.p /= sumweight;
      }
      StringBuilder sb = new StringBuilder();
      for (T t : newterms) {
         sb.append(t.term).append("#").append(t.p).append(" ");
      }
      result.stemmedquery = sb.toString();
      result.setStrategyClass("RetrievalModel");
      result.id = query.id;
      //result.performLowercasing = false;
      //result.performStemming = false;
      result.removeStopwords = false;
      //log.info("cascade() %s %d", result.query, result.documentlimit);
      this.query = result;
      return result;
   }

   static class FBModel extends HashMap<Integer, T> {

      double score;

      private FBModel() {
      }

      public FBModel(RetrievalModel rm, Retriever retriever, int fb, boolean fbstopwords) {
         Repository repository = retriever.repository;
         DocForward all = (DocForward) repository.getFeature("DocForward:all");
         TermString termstring = (TermString) repository.getFeature("TermString");
         termstring.openRead();
         TermTF termtf = (TermTF) repository.getFeature("TermTF");
         termtf.loadMem();
         for (GraphNode f : rm.root.containedfeatures) {
            if (f instanceof Term) {
               Term ft = (Term) f;
               //long tf = termtf.readValue(ft.term.termid);
               if (ft.exists()) {
                  T t = new T(ft.termid, ft.stemmedterm, ft.featurevalues.corpusfrequency / (double)repository.getCorpusTF());
                  put(ft.termid, t);
                  //log.info("existing term %s mle %f", t.term, t.termcorpusmle); 
               }
            }
         }
         int doccount = 0;
         int showterm = 0;
         for (Document d : ((CollectorDocument) rm.collectors.get(0)).getRetrievedDocs()) {
            if (doccount++ >= fb) {
               break;
            }
            all.setPartition(d.partition);
            all.setBufferSize(10000);
            all.openRead();
            //log.info("%5d#%3d %f %s\n%s", d.docid, d.partition, d.score,
            //          d.getLiteral("DocLiteral:collectionid"), d.report);
            all.read(d);
            int tokens[] = all.getValue();
            //log.info("fb doc %d %s", d.docid, Lib.ArrayTools.toString(tokens));
            for (int termid : tokens) {
               T t = get(termid);
               if (t == null) {
                  long tf = termtf.readValue(termid);
                  //log.info("aap");
                  String term = termstring.readValue(termid);
                  //log.info("getValue term %d %s", termid, term);
                  if ((fbstopwords || !StopWords.get(repository).isStemmedStopWord(term))) {
                     t = new T(termid, term, tf / (double) retriever.getRepository().getCorpusTF());
                     t.tf = 1;
                     put(termid, t);
                  }
               } else {
                  t.tf++;
                  //log.info("existing term %s tf %d", t.term, t.tf);
               }
            }
            //log.crash();
            //log.info("done");
         }
      }

      @Override
      public FBModel clone() {
         FBModel fb = new FBModel();
         for (Map.Entry<Integer, T> entry : entrySet()) {
            T t = new T(entry.getValue().termid, entry.getValue().term, entry.getValue().termcorpusmle);
            t.p = entry.getValue().p;
            t.weight = entry.getValue().weight;
            fb.put(t.termid, t);
         }
         fb.score = score;
         return fb;
      }

      public void EM(double lambda) {
         double sum = 0;
         for (T term : values()) {
            term.p = term.tf;//Lib.Random.getDouble();      
            sum += term.p;
         }
         for (T term : values()) {
            term.p /= sum;
         }
         double diff = 1;
         while (diff > 0.001) {
            diff = 0;
            // E-step
            for (T term : values()) {
               double newweight = (1 - lambda) * term.p / ((1 - lambda) * term.p + lambda * term.termcorpusmle);
               diff += Math.abs(term.weight - newweight);
               term.weight = newweight;
            }
            // M-step
            double sump = 0;
            for (T term : values()) {
               term.p = term.tf * term.weight;
               sump += term.p;
            }
            for (T term : values()) {
               term.p /= sump;
            }
         }
      }
   }

   static class T implements Comparable<T> {

      int termid;
      String term;
      double termcorpusmle;
      int tf;
      double weight;
      double p;
      double querymle;

      public T(int termid, String term, double corpusmle) {
         this.termid = termid;
         this.term = term;
         termcorpusmle = p = corpusmle;
      }

      @Override
      public int compareTo(T o) {
         return (p < o.p) ? 1 : -1;
      }
   }

   static class NEWTERM {

      int termid;
      String term;
      double p;

      public NEWTERM(int termid, String term, double p) {
         this.termid = termid;
         this.term = term;
         this.p = p;
         term.hashCode();
      }

      public int hashCode() {
         return termid;
      }

      public boolean equals(Object o) {
         return (o instanceof NEWTERM && ((NEWTERM) o).termid == termid);
      }
   }
}
