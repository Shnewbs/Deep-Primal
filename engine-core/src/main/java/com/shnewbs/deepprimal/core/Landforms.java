package com.shnewbs.deepprimal.core;

import static com.shnewbs.deepprimal.core.TerrainNoise.*;

/** V7 regional landforms. Drainage is planned after this continuous height field. */
public final class Landforms {
  private final TerrainNoise noise;
  private final TerrainOptions options;
  public Landforms(TerrainNoise noise, TerrainOptions options) {
    this.noise = noise; this.options = options;
  }
  public double relief(double x, double z) {
    double region = Math.clamp(.5 + noise.field(x,z,1800,4200)*1.1,0,1);
    double hills = smooth((region-.20)/.22);
    double uplands = smooth((region-.48)/.20);
    double mountains = smooth((region-.69)/.19);
    double broad = noise.field(x,z,650,4205), detail = noise.field(x,z,110,4208);
    double lowland = 11 + broad*7 + detail*2.5;
    double rolling = 29 + broad*25 + detail*7;
    // Eroded plateau rims are a different shape from rounded hills or folded ranges.
    double mesa = smooth((noise.field(x,z,330,4210)+.12)/.30);
    double plateau = 48 + mesa*56 + broad*15 + detail*3;
    double wx=x+noise.field(x,z,420,4212)*150;
    double wz=z+noise.field(x,z,420,4213)*150;
    double ridge=1-Math.abs(noise.field(wx,wz,700,4214));
    double spurs=1-Math.abs(noise.field(wx,wz,220,4218));
    double range=55 + options.mountainScale()*(Math.pow(ridge,4)*155 + Math.pow(spurs,5)*24);
    return lerp(lerp(lerp(lowland,rolling,hills),plateau,uplands),range,mountains);
  }
}
