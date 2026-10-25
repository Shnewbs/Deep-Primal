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
    channels();
    EarthRiverChecks.run();
    DeltaChecks.run();
    OxbowChecks.run();
    soilOutcrops();
  }
  /** Material revision 2 removes iso-slope weathered ribbons; revisions 0/1 replay exact legacy horizons. */
  private static void soilOutcrops() {
    long land=0,ribbons1=0,ribbons2=0,edgeSoil=0,edgeRock=0,below=0,above=0,outcrops=0;
    for(long seed:new long[]{0,73,20261020}) {
      var t=new TerrainEngine(seed,7);var geology=new Geology(t.noise);
      for(int z=-1536;z<1536;z+=5)for(int x=-1536;x<1536;x+=5) {
        var s=t.sample(x,z);if(s.ground()<TerrainEngine.SEA)continue;
        var g=geology.column(x,z,s);var legacy=SoilProfile.at(s,g);var next=SoilProfile.at(t.noise,x,z,s,g);
        double slope=s.slope();
        var k=legacy.kind();
        int top=slope>1.6?0:k==SoilProfile.Kind.GRAVEL?1:s.rain()>.7?2:1;
        int sub=(int)Math.clamp(1+s.rain()*3+s.sediment()*2-slope*2,0,6);
        int weathered=slope>2.2?0:g.permeability()>.4?2:1;
        if(legacy.topsoil()!=top||legacy.subsoil()!=sub||legacy.weathered()!=weathered)
          throw new AssertionError("Legacy soil horizons changed at "+x+","+z);
        if(next.kind()!=legacy.kind()||!next.equals(SoilProfile.at(t.noise,x,z,s,g)))throw new AssertionError("Revision-2 soil replay");
        double jitter=SoilProfile.slopeJitter(t.noise,x,z);
        if(Math.abs(jitter)>.47)throw new AssertionError("Unbounded slope jitter "+jitter);
        if(s.wet())continue;
        land++;
        if(legacy.topsoil()+legacy.subsoil()==0&&legacy.weathered()>0)ribbons1++;
        if(next.topsoil()+next.subsoil()==0&&next.weathered()>0)ribbons2++;
        if(next.depth()==0&&legacy.depth()>0)outcrops++;
        if(slope>1.35&&slope<=1.6){below++;if(next.topsoil()==0)edgeRock++;} else if(slope>1.6&&slope<1.85){above++;if(next.topsoil()>0)edgeSoil++;}
      }
    }
    if(ribbons1<1000)throw new AssertionError("Legacy weathered ribbon witness missing: "+ribbons1);
    if(ribbons2!=0)throw new AssertionError("Revision-2 dry land still exposes weathered ribbons: "+ribbons2);
    double mix=Math.min(edgeSoil,edgeRock)/(double)Math.max(1,edgeSoil+edgeRock);
    if(mix<.2)throw new AssertionError("Soil edge still follows a sharp slope contour: mix="+mix);
    System.out.printf("SOIL_OUTCROP_REVISION_PASS land=%d legacyRibbons=%d revision2Ribbons=0 outcrops=%d edgeMix=%.3f legacyReplay=true%n",
        land,ribbons1,outcrops,mix);
  }
  /** Channel revision 1 only raises perched channel beds; stages and dry land replay revision 0. */
  private static void channels() {
    long wet=0,deep0=0,deep1=0,raised=0;double worst0=0,worst1=0;
    for(long seed:new long[]{0,73,20261020}) {
      var legacy=new TerrainEngine(seed,7);var cascade=new TerrainEngine(seed,7,TerrainOptions.DEFAULT,1);
      if(!legacy.sample(-17,31).equals(new TerrainEngine(seed,7,TerrainOptions.DEFAULT,0).sample(-17,31)))
        throw new AssertionError("Channel revision 0 replay");
      for(var p:legacy.waterways(0,0).probes())
        for(int dz=-6;dz<=6;dz+=2)for(int dx=-6;dx<=6;dx+=2){
          double x=Math.floor(p.x())+dx,z=Math.floor(p.z())+dz;
          var a=legacy.column(x,z);var b=cascade.column(x,z);
          if(a.wet()!=b.wet()||a.wet()&&a.water()!=b.water())throw new AssertionError("Channel revision moved a stage "+x+","+z);
          if(!a.wet()){if(a.ground()!=b.ground())throw new AssertionError("Channel revision moved dry land "+x+","+z);continue;}
          if(b.ground()<a.ground())throw new AssertionError("Channel revision deepened a bed");
          if(b.ground()>a.ground())raised++;
          wet++;double d0=a.water()-a.ground(),d1=b.water()-b.ground();
          boolean basin=a.ground()>=legacy.raw(x,z)-1e-9; // natural lake basin, not a cut channel
          if(!basin&&p.stage()>TerrainEngine.SEA+2&&b.speed()>.5){worst0=Math.max(worst0,d0);worst1=Math.max(worst1,d1);if(d0>5)deep0++;if(d1>5)deep1++;}
        }
    }
    var site=new TerrainEngine(20261020,7,TerrainOptions.DEFAULT,1).column(-1648,-743);
    if(site.water()-site.ground()>4)throw new AssertionError("Reproduced perched creek trough remains "+site);
    if(raised==0||worst1>=worst0||deep1*4>deep0)throw new AssertionError("Steep channel beds not bounded: worst "+worst0+"->"+worst1+" deep "+deep0+"->"+deep1);
    System.out.println("CHANNEL_REVISION_PASS wetColumns="+wet+" raisedBeds="+raised+" steepWorstDepth="+worst0+"->"+worst1
        +" steepDeepColumns="+deep0+"->"+deep1+" reproducedSiteDepth="+(site.water()-site.ground()));
  }
}
