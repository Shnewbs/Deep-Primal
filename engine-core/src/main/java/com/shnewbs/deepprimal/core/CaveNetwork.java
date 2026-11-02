package com.shnewbs.deepprimal.core;

/** Geology-controlled continuous cave field with a protected surface roof and finite perched water. */
public final class CaveNetwork {
  public static final int ROOF=10;
  private final TerrainNoise noise;
  public CaveNetwork(TerrainNoise noise) { this.noise=noise; }
  public final class Column {
    private final int low,high,first;
    private final double[] a,b,room;
    private final Geology.Column geology;
    public final int waterTable;
    Column(int x,int z,double surface,int minY,int maxY,Geology.Column geology) {
      this.geology=geology;low=minY+12;high=Math.min(maxY,(int)Math.floor(surface)-ROOF);
      first=Math.floorDiv(low,48);int count=Math.max(2,Math.floorDiv(Math.max(low,high),48)-first+2);
      if(count>90)throw new IllegalArgumentException("Cave height outside engine bounds");
      a=new double[count];b=new double[count];room=new double[count];
      for(int i=0;i<count;i++) {
        long slice=(long)(first+i)*7919;
        a[i]=noise.noise(x,z,48,3200+slice);b[i]=noise.noise(x,z,61,3210+slice);
        room[i]=noise.noise(x,z,90,3220+slice);
      }
      waterTable=(int)Math.floor(TerrainEngine.SEA-28+noise.field(x,z,700,3230)*18);
    }
    private double field(double[] values,int y) {
      double q=y/48.;int i=(int)Math.floor(q)-first;
      return TerrainNoise.lerp(values[i],values[i+1],TerrainNoise.smooth(q-Math.floor(q)));
    }
    public boolean contains(int y) {
      if(y<low||y>high)return false;
      var rock=geology.rock(y);
      double x=field(a,y),z=field(b,y),chamber=field(room,y);
      double width=rock==Geology.Rock.CALCITE?.029:rock==Geology.Rock.SANDSTONE?.014:.007;
      if(geology.permeability()>.7)width*=1.35;
      // Chamber expansion stays in soluble host; intact surface cover is never carved away.
      return x*x+z*z<width || rock==Geology.Rock.CALCITE && x*x+z*z<.09 && chamber>.33;
    }
    public boolean flooded(int y) { return y<waterTable && geology.permeability()<.75 && contains(y); }
  }
  public Column column(int x,int z,double surface,int minY,int maxY,Geology.Column geology) {
    return new Column(x,z,surface,minY,maxY,geology);
  }
}
