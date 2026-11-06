package com.shnewbs.deepprimal.validation;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.shnewbs.deepprimal.engine.EngineChunkGenerator;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.*;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.biome.Biome;

/** Actual Minecraft codecs and production adapter, without client/loader API doubles. */
public final class OrganicChannelCodecChecks {
  public static void main(String[] args) {
    SharedConstants.tryDetectVersion(); Bootstrap.bootStrap();
    var lookup=VanillaRegistries.createLookup();
    var biomes=new MappedRegistry<Biome>(Registries.BIOME,Lifecycle.stable());
    lookup.lookupOrThrow(Registries.BIOME).listElements()
        .forEach(h->biomes.register(h.key(),h.value(),RegistrationInfo.BUILT_IN));
    biomes.freeze();
    var ops=RegistryOps.create(JsonOps.INSTANCE,
        new RegistryAccess.ImmutableRegistryAccess(java.util.List.of(biomes)).freeze());
    var input=JsonParser.parseString("""
      {"biome_source":{"type":"minecraft:fixed","biome":"minecraft:plains"},
       "min_y":-128,"height":768,"material_revision":6,"channel_revision":10,
       "terrain":{"deposits":[]}}
      """).getAsJsonObject();
    int checks=0;
    for (int revision=0;revision<=10;revision++) {
      input.addProperty("channel_revision",revision);
      var generator=EngineChunkGenerator.CODEC_V7.codec().parse(ops,input).getOrThrow();
      if (generator.channelRevision()!=revision || generator.geography().channelRevision!=revision)
        throw new AssertionError("Decoded generator/engine revision "+revision);
      checks++;
      var encoded=EngineChunkGenerator.CODEC_V7.codec().encodeStart(ops,generator).getOrThrow();
      var reopened=EngineChunkGenerator.CODEC_V7.codec().parse(ops,encoded).getOrThrow();
      if (reopened.channelRevision()!=revision || reopened.getMinY()!=-128 || reopened.getGenDepth()!=768)
        throw new AssertionError("Saved generator revision/height "+revision);
      checks++;
    }
    for (int rejected:new int[] {-1,11}) {
      input.addProperty("channel_revision",rejected);
      if (EngineChunkGenerator.CODEC_V7.codec().parse(ops,input).error().isEmpty())
        throw new AssertionError("Admitted invalid channel revision "+rejected);
      checks++;
    }
    System.out.println("ORGANIC_CHANNEL_CODEC_PASS checks="+checks+" revisions=0..10 invalid=-1,11");
  }
}
