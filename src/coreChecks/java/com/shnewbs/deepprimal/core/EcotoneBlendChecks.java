package com.shnewbs.deepprimal.core;

import com.shnewbs.deepprimal.core.biome.EcotoneBlend;
import com.shnewbs.deepprimal.core.biome.EcotoneBlend.Climate;
import com.shnewbs.deepprimal.core.biome.EcotoneBlend.Domain;
import com.shnewbs.deepprimal.core.biome.EcotoneBlend.Widths;
import com.shnewbs.deepprimal.core.biome.EcotoneBlend.Weights;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

/** Independent hard oracle and continuous-weight checks; no native world-generation claim. */
public final class EcotoneBlendChecks {
  private static long assertions;
  private static int hardCases, softCases, continuityCases, partitionCases, parallelCases;
  private static final boolean[] observed = new boolean[40];
  private static final double[][] THRESHOLDS = {
      {45, 100, 105, 165, 170, 210, 260},
      {.18, .2, .22, .25, .26, .3, .39, .42, .45, .62, .64, .71, .72, .73, .8},
      {.32, .46, .5, .56, .57, .66, .7, .74, .77},
      {.9, 1.2, 1.4},
      {-.12, .18, .25, .30}
  };

  private EcotoneBlendChecks() {}

  private static void check(boolean condition, String message) {
    assertions++;
    if (!condition) throw new AssertionError(message);
  }

  private static void close(double got, double expected, double tolerance, String message) {
    assertions++;
    if (!Double.isFinite(got) || Math.abs(got - expected) > tolerance)
      throw new AssertionError(message + ": " + got + " != " + expected);
  }

  /* Frozen independent decision oracle from Landscape.biome at 49bef407.
   * Source blob 3c7b31e84c952878671511924188175f2b5181a0. No candidate gate helper used. */
  private static int hard(Climate c, Domain domain) {
    double h=c.height(),t=c.temperature(),r=c.rainfall(),patch=c.patch(),slope=c.slope();
    if(domain == Domain.OCEAN) {
      if(h<45)return t<.18?35:t<.42?34:33;
      return t<.18?32:t<.42?31:t>.8?29:t>.64?30:3;
    }
    if(domain == Domain.RIVER)return t<.22?37:36;
    if(h<100)return t<.25?26:slope>1.4?27:25;
    if(h>260)return t<.26?19:slope>1.2?20:7;
    if(h>210)return t<.3?18:t<.45?17:slope>.9?23:21;
    if(t<.2)return r>.50?16:5;
    if(t<.39)return r>.66?15:4;
    if(h<105&&r>.74)return t>.72?28:6;
    if(r<.32)return patch>.18?9:2;
    if(r<.46)return t>.62?8:0;
    if(t>.73&&r>.70)return patch>.25?11:10;
    if(t>.71&&r>.56)return 12;
    if(r>.77)return 13;
    if(h>165&&patch>.30)return 22;
    if(slope>1.2)return 24;
    if(r>.57)return patch<-.12?14:1;
    return h>170?21:0;
  }

  private static Climate randomClimate(Random random) {
    return new Climate(-384 + random.nextDouble() * 1024, random.nextDouble(),
        random.nextDouble(), random.nextDouble() * 3, random.nextDouble() * 2 - 1);
  }

  private static Climate axis(Climate c, int axis, double value) {
    double[] v = {c.height(), c.temperature(), c.rainfall(), c.slope(), c.patch()};
    v[axis] = value;
    return new Climate(v[0], v[1], v[2], v[3], v[4]);
  }

  private static void hardCase(Climate c, Domain d) {
    Weights got = EcotoneBlend.sample(c, d, Widths.HARD);
    int expected = hard(c, d);
    for (int id = 0; id < 40; id++) close(got.weight(id), id == expected ? 1 : 0, 0, "hard oracle");
    check(got.dominantBiome() == expected, "hard dominant");
    check(got.choose(0) == expected && got.choose(Math.nextDown(1.0)) == expected, "hard choice");
    observed[expected] = true;
    hardCases++;
  }

  private static void softCase(Climate c, Domain d, Widths widths) {
    Weights result = EcotoneBlend.sample(c, d, widths);
    double sum = 0;
    for (int id = 0; id < 40; id++) {
      double value = result.weight(id);
      check(Double.isFinite(value) && value >= 0 && value <= 1, "finite unit weight");
      boolean ocean = id == 3 || id >= 29 && id <= 35;
      boolean river = id == 36 || id == 37;
      if (d == Domain.LAND && (ocean || river) || d == Domain.RIVER && !river || d == Domain.OCEAN && !ocean)
        close(value, 0, 0, "hydrological domain isolation");
      close(id >= 38 ? value : 0, 0, 0, "reserved palette ids");
      sum += value;
    }
    close(sum, 1, 2e-15, "normalized weights");
    check(Arrays.equals(result.toArray(), EcotoneBlend.sample(c, d, widths).toArray()), "repeat bit identity");
    for (double unit : new double[]{0, .19, .5, .99, Math.nextDown(1.0)})
      check(result.weight(result.choose(unit)) > 0, "choice has positive weight");
    softCases++;
  }

  private static void normalizationAndOracle() {
    Random random = new Random(20261028L);
    Widths[] widths = {Widths.PREVIEW, new Widths(.5,.5,256,4,2),
        new Widths(Double.MIN_VALUE,Double.MIN_VALUE,Double.MIN_VALUE,Double.MIN_VALUE,Double.MIN_VALUE),
        new Widths(0,.03,12,0,.1)};
    for (int i = 0; i < 12000; i++) {
      Climate c = randomClimate(random);
      for (Domain d : Domain.values()) {
        hardCase(c, d);
        softCase(c, d, widths[i % widths.length]);
      }
    }
    for (int axis = 0; axis < THRESHOLDS.length; axis++) {
      for (double threshold : THRESHOLDS[axis]) {
        for (int context = 0; context < 32; context++) {
          Climate base = randomClimate(random);
          for (double value : new double[]{Math.nextDown(threshold), threshold, Math.nextUp(threshold)}) {
            Climate c = axis(base, axis, value);
            for (Domain d : Domain.values()) {
              hardCase(c, d);
              softCase(c, d, widths[context % widths.length]);
            }
          }
        }
      }
    }
    for (int id = 0; id < 38; id++) check(observed[id], "oracle corpus missing palette id " + id);
    for (double height : new double[]{-Double.MAX_VALUE, Double.MAX_VALUE})
      for (Domain d : Domain.values()) {
        Climate c = new Climate(height,.5,.5,Double.MAX_VALUE,-Double.MAX_VALUE);
        hardCase(c,d);softCase(c,d,Widths.PREVIEW);
      }
  }

  private static double l1(Weights a, Weights b) {
    double result = 0;
    for (int i = 0; i < 40; i++) result += Math.abs(a.weight(i) - b.weight(i));
    return result;
  }

  private static void continuity() {
    Random random = new Random(7382);
    double[] widths = {16,.025,.04,.15,.08};
    for (int axis = 0; axis < THRESHOLDS.length; axis++) {
      for (double threshold : THRESHOLDS[axis]) {
        for (int context = 0; context < 64; context++) {
          Climate base = randomClimate(random);
          for (double edge : new double[]{threshold - widths[axis], threshold, threshold + widths[axis]}) {
            double eps = widths[axis] * 1e-6;
            if ((axis == 1 || axis == 2) && (edge - eps < 0 || edge + eps > 1) || axis == 3 && edge - eps < 0) continue;
            for (Domain d : Domain.values()) {
              Weights left = EcotoneBlend.sample(axis(base,axis,edge-eps),d,Widths.PREVIEW);
              Weights right = EcotoneBlend.sample(axis(base,axis,edge+eps),d,Widths.PREVIEW);
              check(l1(left,right) < 2e-5, "continuous threshold neighborhood");
              continuityCases++;
            }
          }
        }
      }
    }
    Climate center = new Climate(140,.22,.5,.3,0);
    Weights equal = EcotoneBlend.sample(center,Domain.RIVER,Widths.PREVIEW);
    close(equal.weight(37),.5,0,"river midpoint cold");
    close(equal.weight(36),.5,0,"river midpoint warm");
    check(equal.dominantBiome() == 36,"stable smallest-id tie");
    close(EcotoneBlend.sample(axis(center,1,.18),Domain.RIVER,Widths.PREVIEW).weight(37),1,0,"cold endpoint");
    close(EcotoneBlend.sample(axis(center,1,.26),Domain.RIVER,Widths.PREVIEW).weight(36),1,0,"warm endpoint");
    int warm = 0;
    for (int i = 0; i < 1000; i++) if (equal.choose((i + .5) / 1000) == 36) warm++;
    check(warm == 500,"stratified palette choices match weights");
    double[] properties = new double[40];
    properties[36] = .8;properties[37] = .2;
    close(equal.blendUnitValues(properties),.5,1e-15,"continuous property mixing");
    close(equal.blendBoundedValues(new double[]{0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.2}, -2, 2, "bounded property"), .2, 1e-15, "bounded continuous property mixing");
    Arrays.fill(properties,1);close(equal.blendUnitValues(properties),1,1e-15,"constant property preserved");
    double[] copy = equal.toArray();Arrays.fill(copy,0);
    close(equal.weight(36),.5,0,"immutable distribution");
  }

  /* Synthetic global fields, NOT Minecraft/TerrainEngine coverage. Chunk ids never seed sampling. */
  private static Climate world(int x,int z) {
    double a=StrictMath.sin(x/617.0+z/1913.0), b=StrictMath.cos(z/877.0-x/1091.0);
    return new Climate(170+110*a,.5+.4*b,.5+.4*StrictMath.sin(x/911.0),
        .8+.7*StrictMath.cos(z/503.0),a*b);
  }

  private static void partitionAndParallel() throws Exception {
    int[] chunks = {Integer.MIN_VALUE/16,-513,-17,-1,0,1,17,511,Integer.MAX_VALUE/16-1};
    for (int cx : chunks) for (int cz : chunks) {
      for (int offset=0;offset<=16;offset++) {
        int x=Math.toIntExact((long)cx*16+16),z=Math.toIntExact((long)cz*16+offset);
        Weights left=EcotoneBlend.sample(world(x,z),Domain.LAND,Widths.PREVIEW);
        int fromNext=Math.toIntExact(((long)cx+1)*16);
        Weights right=EcotoneBlend.sample(world(fromNext,z),Domain.LAND,Widths.PREVIEW);
        check(Arrays.equals(left.toArray(),right.toArray()),"partition seam / X halo identity");
        int zx=Math.toIntExact((long)cx*16+offset),zz=Math.toIntExact((long)cz*16+16);
        check(Arrays.equals(EcotoneBlend.sample(world(zx,zz),Domain.LAND,Widths.PREVIEW).toArray(),
            EcotoneBlend.sample(world(zx,Math.toIntExact(((long)cz+1)*16)),Domain.LAND,Widths.PREVIEW).toArray()),
            "partition seam / Z halo identity");
        partitionCases+=2;
      }
    }
    double[][] serial=new double[2048][];
    for(int i=0;i<serial.length;i++) serial[i]=EcotoneBlend.sample(world(i*37-40000,i*71-50000),Domain.LAND,Widths.PREVIEW).toArray();
    try(var executor=Executors.newFixedThreadPool(4)) {
      var tasks=new java.util.ArrayList<Callable<Boolean>>();
      for(int job=0;job<8;job++) {
        final boolean reverse=(job&1)!=0;
        tasks.add(()->{
          for(int n=0;n<serial.length;n++) {
            int i=reverse?serial.length-1-n:n;
            if(!Arrays.equals(serial[i],EcotoneBlend.sample(world(i*37-40000,i*71-50000),Domain.LAND,Widths.PREVIEW).toArray())) return false;
          }
          return true;
        });
      }
      for(var future:executor.invokeAll(tasks)) check(future.get(),"parallel/reverse-order replay");
      parallelCases=8*serial.length;
    }
  }

  private static void reject(Runnable call) {
    boolean rejected=false;
    try { call.run(); }
    catch(IllegalArgumentException | NullPointerException | IndexOutOfBoundsException expected) { rejected=true; }
    check(rejected,"invalid input accepted");
  }

  private static void validation() {
    for(double bad:new double[]{Double.NaN,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY}) {
      for(int axis=0;axis<5;axis++) {
        final int field=axis;
        reject(()->axis(new Climate(150,.5,.5,.5,0),field,bad));
      }
      reject(()->new Widths(bad,0,0,0,0));
    }
    reject(()->new Climate(100,-.1,.5,0,0));reject(()->new Climate(100,.5,1.1,0,0));
    reject(()->new Climate(100,.5,.5,-1,0));
    reject(()->new Widths(-1,0,0,0,0));reject(()->new Widths(.6,0,0,0,0));
    reject(()->new Widths(0,.6,0,0,0));reject(()->new Widths(0,0,257,0,0));
    reject(()->new Widths(0,0,0,5,0));reject(()->new Widths(0,0,0,0,3));
    Climate c=new Climate(150,.5,.5,.5,0);
    reject(()->EcotoneBlend.sample(null,Domain.LAND,Widths.PREVIEW));
    reject(()->EcotoneBlend.sample(c,null,Widths.PREVIEW));reject(()->EcotoneBlend.sample(c,Domain.LAND,null));
    Weights result=EcotoneBlend.sample(c,Domain.LAND,Widths.PREVIEW);
    for(double unit:new double[]{-.1,1,Double.NaN,Double.POSITIVE_INFINITY}) reject(()->result.choose(unit));
    reject(()->result.weight(-1));reject(()->result.weight(40));
    reject(()->result.blendUnitValues(null));reject(()->result.blendUnitValues(new double[39]));
    double[] badPalette=new double[40];badPalette[39]=Double.NaN;
    reject(()->result.blendUnitValues(badPalette));
    badPalette[39]=1.1;reject(()->result.blendUnitValues(badPalette));
  }

  private static String fingerprint() throws Exception {
    MessageDigest digest=MessageDigest.getInstance("SHA-256");
    ByteBuffer bytes=ByteBuffer.allocate(8);
    Random random=new Random(50153);
    for(int i=0;i<2000;i++) {
      Climate c=randomClimate(random);
      for(Domain d:Domain.values()) for(double weight:EcotoneBlend.sample(c,d,Widths.PREVIEW).toArray()) {
        bytes.clear();bytes.putLong(Double.doubleToLongBits(weight));digest.update(bytes.array());
      }
    }
    return HexFormat.of().formatHex(digest.digest());
  }

  public static void main(String[] args) throws Exception {
    normalizationAndOracle();continuity();partitionAndParallel();validation();
    String digest=fingerprint();
    check(digest.equals("9d2ea5001443660500e66afff79cbb724753f393c9e4fa5cc4691a408be56e31"),"policy-1 fingerprint");
    System.out.printf("ECOTONE_BLEND_PASS hardCases=%d softCases=%d continuityCases=%d partitionCases=%d parallelSamples=%d assertions=%d fingerprint=%s%n",
        hardCases,softCases,continuityCases,partitionCases,parallelCases,assertions,digest);
  }
}
