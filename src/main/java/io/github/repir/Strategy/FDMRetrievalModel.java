package io.github.repir.Strategy;

import io.github.repir.Strategy.Operator.FDMOrderedCombinations;
import io.github.repir.Strategy.Operator.FDMUnorderedCombinations;
import io.github.repir.Strategy.Operator.FDMIndependentTerms;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.Strategy.Strategy;
import io.github.repir.Strategy.Strategy;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Retriever.Query;
import io.github.repir.Strategy.ScoreFunction.ScoreFunctionDirichletLM;
import io.github.repir.tools.lib.Log;

/**
 * Implementation of Markov Random Field for term dependency by Metzler & Croft.
 * MRF uses a different score function and expands the query with a set of
 * independent terms (FI), a set of phrases that consist of two or more
 * sequential query terms (SD), and a set of phrases that containing all
 * combinations of two or more query terms (FD). This wrapper expands the query
 * to contain those sets. By default the phrase objects will request a pre-pass
 * to collect the required corpus statistics. The weights for the three sets can
 * be controlled by configuring mrf.sd and mrf.fd. The weight used for mrf.fi =
 * 1 - mrf.sd - mrf.fd.
 * <p/>
 * @author jeroen
 */
public class FDMRetrievalModel extends RetrievalModel {

   public static Log log = new Log(FDMRetrievalModel.class);

   public FDMRetrievalModel(Retriever retriever) {
      super(retriever);
   }
   
   @Override
   public String getQueryToRetrieve() {
      query.setScorefunctionClassname(ScoreFunctionDirichletLM.class.getSimpleName());
      return io.github.repir.tools.lib.PrintTools.sprintf("%s:(%s) %s:(%s) %s:(%s)", 
              FDMIndependentTerms.class.getSimpleName(), query.query, 
              FDMOrderedCombinations.class.getSimpleName(), query.query, 
              FDMUnorderedCombinations.class.getSimpleName(), query.query);
   }
}
