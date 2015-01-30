package io.github.repir.apps.TermDistance;

import io.github.repir.tools.io.buffer.BufferDelayedWriter;
import io.github.repir.tools.io.buffer.BufferReaderWriter;
import io.github.repir.Retriever.Document;
import io.github.repir.tools.lib.Log;
import io.github.repir.Repository.DocLiteral;
import io.github.repir.Repository.ReportedUnstoredFeature;
import io.github.repir.Repository.Repository;
import io.github.repir.Strategy.Operator.Operator;
import io.github.repir.Strategy.Operator.CPEProximityModel;
import io.github.repir.Strategy.Operator.CPEProximityModel.combination;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.Strategy.Strategy;
import io.github.repir.Strategy.Tools.MatchSetLength;
import io.github.repir.Strategy.Tools.ProximityOccurrence;
import io.github.repir.tools.io.EOCException;

/**
 * Collects the distance distribution for Phrase occurrences. Typically, this is
 * done on a unigram index, that has no record of co-occurrences, to accurately
 * obtain the distance distribution of a single FeaturePhraseOld used for query
 * reformulation or analysis.
 * <p/>
 * @author jeroen
 */
public class AnalyzeDistanceFeature extends ReportedUnstoredFeature<ArrayList<AnalyzeDistanceFeature.Occurrence>> {

   public static Log log = new Log(AnalyzeDistanceFeature.class);
   CPEProximityModel proximitymodel;
   DocLiteral docid;
   public TreeMap<String, ArrayList<Occurrence>> distances = new TreeMap<String, ArrayList<Occurrence>>();

   public AnalyzeDistanceFeature(Repository repository) {
      super(repository);
   }

   @Override
   public void prepareRetrieval(Strategy strategy) {
      super.prepareRetrieval(strategy);
      RetrievalModel rm = (RetrievalModel)strategy;
      for (Operator n : rm.root.containednodes) {
         if (n instanceof CPEProximityModel) {
            proximitymodel = (CPEProximityModel) n;
         }
      }
      //log.info("preparetrieval %s", proximitymodel);
   }

   /**
    * Adds the span of occurring phrases to a distance map.
    * <p/>
    * @param d
    */
   @Override
   public void report(Document d, int reportid) {
      //log.info("report doc %d", d.docid);
      //FeatureValues score = proximitymodel.process(d);
      ArrayList<Occurrence> occurrences = new ArrayList<Occurrence>();
      if (proximitymodel.occurrences != null) {
         for (Map.Entry<Long, MatchSetLength> entry : proximitymodel.occurrences.entrySet()) {
            long phraseid = entry.getKey();
            combination phrase = proximitymodel.validcombinations.get(phraseid);
            for (ProximityOccurrence m : entry.getValue()) {
               Occurrence occ = new Occurrence();
               occ.phrase = phrase.toString();
               occ.span = m.span;
               occ.pos = m.pos;
               occurrences.add(occ);
            }
         }
      }
      d.reportdata[reportid] = occurrences;
   }

   @Override
   public void decode(Document d, int reportid) {
      try {
         ArrayList<Occurrence> list = new ArrayList<Occurrence>();
         BufferReaderWriter reader = new BufferReaderWriter((byte[]) d.reportdata[reportid]);
         int size = reader.readCInt();
         for (int i = 0; i < size; i++) {
            Occurrence o = new Occurrence();
            o.phrase = reader.readString();
            o.span = reader.readCInt();
            o.pos = reader.readCInt();
            list.add(o);
         }
         d.reportdata[reportid] = list;
         //log.info("decode doc %d reportid %d occ %d ist %d", d.docid, reportid, size, ((ArrayList<Occurrence>)d.reportdata[reportid]).size());
      } catch (EOCException ex) {
         log.fatalexception(ex, "valueReported(%s)", d);
      }
   }

   @Override
   public void encode(Document d, int reportid) {
      ArrayList<Occurrence> list = (ArrayList<Occurrence>) d.reportdata[reportid];
      //log.info("encode doc %d reportid %d occ %d", d.docid, reportid, list.size());
      BufferDelayedWriter bdw = new BufferDelayedWriter();
      bdw.writeC(list.size());
      for (Occurrence o : list) {
         bdw.write(o.phrase);
         bdw.writeC(o.span);
         bdw.writeC(o.pos);
      }
      d.reportdata[reportid] = bdw.getBytes();
   }

   @Override
   public ArrayList<Occurrence> valueReported(Document doc, int reportid) {
      return (ArrayList<Occurrence>) doc.reportdata[ reportid];
   }

   @Override
   public String getLabel() {
      return getClass().getSimpleName();
   }

   public static class Occurrence {

      public int span;
      public int pos;
      public String phrase;
   }
}
