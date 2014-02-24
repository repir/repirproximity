package io.github.repir.Strategy;

import io.github.repir.Strategy.GraphNode;
import io.github.repir.Strategy.FeatureProximity;
import io.github.repir.Strategy.GraphRoot;
import java.util.ArrayList;
import io.github.repir.tools.Lib.Log;

/**
 * Parameterized variant of CPEFeature
 * <p/>
 * @author jeroen
 */
public class CPESensitivityFeature extends CPEFeature {

   public static Log log = new Log(FeatureProximity.class);
   private double decayrate = -1;
   private double proxweight = -1;

   public CPESensitivityFeature(GraphRoot im, ArrayList<GraphNode> list) {
      super(im, list);
   }

   public void setdecayrate( Double value ) {
      decayrate = value;
   }
   
   public void setproxweight( Double value ) {
      proxweight = value;
   }
   
   @Override
   public void configureFeatures() {
      super.configureFeatures();
      if (decayrate == -1)
         decayrate = repository.getConfigurationDouble("proximity.decayrate", 1.0);
      if (proxweight == -1)
         proxweight = repository.getConfigurationDouble("proximity.proxweight", 1.0);
   }

   protected double relevance(int terms, double span) {
      if (span <= 1) {
         return 1;
      } else {
         return proxweight * (terms - 1) / (terms - 1 + (span - terms) * decayrate);
      }
   }


}
