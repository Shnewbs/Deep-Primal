package com.shnewbs.deepprimal.validation;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import com.shnewbs.deepprimal.core.density.*;
import com.shnewbs.deepprimal.engine.DensityGraphCodec;
import java.util.*;

/** Packaged-platform codec checks, not native block generation acceptance. */
public final class DensityGraphProfileChecks {
  public static void run() {
    int count=0;
    for(boolean floating:new boolean[]{false,true}) {
      var definition=TerrainShapeV8.definition(floating);
      var json=DensityGraphCodec.CODEC.encodeStart(JsonOps.INSTANCE,definition).getOrThrow();
      var decoded=DensityGraphCodec.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow();
      if(!decoded.equals(definition))throw new AssertionError("graph definition roundtrip");count++;
      for(long seed:new long[]{0,1,73,Long.MIN_VALUE}) {
        var a=definition.compile(seed);var b=decoded.compile(seed);var sampler=b.newSampler();
        for(int i=0;i<256;i++){double expected=a.sample(i*13-1000,i-128,-i*17);if(Double.doubleToLongBits(expected)!=Double.doubleToLongBits(b.sample(i*13-1000,i-128,-i*17)))throw new AssertionError("graph output roundtrip");if(Double.doubleToLongBits(expected)!=Double.doubleToLongBits(sampler.sample(i*13-1000,i-128,-i*17)))throw new AssertionError("packaged sampler output");}
        count++;
      }
    }
    String valid="{\"version\":1,\"root\":\"out\",\"nodes\":[{\"id\":\"out\",\"op\":\"constant\",\"parameters\":[1]}]}";
    var template=JsonParser.parseString(valid).getAsJsonObject();
    var bad=new ArrayList<JsonElement>();bad.add(JsonParser.parseString("[]"));bad.add(JsonNull.INSTANCE);
    for(String key:List.of("version","root","nodes")){var j=template.deepCopy();j.remove(key);bad.add(j);}
    var j=template.deepCopy();j.addProperty("version",1.5);bad.add(j);
    j=template.deepCopy();j.addProperty("version",2);bad.add(j);
    j=template.deepCopy();j.addProperty("root",true);bad.add(j);
    j=template.deepCopy();j.addProperty("unknown",1);bad.add(j);
    for(String key:List.of("id","op","inputs","parameters","noise","typo")){
      j=template.deepCopy();j.getAsJsonArray("nodes").get(0).getAsJsonObject().add(key,JsonNull.INSTANCE);bad.add(j);
    }
    j=template.deepCopy();j.getAsJsonArray("nodes").get(0).getAsJsonObject().addProperty("op","invented");bad.add(j);
    j=template.deepCopy();j.getAsJsonArray("nodes").get(0).getAsJsonObject().add("parameters",JsonParser.parseString("[1e999]"));bad.add(j);
    j=template.deepCopy();j.getAsJsonArray("nodes").get(0).getAsJsonObject().add("parameters",JsonParser.parseString("[\"1\"]"));bad.add(j);
    j=template.deepCopy();j.getAsJsonArray("nodes").get(0).getAsJsonObject().add("parameters",JsonParser.parseString("[1,2,3]"));bad.add(j);
    j=template.deepCopy();j.getAsJsonArray("nodes").get(0).getAsJsonObject().add("inputs",JsonParser.parseString("[\"a\",\"b\",\"c\",\"d\",\"e\"]"));bad.add(j);
    j=template.deepCopy();j.getAsJsonArray("nodes").add(j.getAsJsonArray("nodes").get(0).deepCopy());bad.add(j);
    j=template.deepCopy();j.addProperty("root","absent");bad.add(j);
    j=template.deepCopy();j.getAsJsonArray("nodes").get(0).getAsJsonObject().addProperty("op","abs");bad.add(j);
    for(var invalid:bad){if(DensityGraphCodec.CODEC.parse(JsonOps.INSTANCE,invalid).result().isPresent())throw new AssertionError("invalid graph accepted: "+invalid);count++;}
    // Preserve a full signed-long salt through the graph's nested JSON codec.
    var n=DensityGraph.Node.noise("out",new NoiseSpec(.01,.01,.01,1,2,.5,Long.MAX_VALUE),false);
    var def=new DensityGraph.Definition(1,"out",List.of(n));
    if(!DensityGraphCodec.CODEC.parse(JsonOps.INSTANCE,DensityGraphCodec.CODEC.encodeStart(JsonOps.INSTANCE,def).getOrThrow()).getOrThrow().equals(def))throw new AssertionError("nested salt");count++;
    System.out.println("DENSITY_GRAPH_CODEC_PASS cases="+count+" roundtripSamples=2048 samplerSamples=2048 invalidRejected="+bad.size());
  }
  private DensityGraphProfileChecks() {}
}
