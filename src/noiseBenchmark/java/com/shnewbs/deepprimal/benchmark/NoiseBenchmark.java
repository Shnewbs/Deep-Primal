package com.shnewbs.deepprimal.benchmark;

import com.shnewbs.deepprimal.core.density.*;
import java.util.*;
import java.lang.management.ManagementFactory;

/** Repeatable component benchmark; deliberately not reported as whole Minecraft chunk generation. */
public final class NoiseBenchmark {
  private static volatile double sink;
  private static final int HEIGHT=768, SAMPLES=16*HEIGHT*16;
  private static final com.sun.management.ThreadMXBean BEAN=(com.sun.management.ThreadMXBean)ManagementFactory.getThreadMXBean();
  private static final long THREAD=Thread.currentThread().threadId();
  private static double run(String mode,SeededNoise3D noise,double[] output,int index) {
    int x=(index*97-200)*16,z=(index*53-100)*16;
    if(mode.equals("exact_bulk"))noise.fill(output,0,16,HEIGHT,16,x,-128,z,1,1,1);
    else if(mode.equals("exact_scalar")) {
      int at=0;for(int iz=0;iz<16;iz++)for(int iy=0;iy<HEIGHT;iy++)for(int ix=0;ix<16;ix++)output[at++]=noise.sample(x+ix,-128+iy,z+iz);
    } else {
      var grid=DensityGrid.sample(noise,x,-128,z,4,8,4,5,97,5);
      int at=0;for(int iz=0;iz<16;iz++)for(int iy=0;iy<HEIGHT;iy++)for(int ix=0;ix<16;ix++)output[at++]=grid.sample(x+ix,-128+iy,z+iz);
    }
    double sum=0;for(double value:output)sum+=value;return sum;
  }
  public static void main(String[] args) {
    BEAN.setThreadAllocatedMemoryEnabled(true);
    var noise=new SeededNoise3D(20261020,NoiseSpec.DEFAULT);double[] output=new double[SAMPLES];
    String[] modes={"exact_scalar","exact_bulk","lattice_build_and_sample"};
    for(int i=0;i<12;i++)for(String mode:modes)sink=run(mode,noise,output,i);
    double[][] elapsed=new double[3][24];long[][] allocated=new long[3][24];
    for(int i=0;i<24;i++)for(int shift=0;shift<3;shift++){
      int mode=(shift+i)%3;long before=BEAN.getThreadAllocatedBytes(THREAD),start=System.nanoTime();
      sink=run(modes[mode],noise,output,i+12);elapsed[mode][i]=(System.nanoTime()-start)*1e-6;
      allocated[mode][i]=BEAN.getThreadAllocatedBytes(THREAD)-before;
    }
    System.out.println("mode,samples_per_iteration,iterations,mean_ms,p50_ms,p95_ms,p99_ms,max_ms,mean_thread_allocated_bytes");
    for(int i=0;i<3;i++) {
      var a=elapsed[i];Arrays.sort(a);
      System.out.printf(Locale.ROOT,"%s,%d,%d,%.6f,%.6f,%.6f,%.6f,%.6f,%.1f%n",modes[i],SAMPLES,a.length,
          Arrays.stream(a).average().orElseThrow(),a[11],a[22],a[23],a[23],Arrays.stream(allocated[i]).average().orElseThrow());
    }
    // Error measured separately: the lattice is an approximation, never claimed bit-equivalent.
    var grid=DensityGrid.sample(noise,-16,-128,-16,4,8,4,5,97,5);double sum=0,max=0;int count=0;
    for(int z=-16;z<0;z++)for(int y=-128;y<640;y+=3)for(int x=-16;x<0;x++){
      double error=Math.abs(grid.sample(x,y,z)-noise.sample(x,y,z));sum+=error*error;max=Math.max(max,error);count++;
    }
    System.out.printf(Locale.ROOT,"APPROXIMATION samples=%d rms=%.10f max=%.10f latticeNodes=2425 latticePayloadBytes=19400%n",count,Math.sqrt(sum/count),max);
    System.out.println("NOISE_BENCHMARK_COMPLETE checksum="+sink+" java="+System.getProperty("java.runtime.version")+" os="+System.getProperty("os.arch")+" processors="+Runtime.getRuntime().availableProcessors());
  }
}
