package com.shnewbs.deepprimal.core;
import java.util.*;
final class TerrainV7Checks {
  static void run() {
    var heights=new HashSet<Long>();var climates=new HashSet<Long>();var biomes=new HashSet<Integer>();
    double low=Double.POSITIVE_INFINITY,high=-low,rainLow=1,rainHigh=0;long columns=0;int streams=0;
    for(long seed:new long[]{0,1,2,3,73,20261020,8675309,-987654321}) {
      var t=new TerrainEngine(seed,7);var origin=t.sample(0,0);
      heights.add(Double.doubleToLongBits(t.raw(0,0)));climates.add(Double.doubleToLongBits(origin.rain()));
      low=Math.min(low,t.raw(0,0));high=Math.max(high,t.raw(0,0));rainLow=Math.min(rainLow,origin.rain());rainHigh=Math.max(rainHigh,origin.rain());
      for(int z=-1024;z<=1024;z+=128)for(int x=-1024;x<=1024;x+=128){
        var a=t.sample(x,z);if(!Double.isFinite(a.ground())||a.ground()<=-128||a.ground()>=640||a.wet()&&a.water()<=a.ground())throw new AssertionError("Invalid V7 column");
        biomes.add(t.biome(x,z));columns++;
      }
      var widths=t.waterways(0,0).centerline().stream().mapToDouble(p->p.radius()*2).sorted().toArray();
      if(widths.length==0||widths[widths.length/2]<4||widths[widths.length-1]<10)throw new AssertionError("Only narrow waterways");
      streams+=widths.length;
      for(int n=-4096;n<=4096;n+=128)
        if(t.raw(4096,n)>=TerrainEngine.SEA||t.raw(-4096,n)>=TerrainEngine.SEA
            ||t.raw(n,4096)>=TerrainEngine.SEA||t.raw(n,-4096)>=TerrainEngine.SEA)
          throw new AssertionError("Land crosses an independent drainage domain");
      var copy=new TerrainEngine(seed,7);if(!copy.sample(-17,31).equals(t.sample(-17,31)))throw new AssertionError("V7 seed replay");
    }
    if(heights.size()!=8||climates.size()!=8||high-low<80||rainHigh-rainLow<.5||biomes.size()<12)throw new AssertionError("Origin aliasing or missing regional variation");
    for(int b:new int[]{1,10,13,14,15})if(Vegetation.treeChance(b,.5)<Vegetation.treeChance(0,.5)*10)throw new AssertionError("Forest canopy density does not follow biome");
    System.out.println("TERRAIN_V7_PASS seeds=8 columns="+columns+" biomeIds="+biomes+" uniqueOriginHeights=8 uniqueOriginRain=8 reaches="+streams);
  }
}
