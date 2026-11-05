import com.shnewbs.deepprimal.core.density.*;
import java.util.*;
import java.lang.management.ManagementFactory;
import static com.shnewbs.deepprimal.core.density.DensityGraph.*;

public final class MetadataBenchmark {
  private static volatile DensityGraph retained;
  private static volatile double sink;
  private static final com.sun.management.ThreadMXBean BEAN=(com.sun.management.ThreadMXBean)ManagementFactory.getThreadMXBean();
  private static final long THREAD=Thread.currentThread().threadId();
  private static Definition dag() {
    var n=new ArrayList<Node>();n.add(Node.noise("n0",NoiseSpec.DEFAULT,true));
    for(int i=1;i<=11;i++)n.add(Node.operation("n"+i,Op.ADD,"n"+(i-1),"n"+(i-1)));
    return new Definition(1,"n11",n);
  }
  private static Definition large() {
    var n=new ArrayList<Node>();var layer=new ArrayList<String>();
    for(int i=0;i<64;i++){String id="c"+i;n.add(Node.constant(id,i%2==0?1:-.25));layer.add(id);}
    int seq=0;while(layer.size()>1){var next=new ArrayList<String>();for(int i=0;i<layer.size();i+=2){String id="s"+seq++;n.add(Node.operation(id,Op.ADD,layer.get(i),layer.get(i+1)));next.add(id);}layer=next;}
    n.add(Node.operation("root",Op.ABS,layer.getFirst()));return new Definition(1,"root",n);
  }
  private static void row(String phase,String name,double[] times,long[] bytes,double checksum) {
    double[] sorted=times.clone();Arrays.sort(sorted);
    System.out.printf(Locale.ROOT,"%s,%s,%d,%.6f,%.6f,%.6f,%.6f,%.6f,%.1f,%.17g%n",phase,name,times.length,
        Arrays.stream(times).average().orElseThrow(),sorted[(sorted.length-1)/2],sorted[(int)Math.ceil(sorted.length*.95)-1],sorted[(int)Math.ceil(sorted.length*.99)-1],sorted[sorted.length-1],Arrays.stream(bytes).average().orElseThrow(),checksum);
  }
  private static void constructors() {
    Definition[] definitions={TerrainShapeV8.definition(false),TerrainShapeV8.definition(true),dag(),large()};
    String[] names={"shape_off","shape_on","shared_2d_dag","constant_128"};
    for(int k=0;k<definitions.length;k++) {
      long b=BEAN.getThreadAllocatedBytes(THREAD),start=System.nanoTime();retained=definitions[k].compile(73);
      row("cold_compile",names[k],new double[]{(System.nanoTime()-start)*1e-6},new long[]{BEAN.getThreadAllocatedBytes(THREAD)-b},retained.bounds().min());
    }
    for(int i=0;i<128;i++)for(var d:definitions)retained=d.compile(i);
    double[][] time=new double[4][512];long[][] bytes=new long[4][512];double[] sums=new double[4];
    for(int i=0;i<512;i++)for(int shift=0;shift<4;shift++) {
      int k=(i+shift)%4;long b=BEAN.getThreadAllocatedBytes(THREAD),start=System.nanoTime();retained=definitions[k].compile(i-256);
      time[k][i]=(System.nanoTime()-start)*1e-6;bytes[k][i]=BEAN.getThreadAllocatedBytes(THREAD)-b;sums[k]+=retained.bounds().min();
    }
    for(int k=0;k<4;k++)row("warm_compile",names[k],time[k],bytes[k],sums[k]);
  }
  private static double volume(DensityGraph graph,int x,int z,boolean rowOrder) {
    var sampler=graph.newSampler();double sum=0;
    if(rowOrder)for(int iz=0;iz<16;iz++)for(int y=-128;y<640;y++)for(int ix=0;ix<16;ix++)sum+=sampler.sample(x+ix,y,z+iz);
    else for(int iz=0;iz<16;iz++)for(int ix=0;ix<16;ix++)for(int y=-128;y<640;y++)sum+=sampler.sample(x+ix,y,z+iz);
    return sum;
  }
  private static void sampling() {
    for(long seed:new long[]{73,20261020})for(boolean floating:new boolean[]{false,true})for(boolean rowOrder:new boolean[]{false,true}) {
      var graph=TerrainShapeV8.definition(floating).compile(seed);String name=seed+"_"+(floating?"on":"off")+"_"+(rowOrder?"row":"column");
      int x=(seed==73?43:232)*16,z=(seed==73?34:27)*16;
      long b=BEAN.getThreadAllocatedBytes(THREAD),start=System.nanoTime();sink=volume(graph,x,z,rowOrder);
      row("cold_volume",name,new double[]{(System.nanoTime()-start)*1e-6},new long[]{BEAN.getThreadAllocatedBytes(THREAD)-b},sink);
      for(int i=1;i<=3;i++)sink=volume(graph,x+i*16,z+i*16,rowOrder);
      double[] times=new double[4];long[] bytes=new long[4];double checksum=0;
      for(int i=0;i<4;i++) {
        b=BEAN.getThreadAllocatedBytes(THREAD);start=System.nanoTime();sink=volume(graph,x+(i+4)*16,z+(i+4)*16,rowOrder);
        times[i]=(System.nanoTime()-start)*1e-6;bytes[i]=BEAN.getThreadAllocatedBytes(THREAD)-b;checksum+=sink;
      }
      row("warm_volume",name,times,bytes,checksum);System.out.flush();
    }
  }
  public static void main(String[] args) {
    BEAN.setThreadAllocatedMemoryEnabled(true);System.out.println("phase,case,n,mean_ms,p50_ms,p95_ms,p99_ms,max_ms,allocated_bytes,checksum");
    if(args[0].equals("compile"))constructors();else sampling();
    System.out.println("METADATA_BENCHMARK_PASS mode="+args[0]+" java="+System.getProperty("java.runtime.version"));
  }
}
