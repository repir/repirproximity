package io.github.repir.apps.Oracle;

import io.github.repir.RetrieverMR.RetrieverMR;
import io.github.repir.tools.Lib.HDTools;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.TermDF;
import io.github.repir.Repository.TermString;
import io.github.repir.Repository.TermTF;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.DataTypes.Configuration;

/**
 * Shows the top-10000 terms in the vocabulary
 */
public class ShowTopTerms {

   public static Log log = new Log(ShowTopTerms.class);

   public static void main(String args[]) {
      Configuration conf = HDTools.readConfig(args[0]);
      Repository repository = new Repository(conf);
      RetrieverMR retriever = new RetrieverMR(repository);
      TermString termstring = (TermString) repository.getFeature("TermString");
      TermDF df = (TermDF) repository.getFeature("TermDF");
      TermTF tf = (TermTF) repository.getFeature("TermTF");
      for (int i = 0; i < 10000; i++) {
         String s = termstring.readValue(i);
         log.info("Term %d is %s df %d tf %d", i,
                 s,
                 df.readValue(i),
                 tf.readValue(i));
         log.info("aap");
      }
   }
}
