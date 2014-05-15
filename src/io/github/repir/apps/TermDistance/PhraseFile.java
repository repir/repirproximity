//package io.github.repir.apps.TermDistance;
//import io.github.repir.tools.Content.Datafile;
//import io.github.repir.tools.Content.StructuredTextFile;
//import io.github.repir.tools.Lib.Log;
//
//public class PhraseFile extends StructuredTextFile {
//   public static Log log = new Log(PhraseFile.class);
//   public IntField topic = this.("topic");
//   public StringField phrase = new StringField("phrase");
//
//   public PhraseFile(Datafile df) {
//      super(df);
//   }
//   
//   @Override
//   public String createEndFieldTag(Field f) {
//      return ",";
//   }
//}
