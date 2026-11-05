package com.shnewbs.deepprimal.benchmark;

import com.shnewbs.deepprimal.core.density.*;
import java.lang.management.ManagementFactory;
import java.util.*;

/** Component latency and topology error; excludes native block, surface, water, features and lighting. */
public final class DensityBenchmark {
  private static volatile double sink;
  private static double run(DensityGraph graph,boolean lattice,int chunk) {
    int x=(chunk*71-250)*16,z=(chunk*31-150)*16;double sum=0;
    DensityField field=lattice?DensityGrid.sample(graph,x,-128,z,4,8,4,5,97,5):graph;
    for(int iz=0;iz<16;iz++)for(int y=-128;y<640;y++)for(int ix=0;ix<16;ix++)sum+=field.sample(x+ix,y,z+iz);
    return sum;
  }
  public static void main(String[] args) {
    var graph=TerrainShapeV8.definition(false).compile(20261020);
    var bean=(com.sun.management.ThreadMXBean)ManagementFactory.getThreadMXBean();bean.setThreadAllocatedMemoryEnabled(true);long thread=Thread.currentThread().threadId();
    for(int i=0;i<6;i++){sink=run(graph,false,i);sink=run(graph,true,i);}
    double[][] times=new double[2][16];long[][] allocation=new long[2][16];
    for(int i=0;i<16;i++)for(int shift=0;shift<2;shift++){
      int mode=(i+shift)%2;long before=bean.getThreadAllocatedBytes(thread),start=System.nanoTime();
      sink=run(graph,mode==1,i+6);times[mode][i]=(System.nanoTime()-start)*1e-6;allocation[mode][i]=bean.getThreadAllocatedBytes(thread)-before;
    }
    System.out.println("mode,samples,iterations,mean_ms,p50_ms,p95_ms,p99_ms,max_ms,mean_thread_allocated_bytes");
    for(int mode=0;mode<2;mode++){
      Arrays.sort(times[mode]);var a=times[mode];System.out.printf(Locale.ROOT,"%s,196608,16,%.6f,%.6f,%.6f,%.6f,%.6f,%.1f%n",mode==0?"exact_graph":"lattice_graph",Arrays.stream(a).average().orElseThrow(),a[7],a[15],a[15],a[15],Arrays.stream(allocation[mode]).average().orElseThrow());
    }
    double squares=0,max=0;int count=0,signs=0;
    for(int c=0;c<4;c++){
      int x=c*256-512,z=c*176-384;var tile=DensityGrid.sample(graph,x,-128,z,4,8,4,5,97,5);
      for(int iz=0;iz<16;iz++)for(int y=-128;y<640;y+=2)for(int ix=0;ix<16;ix++){
        double a=graph.sample(x+ix,y,z+iz),b=tile.sample(x+ix,y,z+iz),e=Math.abs(a-b);squares+=e*e;max=Math.max(max,e);count++;if((a>0)!=(b>0))signs++;
      }
    }
    System.out.printf(Locale.ROOT,"DENSITY_APPROXIMATION samples=%d rms=%.9f max=%.9f signMismatches=%d%n",count,Math.sqrt(squares/count),max,signs);
    System.out.println("DENSITY_BENCHMARK_COMPLETE nodes="+graph.definition().nodes().size()+" depth="+graph.depth()+" maxNodeVisits="+graph.maximumEvaluationCost()+" checksum="+sink);
  }
}
