package io.github.repir.Strategy;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.RecordCSV;
import io.github.repir.tools.Lib.Log;

public class PhraseFile extends RecordCSV {
   public static Log log = new Log(PhraseFile.class);
   public IntField topic = new IntField("topic");
   public StringField phrase = new StringField("phrase");

   public PhraseFile(Datafile df) {
      super(df);
   }
   
   @Override
   public String createEndFieldTag(Field f) {
      return ",";
   }
}
