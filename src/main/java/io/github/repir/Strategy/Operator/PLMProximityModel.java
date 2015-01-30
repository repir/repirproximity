package io.github.repir.Strategy.Operator;

import io.github.repir.Retriever.Document;
import io.github.repir.tools.lib.Log;
import io.github.repir.Repository.DocTF;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import io.github.repir.Strategy.GraphRoot;
import io.github.repir.Strategy.Tools.ProximityPartialSet;
import io.github.repir.Strategy.Tools.ProximitySet.ProximityTerm;

/**
 * An implementation of (Zhao and Yun, 2009), calculating the minimal distance
 * between two co-occurring query terms, and assigning a score based on this
 * distance.
 * <p/>
 * @author jeroen
 */
public class PLMProximityModel extends Operator {

   public static Log log = new Log(PLMProximityModel.class);
   public double para = 1.7;
   public int lambda = 6;
   public double mu = 2500;
   public DocTF doctf;
   protected double secondaryfrequency;
   private int prox[][];
   public ProximityPartialSet termpositions;

   public PLMProximityModel(GraphRoot im) {
      super(im);
      para = im.repository.configuredDouble("plm.para", 1.7);
      lambda = im.repository.configuredInt("plm.lambda", 6);
      //log.info("PLM para %f lambda %d", para, lambda);
   }

   public PLMProximityModel(GraphRoot im, ArrayList<Operator> list) {
      this(im);
      add(combineDuplicates(list));
   }

   @Override
   public void doAnnounce() {
   }

   @Override
   public boolean positional(boolean positional) {
      return super.positional(true);
   }

   /**
    * pass willbescored (most likely true) to its contained terms to let them know
    * to prepare for scoring.
    */
   @Override
   public boolean willBeScored(boolean willbescored) {
      super.positional(willbescored);
      return willbescored;
   }

   @Override
   public void prepareRetrieval() {
      doctf = DocTF.get(repository, "all");
      retrievalmodel.requestFeature(doctf);
      termpositions = new ProximityPartialSet(new ArrayList<PositionalOperator>(containednodes));
   }

   @Override
   public void configureFeatures() {
      for (Operator n : containednodes) {
         n.setweight(1.0 / containednodes.size());
      }
   }

   @Override
   public void process(Document doc) {
      prox = new int[containednodes.size()][containednodes.size()];
      int doclength = doctf.getValue();

      for (int i = 0; i < containednodes.size(); i++) {
         for (int j = i + 1; j < containednodes.size(); j++) {
            prox[i][j] = doclength;
         }
      }
      if (termpositions.hasProximityMatches(doc)) {
         do {
            ProximityTerm first = termpositions.first;
            for (ProximityTerm other : termpositions.proximitytermlist) {
               int i = Math.min(first.sequence, other.sequence);
               int j = Math.max(first.sequence, other.sequence);
               prox[i][j] = Math.min(prox[i][j], other.current - first.current);
            }
         } while (termpositions.next());
      }
      for (int i = 0; i < containednodes.size(); i++) {
         Operator n = containednodes.get(i);
         double sumprox = 0;
         if (n.getFrequency() > 0) {
            for (int j = 0; j < i; j++) {
               sumprox += prox(prox[j][i]);
            }
            for (int j = i + 1; j < containednodes.size(); j++) {
               sumprox += prox(prox[i][j]);
            }
         }
         n.setSecondaryFrequency( sumprox );
      }
   }

   public double prox(int distance) {
      return lambda * Math.pow(para, -distance);
   }

   @Override
   public Operator clone(GraphRoot newmodel) {
      PLMProximityModel f = new PLMProximityModel(newmodel);
      return f;
   }
   
   @Override
   public void setTDFDependencies() {
      for (Operator g : containednodes) {
         g.setTDFDependencies();
      }
   }
}
