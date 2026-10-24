package com.shnewbs.deepprimal.core.density;

import java.util.*;
import java.util.concurrent.*;
import java.security.MessageDigest;
import java.nio.ByteBuffer;

/** Independent corner-weight oracle, world-lattice boundaries and immutable parallel replay. */
public final class NoiseCoreChecks {
  private static int checks;
  private static void require(boolean ok,String message) { checks++;if(!ok)throw new AssertionError(message); }
  private static void rejects(Runnable action) {
    try {action.run();throw new AssertionError("invalid request accepted");}
    catch(IllegalArgumentException expected){checks++;}
  }
  private static void equal(double a,double b,String label) {require(Double.doubleToLongBits(a)==Double.doubleToLongBits(b),label);}
  public static void main(String[] args)throws Exception {
    if(args.length>0&&args[0].equals("fingerprint")){System.out.println("NOISE_FINGERPRINT "+fingerprint());return;}
    Random random=new Random(717399);
    for(int seed=0;seed<8;seed++) {
      var spec=new NoiseSpec(1./97,1./51,1./137,1+seed%5,1.8,.53,seed*351L);
      var noise=new SeededNoise3D(seed-3,spec);
      for(int i=0;i<5000;i++) {
        double x=(random.nextDouble()-.5)*60_000_000,y=(random.nextDouble()-.5)*4096,z=(random.nextDouble()-.5)*60_000_000;
        double actual=noise.sample(x,y,z),oracle=reference(seed-3,spec,x,y,z);
        require(Math.abs(actual-oracle)<2e-14,"independent eight-corner weight oracle");
        require(Double.isFinite(actual)&&Math.abs(actual)<=1,"finite bounded sample");
      }
    }
    var origins=new HashSet<Long>();for(long seed:new long[]{0,1,2,3,73,20261020,Long.MIN_VALUE,Long.MAX_VALUE})
      origins.add(Double.doubleToLongBits(new SeededNoise3D(seed,NoiseSpec.DEFAULT).sample(0,0,0)));
    require(origins.size()==8,"shared seed origin");
    var noise=new SeededNoise3D(Long.MIN_VALUE,NoiseSpec.DEFAULT);
    double[] bulk=new double[17*31*17+6];Arrays.fill(bulk,42);
    noise.fill(bulk,3,17,31,17,-16,-128,-16,1,8,1);
    int index=3;for(int z=0;z<17;z++)for(int y=0;y<31;y++)for(int x=0;x<17;x++)
      equal(bulk[index++],noise.sample(-16+x,-128+y*8,-16+z),"bulk/scalar bits");
    require(bulk[0]==42&&bulk[2]==42&&bulk[bulk.length-1]==42,"bulk output bounds");
    long seams=0;
    for(int origin:new int[]{-29_999_984,-32,-16,0,16,29_999_968}) {
      var west=DensityGrid.sample(noise,origin,-128,origin,4,8,4,5,49,5);
      var east=DensityGrid.sample(noise,origin+16,-128,origin,4,8,4,5,49,5);
      var south=DensityGrid.sample(noise,origin,-128,origin+16,4,8,4,5,49,5);
      var upper=DensityGrid.sample(noise,origin,256,origin,4,8,4,5,49,5);
      require(west.payloadBytes()==9800&&west.nodeCount()==1225,"bounded retained lattice");
      for(int iz=0;iz<=64;iz++)for(int iy=0;iy<=96;iy++) {
        double y=-128+iy*4.,z=origin+iz*.25;
        equal(west.sample(origin+16,y,z),east.sample(origin+16,y,z),"x shared face");
        equal(west.sample(z,y,origin+16),south.sample(z,y,origin+16),"z shared face");seams+=2;
      }
      for(int iz=0;iz<=16;iz++)for(int ix=0;ix<=16;ix++){
        equal(west.sample(origin+ix,256,origin+iz),upper.sample(origin+ix,256,origin+iz),"vertical shared face");seams++;
      }
      for(int iz=0;iz<5;iz++)for(int iy=0;iy<49;iy++)for(int ix=0;ix<5;ix++)
        equal(west.sample(origin+ix*4,-128+iy*8,origin+iz*4),noise.sample(origin+ix*4,-128+iy*8,origin+iz*4),"exact node");
    }
    var affine=DensityGrid.sample((x,y,z)->x*.125+y*.25-z*.5,-16,-128,-16,4,8,4,5,49,5);
    for(int i=0;i<10000;i++){
      double x=-16+random.nextDouble()*16,y=-128+random.nextDouble()*384,z=-16+random.nextDouble()*16;
      require(Math.abs(affine.sample(x,y,z)-(x*.125+y*.25-z*.5))<1e-12,"affine interpolation oracle");
    }
    invalid(noise);
    var grid=DensityGrid.sample(noise,-16,-128,-16,4,8,4,5,49,5);
    long expected=hash(grid);try(var executor=Executors.newFixedThreadPool(4)) {
      var jobs=new ArrayList<Future<Long>>();for(int i=0;i<64;i++)jobs.add(executor.submit(()->hash(grid)));
      for(var job:jobs)require(job.get()==expected,"immutable concurrent replay");
    }
    String fingerprint=fingerprint();
    require(fingerprint.equals("6d53af6ab4d0f54f725723a6fcf0708bb6a7ff82b0b8a6082bb78b31d07267c1"),"algorithm version-one fingerprint changed");
    System.out.println("NOISE_CORE_PASS checks="+checks+" oracleSamples=40000 sharedFaceSamples="+seams+" parallelJobs=64 fingerprint="+fingerprint);
  }

  private static void invalid(SeededNoise3D noise) {
    for(double bad:new double[]{Double.NaN,Double.POSITIVE_INFINITY,0,-1,17})rejects(()->new NoiseSpec(bad,.01,.01,1,2,.5,0));
    rejects(()->new NoiseSpec(1,1,1,8,4,.5,0));rejects(()->new NoiseSpec(.1,.1,.1,0,2,.5,0));
    rejects(()->new NoiseSpec(.1,.1,.1,9,2,.5,0));rejects(()->new NoiseSpec(.1,.1,.1,1,Double.NaN,.5,0));
    rejects(()->new NoiseSpec(.1,.1,.1,1,2,Double.NaN,0));rejects(()->new NoiseSpec(.1,.1,.1,1,2,1.1,0));
    for(double bad:new double[]{Double.NaN,Double.NEGATIVE_INFINITY,32_000_001,-32_000_001})rejects(()->noise.sample(bad,0,0));
    double[] out={11,22,33,44};double[] before=out.clone();
    rejects(()->noise.fill(out,0,2,2,2,0,0,0,1,1,1));
    rejects(()->noise.fill(out,0,1,1,1,0,0,0,0,1,1));
    rejects(()->noise.fill(out,0,1_048_576,1_048_576,1_048_576,0,0,0,1,1,1));
    rejects(()->noise.fill(out,0,2,1,1,32_000_000,0,0,1,1,1));
    rejects(()->noise.fill(out,Integer.MAX_VALUE,1,1,1,0,0,0,1,1,1));
    require(Arrays.equals(out,before),"invalid batch must not partially write");
    rejects(()->DensityGrid.sample(noise,-1,0,0,4,8,4,5,49,5));
    rejects(()->DensityGrid.sample(noise,0,0,0,4,8,4,1,49,5));
    rejects(()->DensityGrid.sample(noise,32_000_000,0,0,4,8,4,5,49,5));
    rejects(()->DensityGrid.sample((x,y,z)->Double.NaN,0,0,0,4,8,4,5,49,5));
    var grid=DensityGrid.sample(noise,0,0,0,4,8,4,5,49,5);
    rejects(()->grid.sample(-.01,0,0));rejects(()->grid.sample(Double.NaN,0,0));rejects(()->grid.sample(16.01,0,0));
    for(double at:new double[]{-32_000_000,32_000_000})require(Double.isFinite(noise.sample(at,at,at)),"supported extreme");
  }

  private static long hash(DensityField field) {
    long result=1;
    for(int z=-16;z<=0;z++)for(int y=-128;y<=256;y+=7)for(int x=-16;x<=0;x++)
      result=31*result+Double.doubleToLongBits(field.sample(x,y,z));
    return result;
  }

  public static String fingerprint()throws Exception {
    var digest=MessageDigest.getInstance("SHA-256");var buffer=ByteBuffer.allocate(8);
    for(long seed:new long[]{0,1,-1,Long.MIN_VALUE,Long.MAX_VALUE}) {
      var n=new SeededNoise3D(seed,NoiseSpec.DEFAULT);
      for(int i=0;i<2000;i++) {
        double value=n.sample((i-1000)*29983.125,(i%769)-384.5,(999-i)*29971.25);
        buffer.clear();buffer.putLong(Double.doubleToLongBits(value));digest.update(buffer.array());
      }
    }
    return HexFormat.of().formatHex(digest.digest());
  }

  // Independent tensor-product weighted-corner implementation; no production helper calls.
  private static double reference(long seed,NoiseSpec spec,double x,double y,double z) {
    int[][] gradients={{1,1,0},{-1,1,0},{1,-1,0},{-1,-1,0},{1,0,1},{-1,0,1},{1,0,-1},{-1,0,-1},{0,1,1},{0,-1,1},{0,1,-1},{0,-1,-1}};
    double frequency=1,weight=1,total=0,weights=0;
    for(int octave=0;octave<spec.octaves();octave++) {
      long salt=mix(spec.salt()+octave*0x9e3779b97f4a7c15L);
      double xx=x*(spec.frequencyX()*frequency)+phase(seed^salt^0x13b586a51L);
      double yy=y*(spec.frequencyY()*frequency)+phase(seed^salt^0x6a8ef452dL);
      double zz=z*(spec.frequencyZ()*frequency)+phase(seed^salt^0x31658b9a3L);
      int ix=(int)Math.floor(xx),iy=(int)Math.floor(yy),iz=(int)Math.floor(zz);
      double dx=xx-ix,dy=yy-iy,dz=zz-iz,u=fade(dx),v=fade(dy),w=fade(dz),value=0;
      for(int k=0;k<8;k++) {
        int a=k&1,b=(k>>1)&1,c=(k>>2)&1;
        long h=mix(seed^salt^(ix+a)*0x9e3779b97f4a7c15L^(iy+b)*0xd1b54a32d192ed03L^(iz+c)*0x94d049bb133111ebL);
        int[] g=gradients[(int)((h>>>1)%12)];
        value+=(g[0]*(dx-a)+g[1]*(dy-b)+g[2]*(dz-c))*(a==0?1-u:u)*(b==0?1-v:v)*(c==0?1-w:w);
      }
      total+=weight*value*.5;weights+=weight;frequency*=spec.lacunarity();weight*=spec.gain();
    }
    return total/weights;
  }
  private static double fade(double t){return t*t*t*(t*(t*6-15)+10);}
  private static double phase(long x){return .125+(mix(x)>>>11)*0x1.0p-53*1024;}
  private static long mix(long x){x=(x^(x>>>30))*0xbf58476d1ce4e5b9L;x=(x^(x>>>27))*0x94d049bb133111ebL;return x^(x>>>31);}
}
