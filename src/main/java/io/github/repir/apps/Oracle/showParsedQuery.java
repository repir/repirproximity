package io.github.repir.apps.Oracle;

import io.github.repir.RetrieverMR.RetrieverMR;
import io.github.repir.tools.Lib.HDTools;
import io.github.repir.Repository.Repository;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.DataTypes.Configuration;

/**
 * Gives access to an existing inverted index. The key-part of the index is read
 * into memory, providing access to the posting lists stored on disk.
 */
public class showParsedQuery {

   public static Log log = new Log(showParsedQuery.class);

   public static void main(String args[]) {
      Configuration conf = HDTools.readConfig(args[0]);
      args = io.github.repir.tools.Lib.ArrayTools.subArray(args, 1);
      Repository repository = new Repository(conf);
      RetrieverMR retriever = new RetrieverMR(repository);
      String q = io.github.repir.tools.Lib.StrTools.concat(' ', args);
      //String p = retriever.tokenizeString(retriever.constructQueryRequest(q));
      //log.printf("query  %s\nparsed %s", q, p);
   }
}
