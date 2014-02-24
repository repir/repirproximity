package io.github.repir.Strategy;

import io.github.repir.Strategy.Term;
import io.github.repir.Strategy.GraphNode;
import io.github.repir.Strategy.GraphRoot;
import io.github.repir.Retriever.Document;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Repository.DocTF;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.TreeSet;
import io.github.repir.Repository.TermDocumentFeature;
import io.github.repir.Strategy.Tools.ProximityPartialSet;
import io.github.repir.Strategy.Tools.ProximityTermList;
import io.github.repir.Strategy.Tools.ProximitySet;
import io.github.repir.Strategy.Tools.ProximitySet.ProximityTerm;

/**
 * An implementation of (Zhao and Yun, 2009), calculating the minimal distance
 * between two co-occurring query terms, and assigning a score based on this
 * distance.
 * <p/>
 * @author jeroen
 */
public class PLMFeature extends GraphNode {

   public static Log log = new Log(PLMFeature.class);
   public double para = 1.7;
   public int lambda = 6;
   public double mu = 2500;
   public DocTF doctf;
   private int prox[][];
   public ProximityPartialSet termpositions;

   public PLMFeature(GraphRoot im) {
      super(im);
      para = im.repository.getConfigurationDouble("plm.para", 1.7);
      lambda = im.repository.getConfigurationInt("plm.lambda", 6);
      //log.info("PLM para %f lambda %d", para, lambda);
   }

   public PLMFeature(GraphRoot im, ArrayList<GraphNode> list) {
      this(im);
      OUTER:
      for (GraphNode f : list) {
         if (f instanceof Term) {
            for (GraphNode ff : containedfeatures) {
               if (ff instanceof Term && f.getClass().equals(ff.getClass())
                       && ((Term) f).stemmedterm.equals(((Term) ff).stemmedterm)) {
                  continue OUTER;
               }
            }
            add(f);
         }
      }
   }

   public static PLMFeature create(GraphRoot im) {
      PLMFeature f = null;
      try {
         Constructor cons = im.phraseclass.getDeclaredConstructor(GraphRoot.class);
         f = (PLMFeature) cons.newInstance(im);
      } catch (Exception ex) {
         log.fatalexception(ex, "create( %s )", im);
      }
      return f;
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
      doctf = (DocTF) root.retrievalmodel.requestFeature("DocTF:all");
      termpositions = new ProximityPartialSet(containedfeatures);
   }

   @Override
   public void configureFeatures() {
      for (GraphNode n : containedfeatures) {
         n.setweight(1.0 / containedfeatures.size());
      }
   }

   @Override
   public void process(Document doc) {
      prox = new int[containedfeatures.size()][containedfeatures.size()];
      int doclength = doctf.getValue();

      for (int i = 0; i < containedfeatures.size(); i++) {
         for (int j = i + 1; j < containedfeatures.size(); j++) {
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
      for (int i = 0; i < containedfeatures.size(); i++) {
         GraphNode n = containedfeatures.get(i);
         double sumprox = 0;
         if (n.featurevalues.frequency > 0) {
            for (int j = 0; j < i; j++) {
               sumprox += prox(prox[j][i]);
            }
            for (int j = i + 1; j < containedfeatures.size(); j++) {
               sumprox += prox(prox[i][j]);
            }
         }
         n.featurevalues.secondaryfrequency = sumprox;
      }
   }

   public double prox(int distance) {
      return lambda * Math.pow(para, -distance);
   }

   @Override
   public GraphNode clone(GraphRoot newmodel) {
      PLMFeature f = new PLMFeature(newmodel);
      featurevalues.copy(f.featurevalues);
      return f;
   }
   
   @Override
   public void setTDFDependencies() {
      for (GraphNode g : containedfeatures) {
         g.setTDFDependencies();
      }
   }
}
