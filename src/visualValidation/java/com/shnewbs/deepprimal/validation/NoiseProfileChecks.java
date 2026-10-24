package com.shnewbs.deepprimal.validation;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.shnewbs.deepprimal.engine.NoiseProfileCodec;
import com.shnewbs.deepprimal.core.density.NoiseSpec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value="deepprimal",dist=Dist.DEDICATED_SERVER)
public final class NoiseProfileChecks {
  public NoiseProfileChecks(){NeoForge.EVENT_BUS.register(this);}
  @SubscribeEvent public void started(net.neoforged.neoforge.event.server.ServerStartedEvent event) {
    if(System.getenv("DEEPPRIMAL_DENSITY_CHUNK_NATIVE_CHECK")!=null) {
      DensityChunkNativeChecks.run(event.getServer().overworld());
      if(System.getenv("DEEPPRIMAL_NOISE_CODEC_CHECK")==null)event.getServer().halt(false);
    }
    if(System.getenv("DEEPPRIMAL_NOISE_CODEC_CHECK")==null)return;
    int count=0;
    for(long salt:new long[]{0,1,-1,9007199254740993L,Long.MIN_VALUE,Long.MAX_VALUE}) {
      var spec=new NoiseSpec(.01,.02,.03,4,2,.5,salt);
      var json=NoiseProfileCodec.CODEC.encodeStart(JsonOps.INSTANCE,spec).getOrThrow();
      if(!NoiseProfileCodec.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow().equals(spec))throw new AssertionError("profile roundtrip");count++;
    }
    if(!NoiseProfileCodec.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString("{}")).getOrThrow().equals(NoiseSpec.DEFAULT))throw new AssertionError("defaults");count++;
    for(String bad:new String[]{"{\"octaves\":1.5}","{\"octaves\":0}","{\"octaves\":9}","{\"gain\":2}",
        "{\"frequency_x\":0}","{\"frequency_x\":1,\"octaves\":8}","{\"octaves\":\"4\"}","{\"salt\":1}",
        "{\"salt\":\"9223372036854775808\"}","{\"salt\":\"1.5\"}","{\"octavez\":4}","{\"gain\":true}","[]","{\"frequency_y\":1e999}"}) {
      if(NoiseProfileCodec.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString(bad)).result().isPresent())throw new AssertionError("invalid profile accepted "+bad);count++;
    }
    System.out.println("NOISE_PROFILE_CODEC_PASS cases="+count+" longSaltExact=true invalidRejected=true");
    DensityGraphProfileChecks.run();
  }
}
