package com.shnewbs.deepprimal.core;

import static com.shnewbs.deepprimal.core.TerrainNoise.*;

/** V6 morphology and climate. All fields are immutable, seeded and independent of chunk order. */
public final class Landscape {
  private final TerrainNoise noise;
  private final TerrainOptions options;
  public Landscape(TerrainNoise noise, TerrainOptions options) { this.noise = noise; this.options = options; }
  public double temperature(double x, double z, double height) {
    double latitude = Math.min(1, Math.abs(z) / options.polarDistance());
    return Math.clamp(.94 - .91 * smooth(latitude) + noise.field(x,z,4800,3100)*.17
        - Math.max(0,height-110)/410, 0, 1);
  }
  public double rain(double x, double z) {
    double latitude = Math.abs(z) / options.polarDistance();
    double dryBelt = Math.exp(-Math.pow((latitude-.38)/.15,2))*.20;
    return Math.clamp(.64 + noise.field(x,z,3100,81)*.60 - dryBelt, .08, 1);
  }
  public double wind(double x, double z) {
    return Math.PI*.22 + noise.field(x,z,6000,3101)*.65;
  }
  public double sandSupply(double x, double z, double height) {
    double aridity = smooth((.48-rain(x,z))/.24);
    double coast = smooth((108-height)/10) * smooth((height-95)/5);
    return Math.max(aridity,coast*.65)*smooth((210-height)/60);
  }
  /** Dunes enter the base relief before drainage is planned, so valleys and outlets cut them. */
  public double relief(double x, double z, double height, double inland) {
    double result = height;
    if (options.dunes()) {
      double supply=sandSupply(x,z,height), angle=wind(x,z);
      if(supply>.01) {
        double phase=(x*Math.cos(angle)+z*Math.sin(angle))/27 + noise.field(x,z,170,3102)*2;
        double crest=Math.pow(.5+.5*Math.sin(phase),2.6);
        result += supply*(2+noise.field(x,z,420,3103)*2+crest*7)*inland;
      }
    }
    // Cold valleys widen into troughs; flanking moraines follow the same valley field.
    double cold=smooth((.30-temperature(x,z,height))/.25);
    if(options.glaciers() && cold>.01 && inland>0) {
      double across=Math.abs(noise.field(x,z,650,3110));
      double valley=1-smooth(across/.20);
      double moraine=Math.exp(-Math.pow((across-.24)/.055,2));
      result += cold*inland*(-valley*12+moraine*3);
    }
    return result;
  }
  public boolean tunnel(double x, double z, TerrainEngine.Sample water, double uncut) {
    return options.riverTunnels() && water.wet() && water.speed()>0
        && water.water()>TerrainEngine.SEA+8 && uncut-water.water()>=7;
  }
  public int ice(double x, double z, TerrainEngine.Sample surface) {
    if(!options.glaciers() || surface.wet() || surface.ground()<TerrainEngine.SEA+12) return 0;
    double cold=smooth((.23-temperature(x,z,surface.ground()))/.19);
    return (int)Math.floor(cold*(8+6*(.5+.5*noise.field(x,z,360,3121))));
  }
  public TerrainEngine.Sample surface(double x,double z,TerrainEngine.Sample water,double uncut,boolean support) {
    boolean tunnel=support && tunnel(x,z,water,uncut);
    double ground=tunnel || support&&!water.wet()?Math.max(uncut,water.ground()):water.ground();
    var s=new TerrainEngine.Sample(ground,tunnel?Double.NaN:water.water(),water.rain(),
        temperature(x,z,ground),water.slope(),water.sediment(),water.flowX(),water.flowZ(),water.speed(),water.rock());
    int ice=ice(x,z,s);
    return ice==0?s:new TerrainEngine.Sample(ground+ice,s.water(),s.rain(),s.temperature(),s.slope(),
        s.sediment(),s.flowX(),s.flowZ(),s.speed(),s.rock());
  }
  /** Indices are the serialized V6 40-biome palette; legacy palettes keep their original mapping. */
  public int biome(double x,double z,TerrainEngine.Sample s,boolean ocean) {
    double h=s.ground(),t=s.temperature(),r=s.rain(),patch=noise.field(x,z,1100,3130);
    if(ocean) {
      if(h<45)return t<.18?35:t<.42?34:33;
      return t<.18?32:t<.42?31:t>.8?29:t>.64?30:3;
    }
    if(s.wet()&&s.speed()>0)return t<.22?37:36;
    if(h<100)return t<.25?26:s.slope()>1.4?27:25;
    if(h>260)return t<.26?19:s.slope()>1.2?20:7;
    if(h>210)return t<.3?18:t<.45?17:s.slope()>.9?23:21;
    if(t<.2)return r>.50?16:5;
    if(t<.39)return r>.66?15:4;
    if(h<105&&r>.74)return t>.72?28:6;
    if(r<.32)return patch>.18?9:2;
    if(r<.46)return t>.62?8:0;
    if(t>.73&&r>.70)return patch>.25?11:10;
    if(t>.71&&r>.56)return 12;
    if(r>.77)return 13;
    if(h>165&&patch>.30)return 22;
    if(s.slope()>1.2)return 24;
    if(r>.57)return patch<-.12?14:1;
    return h>170?21:0;
  }
}
