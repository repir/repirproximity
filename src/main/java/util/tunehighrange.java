package util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import io.github.repir.Repository.Repository;
import io.github.repir.tools.Lib.ArgsParser;
import io.github.repir.tools.Lib.HDTools;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.MathTools;

public class tunehighrange {

   public static Log log = new Log(tunehighrange.class);
   public static double sensitivity = 0.000001;
   
   public static void main(String[] args) {
      Repository repository = new Repository(HDTools.readConfig(args, "parameter stepsize {settings}"));
      String parameter = repository.getConfigurationString("parameter");
      BigDecimal stepsize = new BigDecimal(repository.getConfigurationString("stepsize"));
      
      combinetune c = combinetune.create(repository);
      HashMap<BigDecimal, Double> paramrange = new HashMap<BigDecimal, Double>();
      HashMap<TreeMap<String, String>, Double> result = c.list( );
      for (Map.Entry<TreeMap<String, String>, Double> e : result.entrySet()) {
         BigDecimal evalue = new BigDecimal(e.getKey().get(parameter) );
         BigDecimal remainder = evalue.remainder(stepsize);
         if ( remainder.compareTo(BigDecimal.ZERO) == 0 ) {
            for (Map.Entry<TreeMap<String, String>, Double> p : result.entrySet()) {
               BigDecimal pvalue = new BigDecimal(p.getKey().get(parameter) );
               if (evalue.compareTo(pvalue) == 0 || 
                   evalue.compareTo(pvalue.subtract(stepsize)) == 0 || 
                   evalue.compareTo(pvalue.add(stepsize)) == 0) {
                  Double d = paramrange.get(pvalue);
                  if (d == null) {
                     paramrange.put(pvalue, e.getValue());
                  } else {
                     paramrange.put(pvalue, e.getValue() + d);
                  }
               }
            }
         }
      }
      Double max = MathTools.MaxDouble(paramrange.values());
      BigDecimal highest = getKey( paramrange, max );
      log.printf("%f", highest.doubleValue());
   }
   
   public static BigDecimal getKey( Map<BigDecimal, Double> map, Double value) {
      for (Map.Entry<BigDecimal, Double> e : map.entrySet()) {
         if (e.getValue().equals(value))
            return e.getKey();
      }
      return null;
   }
}
