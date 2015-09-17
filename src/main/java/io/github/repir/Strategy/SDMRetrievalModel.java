package io.github.repir.Strategy;

import io.github.repir.Strategy.Operator.SDMOrderedCombinations;
import io.github.repir.Strategy.Operator.SDMUnorderedCombinations;
import io.github.repir.Strategy.Operator.SDMIndependentTerms;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Retriever.Query;
import io.github.repir.Strategy.ScoreFunction.ScoreFunctionDirichletLM;
import io.github.htools.lib.Log;

/**
 * Implementation of the Sequential Dependency as described by
 * Metzler & Croft (2005) in Markov Random Fields for Term Dependency.
 * This retrieval model uses a Dirichlet smoothed LM scoring function.
 * The query is expanded with a pairs of terms that appear consecutive in the
 * query, which are matched both consecutively in a document as well as within a 
 * window of 8 words. The phrase operator is used for these expansion, which will
 * request a pre-pass to collect the required corpus statistics. 
 * <p/>
 * The model consists of a set of individual terms, term pairs that match consecutive words and
 * term pairs that match words within 8 words distance. The weights for the three sets can
 * be controlled by configuring sdm.orderedphrases and sdm.unorderedphrases. 
 * The weight used for the individual terms = 1 - mrf.orderedphrases - mrf.unorderedphrases.
 * <p/>
 * @author jeroen
 */
public class SDMRetrievalModel extends RetrievalModel {

   public static Log log = new Log(SDMRetrievalModel.class);

   public SDMRetrievalModel(Retriever retriever) {
      super(retriever);
   }
   
   @Override
   public String getQueryToRetrieve() {
      query.setScorefunctionClassname(ScoreFunctionDirichletLM.class.getSimpleName());
      return io.github.htools.lib.PrintTools.sprintf("%s:(%s) %s:(%s) %s:(%s)", 
              SDMIndependentTerms.class.getSimpleName(), query.query, 
              SDMOrderedCombinations.class.getSimpleName(), query.query, 
              SDMUnorderedCombinations.class.getSimpleName(), query.query);
   }
   
   
}
