package io.github.repir.apps.Retrieve;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeSet;
import io.github.repir.Repository.ModelParameters;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Query;
import io.github.repir.RetrieverMR.RetrieverMRInputFormat;
import io.github.repir.TestSet.QueryMetricAP;
import io.github.repir.TestSet.ResultSet;

/**
 *
 * @author Jeroen Vuurens
 */
public class SolutionFold5 extends Solution5 {

   int fold;


   public SolutionFold5(Repository repository, int fold, final Tuner tuner) {
      super(repository, tuner);
      this.fold = fold;
      TreeSet<Integer> topics = new TreeSet<Integer>(testset.topics.keySet());
      int foldsize = 5;
      topicstart = topics.first() + fold * foldsize;
      topicend = topics.first() + (fold + 1) * foldsize;
   }
}
