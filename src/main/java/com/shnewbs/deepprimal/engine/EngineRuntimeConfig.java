package com.shnewbs.deepprimal.engine;
import net.neoforged.neoforge.common.ModConfigSpec;
/** Limits for the world-scoped query facade; never rewrites saved generation profiles. */
public final class EngineRuntimeConfig {
  public static final ModConfigSpec SPEC;
  public static final ModConfigSpec.IntValue DENSITY_MB,MESH_MB,DENSITY_ENTRIES,MESH_ENTRIES;
  static {var b=new ModConfigSpec.Builder();
    DENSITY_MB=b.comment("Maximum query occupancy cache MiB per engine dimension. Zero disables caching.").defineInRange("densityCacheMiB",16,0,256);
    MESH_MB=b.comment("Maximum query mesh cache MiB per engine dimension. Zero disables caching.").defineInRange("meshCacheMiB",32,0,512);
    DENSITY_ENTRIES=b.defineInRange("densityCacheEntries",128,1,4096);MESH_ENTRIES=b.defineInRange("meshCacheEntries",64,1,2048);SPEC=b.build();}
  private EngineRuntimeConfig(){}
}
