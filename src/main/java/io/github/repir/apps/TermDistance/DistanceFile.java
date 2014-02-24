package io.github.repir.apps.TermDistance;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.RecordCSV;
import io.github.repir.tools.Lib.Log;

public class DistanceFile extends RecordCSV {
   public static Log log = new Log(DistanceFile.class);
   public StringField docid = new StringField("docid");
   public IntField relevant = new IntField("relevant");
   public IntField topic = new IntField("query");
   public StringField phrase = new StringField("phrase");
   public IntField span = new IntField("span");
   public IntField position = new IntField("position");

   public DistanceFile(Datafile df) {
      super(df);
   }
   
   @Override
   public String createEndFieldTag(Field f) {
      return ",";
   }
}
