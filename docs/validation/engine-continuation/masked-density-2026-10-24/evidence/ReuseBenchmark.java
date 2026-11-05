import com.shnewbs.deepprimal.core.density.*;
import java.lang.management.*;
import java.nio.file.*;
import java.util.*;
public class ReuseBenchmark {
 static volatile long sink;
 public static void main(String[] args)throws Exception {
  int fork=Integer.parseInt(args[0]);var graph=TerrainShapeV8.definition(false).compile(20261024L);
  var bean=(com.sun.management.ThreadMXBean)ManagementFactory.getThreadMXBean();bean.setThreadAllocatedMemoryEnabled(true);
  StringBuilder rows=new StringBuilder("iteration,mode,nanos,allocated_bytes,checksum\n");
  for(int iteration=-4;iteration<8;iteration++)for(int order=0;order<1;order++){
   boolean memo=true;int ox=74131+iteration*32,oz=-71931+iteration*16;
   long beforeBytes=bean.getThreadAllocatedBytes(Thread.currentThread().threadId()),start=System.nanoTime();
   DensityField field=memo?graph.newSampler():graph;long checksum=0;
   for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=-128;y<640;y++)checksum=checksum*31+Double.doubleToRawLongBits(field.sample(ox+x,y,oz+z));
   long elapsed=System.nanoTime()-start,bytes=bean.getThreadAllocatedBytes(Thread.currentThread().threadId())-beforeBytes;sink=checksum;
   rows.append(iteration).append(',').append(memo?"memo":"scalar").append(',').append(elapsed).append(',').append(bytes).append(',').append(checksum).append('\n');
  }
  Files.writeString(Path.of(args[1]),rows);System.out.println("DENSITY_MASK_BENCHMARK_COMPLETE fork="+fork+" measuredPerMode=8 warmupsPerMode=4 samplesPerVolume=196608");
 }
}
