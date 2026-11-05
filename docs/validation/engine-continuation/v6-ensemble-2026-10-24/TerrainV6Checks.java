package com.shnewbs.deepprimal.core;

import java.util.*;
/** Independent invariants, sampled across climate belts and signed region boundaries. */
public final class TerrainV6Checks {
  static void require(boolean b,String message){if(!b)throw new AssertionError(message);}
  public static void main(String[] args) {run();}
  public static void run() {
    long columns=0,caves=0,ice=0,tunnels=0;var biomes=new TreeSet<Integer>();
    for(long seed:new long[]{20261020,73}) {
      var t=new TerrainEngine(seed,6);var geology=new Geology(t.noise);var network=new CaveNetwork(t.noise);
      for(int cz:new int[]{0,1,2,-1})for(int z=-2048;z<=2048;z+=128)for(int x=-2048;x<=2048;x+=128) {
        int wz=cz*8192+z;var s=t.sample(x,wz);var h=t.hydraulicColumn(x,wz);
        require(Double.isFinite(s.ground())&&s.ground()>-128&&s.ground()<640,"bounded standard height");
        require(!s.wet()||s.water()>s.ground(),"positive water depth");
        require(s.slope()>=0&&Double.isFinite(s.slope()),"finite slope");
        if(t.tunnel(x,wz)) {tunnels++;require(!s.wet()&&s.ground()-h.water()>=7,"supported hidden river");}
        if(t.landscape.ice(x,wz,s)>0)ice++;
        biomes.add(t.biome(x,wz));
        require(t.biome(x,wz)==t.biome(x,s.ground()+4,wz),"surface biome agrees between 2D and 3D");
        var g=geology.column(x,wz,s);var soil=SoilProfile.at(s,g);
        require(soil.depth()>=0&&soil.depth()<=10,"bounded soil");
        double roof=h.wet()?Math.min(s.ground(),h.ground()):s.ground();
        var c=network.column(x,wz,roof,-128,639,g);
        for(int y=-128;y<=(int)s.ground()+1;y++) {
          if(c.contains(y)){caves++;require(y<=Math.floor(roof)-10&&y>=-116,"intact cave cover and bedrock");}
          if(c.flooded(y))require(c.contains(y)&&y<c.waterTable,"finite initial cave water");
        }
        columns++;
      }
      // Narrow waterways need a centerline transect in addition to the unbiased climate grid.
      var route=t.waterways(0,0).centerline();
      for(int i=0;i<route.size();i+=8) {
        int x=(int)Math.round(route.get(i).x()),z=(int)Math.round(route.get(i).z());
        var h=t.hydraulicColumn(x,z);
        if(t.tunnel(x,z)) {tunnels++;require(t.column(x,z).ground()-h.water()>=7,"tunnel roof");}
      }
      // Chunk order, parallel access and cache churn may not alter geography.
      int[][] points={{-1,-1},{0,0},{8191,8192},{102,138},{-1250,8200}};
      var original=Arrays.stream(points).map(p->t.sample(p[0],p[1])).toList();
      Arrays.stream(points).parallel().forEach(p->require(t.sample(p[0],p[1]).equals(new TerrainEngine(seed,6).sample(p[0],p[1])),"parallel deterministic sample"));
      for(int i=0;i<8;i++)t.column(i*8192+23,123);
      for(int i=0;i<points.length;i++)require(original.get(i).equals(t.sample(points[i][0],points[i][1])),"eviction preserves geography");
    }
    require(caves>1000&&ice>100&&tunnels>0&&biomes.size()>=20,"feature coverage: "+caves+" "+ice+" "+tunnels+" "+biomes);
    System.out.println("TERRAIN_V6_CORE_PASS columns="+columns+" caves="+caves+" icyColumns="+ice+" tunnelColumns="+tunnels+" biomes="+biomes);
  }
}
