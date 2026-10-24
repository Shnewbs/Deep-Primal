package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.TerrainOptions;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Defaults used only when creating a V6 world without an explicit datapack profile. */
public final class TerrainConfig {
  public static final ModConfigSpec SPEC;
  public static final ModConfigSpec.BooleanValue CAVES,TUNNELS,DUNES,GLACIERS,DEBRIS;
  public static final ModConfigSpec.DoubleValue MOUNTAINS,POLES;
  static {
    var b=new ModConfigSpec.Builder();
    b.comment("Creation-time defaults. Each V6 world saves its effective settings; existing worlds do not change.");
    CAVES=b.define("geologicalCaves",true);TUNNELS=b.define("riverTunnels",true);
    DUNES=b.define("dunes",true);GLACIERS=b.define("glaciers",true);DEBRIS=b.define("naturalDebris",true);
    MOUNTAINS=b.comment("Mountain relief multiplier. New worlds only.").defineInRange("mountainScale",1.3,.5,2);
    POLES=b.comment("Distance from equator to polar climate, in blocks. New worlds only.").defineInRange("polarDistance",16000.,4000,1000000);
    SPEC=b.build();
  }
  public static TerrainProfile creationProfile() {
    return TerrainProfile.defaults(new TerrainOptions(CAVES.get(),TUNNELS.get(),DUNES.get(),GLACIERS.get(),DEBRIS.get(),MOUNTAINS.get(),POLES.get()));
  }
}
