package io.github.repir.Strategy.Operator;

import io.github.repir.Retriever.Document;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Repository.DocTF;
import io.github.repir.Strategy.GraphRoot;
import java.util.ArrayList;
import io.github.repir.Strategy.Tools.ProximityPartialSet;
import io.github.repir.Strategy.Tools.ProximitySet.ProximityTerm;

/**
 * An implementation of (Tao and Zha, 2007), calculating the minimal distance
 * between two co-occurring query terms, and assigning a score based on this
 * distance.
 * <p/>
 * @author jeroen
 */
public class MinDistProximityModel extends Operator {

   public static Log log = new Log(MinDistProximityModel.class);
   public DocTF doctf;
   public ProximityPartialSet termpositions;

   public MinDistProximityModel(GraphRoot im) {
      super(im);
   }

   public MinDistProximityModel(GraphRoot im, ArrayList<Operator> list) {
      super(im);
      this.add(combineDuplicates(list));
   }

   @Override
   public void doAnnounce() {
      super.announce(ANNOUNCEKEY.SCORABLE, this);
   }

   @Override
   public boolean positional(boolean positional) {
      return super.positional(true);
   }

   @Override
   public void prepareRetrieval() {
      doctf = (DocTF) root.retrievalmodel.requestFeature(DocTF.class, "all");
      termpositions = new ProximityPartialSet(new ArrayList<PositionalOperator>(containednodes));
   }

   /**
    *
    * @return true if only one Term is contained, resulting in the
    * MinDistFeature to be removed from the Tree.
    */
   @Override
   public boolean expand() {
      if (containednodes.size() < 2) {
         containednodes.clear();
         return true;
      }
      return false;
   }

   @Override
   public void configureFeatures() {
      setDocumentPrior(0);
   }

   @Override
   public void announce(ANNOUNCEKEY key, Operator node) {
      if (key != ANNOUNCEKEY.SCORABLE) {
         super.announce(key, node);
      }
   }

   @Override
   public void process(Document doc) {
      int mindist = doctf.getValue(); // if no co-occurrences, mindist equals doc length
      if (termpositions.hasProximityMatches(doc)) {
         do {
            ProximityTerm first = termpositions.first;
            ProximityTerm next = termpositions.proximitytermlist.first();
            mindist = Math.min(mindist, next.current - first.current);
         } while (termpositions.next());
      }
      secondaryfrequency = mindist;
   }

   @Override
   public Operator clone(GraphRoot newmodel) {
      MinDistProximityModel f = new MinDistProximityModel(newmodel);
      return f;
   }
}
