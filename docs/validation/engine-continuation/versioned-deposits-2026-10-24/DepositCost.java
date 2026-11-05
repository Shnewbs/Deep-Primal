import com.shnewbs.deepprimal.core.*;
import java.lang.management.ManagementFactory;
import java.util.*;

/** Counter-free matched CPU microprobe; not a frame-time or native generation benchmark. */
public final class DepositCost {
  static volatile Object escaped;
  static volatile long sink;
  static final int N=8192;
  static final com.sun.management.ThreadMXBean bean=(com.sun.management.ThreadMXBean)ManagementFactory.getThreadMXBean();
  static final long tid=Thread.currentThread().threadId();
  static long allocated(){return bean.getThreadAllocatedBytes(tid);}
  static long[] build(boolean candidate) {
    var noise=new TerrainNoise(20261020);var geology=new Geology(noise);var field=new DepositField(noise);
    var sample=new TerrainEngine.Sample(250,0,.6,.5,.2,.4,0,0,0,0);
    Object[] objects=new Object[N]; long a=allocated(),t=System.nanoTime();
    for(int i=0;i<N;i++) {var host=geology.column(i*17-65536,i*7-32768,sample);
      objects[i]=candidate?field.column(i*17-65536,i*7-32768,host):host;}
    long ns=System.nanoTime()-t,bytes=allocated()-a;escaped=objects;return new long[]{ns,bytes};
  }
  static long[] query(boolean candidate,Geology.Column[] hosts,DepositField.Column[] fields) {
    long sum=0,a=allocated(),t=System.nanoTime();
    for(int i=0;i<N;i++)for(int y=-128;y<256;y++)sum+=candidate?fields[i].ore(y,250).ordinal():hosts[i].ore(y,250).ordinal();
    long ns=System.nanoTime()-t,bytes=allocated()-a;sink=sum;return new long[]{ns,bytes,sum};
  }
  public static void main(String[] args) {
    bean.setThreadAllocatedMemoryEnabled(true);
    var noise=new TerrainNoise(20261020);var geology=new Geology(noise);var field=new DepositField(noise);
    var sample=new TerrainEngine.Sample(250,0,.6,.5,.2,.4,0,0,0,0);
    var hosts=new Geology.Column[N];var fields=new DepositField.Column[N];
    for(int i=0;i<N;i++){hosts[i]=geology.column(i*17-65536,i*7-32768,sample);fields[i]=field.column(i*17-65536,i*7-32768,hosts[i]);}
    for(int i=0;i<8;i++){build(false);build(true);query(false,hosts,fields);query(true,hosts,fields);}
    int[] activeHistogram=new int[10];
    try {var method=DepositField.Column.class.getMethod("activeCandidateCount");
      for(var column:fields)activeHistogram[(int)method.invoke(column)]++;
    } catch(ReflectiveOperationException absent) {activeHistogram[9]=N;}
    System.out.println("{\"active_histogram\":"+Arrays.toString(activeHistogram)+",\"columns\":"+N+",\"heights_per_column\":384,\"samples\":[");
    for(int i=0;i<9;i++) {
      long[] b,c,qb,qc;
      if(i%2==0){b=build(false);c=build(true);qb=query(false,hosts,fields);qc=query(true,hosts,fields);}
      else{c=build(true);b=build(false);qc=query(true,hosts,fields);qb=query(false,hosts,fields);}
      if(qb[1]!=0||qc[1]!=0)throw new AssertionError("query allocation");
      System.out.print((i==0?"":",\n")+"{\"legacy_build_ns_bytes\":"+Arrays.toString(b)+",\"candidate_build_ns_bytes\":"+Arrays.toString(c)+",\"legacy_query_ns_bytes_sum\":"+Arrays.toString(qb)+",\"candidate_query_ns_bytes_sum\":"+Arrays.toString(qc)+"}");
    }
    System.out.println("\n]}");
  }
}
