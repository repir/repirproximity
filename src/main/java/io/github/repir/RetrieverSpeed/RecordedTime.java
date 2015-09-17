package io.github.repir.RetrieverSpeed;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.WritableComparable;
import io.github.htools.io.buffer.BufferDelayedWriter;
import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.lib.Log; 

/**
 *
 * @author Jeroen Vuurens
 */
public class RecordedTime implements WritableComparable<RecordedTime> {
  public static Log log = new Log( RecordedTime.class ); 
      public String strategy;
      public int query;
      public double time = -1;
      
      public RecordedTime() {}

   public void write(DataOutput out) throws IOException {
      BufferDelayedWriter writer = new BufferDelayedWriter();
      writer.write( strategy );
      writer.write( query );
      writer.write(time);
      writer.writeBuffer(out);
   }

   public void readFields(DataInput in) throws IOException {
      BufferReaderWriter reader = new BufferReaderWriter(in);
      strategy = reader.readString();
      query = reader.readInt();
      time = reader.readDouble();
   }

   public int compareTo(RecordedTime o) {
      return 0;
   }
}
