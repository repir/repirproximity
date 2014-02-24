package io.github.repir.Strategy;

import io.github.repir.Strategy.Term;
import io.github.repir.Strategy.GraphNode;
import io.github.repir.Strategy.GraphRoot;
import io.github.repir.Retriever.Document;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Repository.DocTF;
import java.util.ArrayList;
import java.util.TreeSet;
import io.github.repir.Strategy.Tools.ProximityPartialSet;
import io.github.repir.Strategy.Tools.ProximityTermList;
import io.github.repir.Strategy.Tools.ProximitySet;
import io.github.repir.Strategy.Tools.ProximitySet.ProximityTerm;

/**
 * An implementation of (Tao and Zha, 2007), calculating the minimal distance
 * between two co-occurring query terms, and assigning a score based on this
 * distance.
 * <p/>
 * @author jeroen
 */
public class MinDistFeature extends GraphNode {

   public static Log log = new Log(MinDistFeature.class);
   public DocTF doctf;
   public ProximityPartialSet termpositions;

   public MinDistFeature(GraphRoot im) {
      super(im);
   }

   public MinDistFeature(GraphRoot im, ArrayList<GraphNode> list) {
      this(im);
      for (GraphNode f : list) {
         if (f instanceof Term) {
            for (GraphNode ff : containedfeatures) {
               if (ff instanceof Term && f.getClass().equals(ff.getClass())
                       && ((Term) f).stemmedterm.equals(((Term) ff).stemmedterm)) {
                  f = null;
                  break;
               }
            }
            if (f != null) {
               add(f);
            }
         }
      }
   }

   public void doAnnounce() {
      super.announce( ANNOUNCEKEY.SCORABLE, this );
   }

   @Override
   public boolean positional( boolean positional ) {
      return super.positional( true );
   }
   
   @Override
   public void prepareRetrieval() {
      doctf = (DocTF) root.retrievalmodel.requestFeature("DocTF:all");
      termpositions = new ProximityPartialSet(containedfeatures);
   }
   
   /**
    * 
    * @return true if only one Term is contained, resulting in the MinDistFeature
    * to be removed from the Tree.
    */
   @Override
   public boolean expand() {
      if (containedfeatures.size() < 2) {
         containedfeatures.clear();
         return true;
      }
      return false;
   }
   
   @Override
   public void configureFeatures() {
      featurevalues.documentprior = 0;
   }

   @Override
   public void announce( ANNOUNCEKEY key, GraphNode node ) {
      if (key != ANNOUNCEKEY.SCORABLE)
         super.announce(key, node);
   }
   
   @Override
   public void process(Document doc) {
      int mindist = doctf.getValue();
      if (termpositions.hasProximityMatches(doc)) {
         do {
         ProximityTerm first = termpositions.first;
         ProximityTerm next = termpositions.proximitytermlist.first();
         mindist = Math.min(mindist, next.current - first.current );
         } while ( termpositions.next());
      }
      featurevalues.secondaryfrequency = mindist;
   }

   @Override
   public GraphNode clone(GraphRoot newmodel) {
      MinDistFeature f = new MinDistFeature(newmodel);
      featurevalues.copy(f.featurevalues);
      return f;
   }
}
