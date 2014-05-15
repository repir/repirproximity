RepIRProximity
===================

This package contains implementations of our Cumulative Phrase Expansion model, and for comparison:
- Metzler and Croft (2005) Markov Random Fields for term dependency
- Tao and Zhai (2007) Minimal Distance Proximity measure
- Zhao and Yun (2009) Proximity Language Model

I'm a bit behind on work, I plan to put more elaborate instructions on how to try it later. Meanwhile, the short versions is, you need a Hadoop Cluster and Maven. Download the IREFproximitymodel.jar with its pom.xml. The preferred location is: ~/iref/lib. Within that directory use Maven to download what is needed:

```bash
mvn dependency:copy-dependencies -U -DoutputDirectory=. -DexcludeGroupIds=org.apache.hadoop
```

Then in the IREF repository, there is a map main/resources. Put the submaps with contents (settings, adhoc and tiny) in ~/iref. 

To work with IREF, it is easiest to create a batchfile 'iref' containing:
```bash
export irefdir=~/iref
export CLASSPATH=$irefdir/lib/\*:/usr/lib/hadoop/lib/\*:/usr/lib/hadoop/\*:/etc/hadoop/conf
set -f
java -Xmx2G $*
```

Of course you have to modify if you chose a different location for the files and if hadoop is installed elsewhere. 

To get started, I suggest you use the "tiny" example. Put the enwiki.trec file from the tiny directory on HDFS as input/tiny/enwiki.trec

Look at settings/clustersettings. The iref.lib entries should all be put in iref/lib by maven, check that the filenames are correct. Maybe on your cluster nodes have less memory, you may alter that, but this may not be important until you do big things.

Look at settings/tiny. The HDFS paths should be modified to the location you use. The repository.dir directory must exist, so create that. IREF will create subdirs, but not the main directory.

If all seems ok, you can give it a try:
```bash
iref vu.nl.iref.apps.VocabularyBuilder.BuildVocabulary tiny
iref vu.nl.iref.apps.RepositoryBuilder.BuildRepository tiny
```

If all went well iref created a repository. In its root you'll find tiny.master which is a text file with the configuration that was used to create the repository. It has a repository directory with files for features (per partition if you set partition higher than 1).

You can first query it without MapReduce:
```bash
iref vu.nl.iref.apps.Retrieve.QueryFromArgsNoMR tiny albert einstein
```

There is probably quite a bit of log turned on to the console, but the tail of the output should contain the top-10 documents for the query "albert einstein".

Look in settings/scoremodel. You can either change the retriever.retrievalmodel in this file, or alternatively set it in the command line of a query:
```bash
iref vu.nl.iref.apps.Retrieve.QueryFromArgsNoMR tiny albert einstein retriever.retrievalmodel=ProximityRetrievalModel
```
And you should notice a change in the top-10, with documents that contain "albert einstein" more proximity.

That's all for now. If you try the TREC collections, please note that you have to decompress the old TREC collections yourself (java does not support that compressor). For ClueWeb you can leave the whole collection as is.  
