package com.shnewbs.deepprimal.engine;

import com.google.gson.*;
import com.mojang.serialization.*;
import com.shnewbs.deepprimal.core.density.*;
import java.util.*;

/** Strict, bounded JSON graph codec. The resulting definition is an inline immutable snapshot. */
public final class DensityGraphCodec {
  public static final Codec<DensityGraph.Definition> CODEC=Codec.PASSTHROUGH.comapFlatMap(dynamic->{
    try {var result=decode(dynamic.convert(JsonOps.INSTANCE).getValue());result.compile(0);return DataResult.success(result);}
    catch(IllegalArgumentException|IllegalStateException e) {return DataResult.error(()->"density graph: "+e.getMessage());}
  },definition->new Dynamic<>(JsonOps.INSTANCE,encode(definition)));

  private static DensityGraph.Definition decode(JsonElement json) {
    JsonObject root=object(json,"$");keys(root,Set.of("version","root","nodes"),"$");
    int version=integer(root.get("version"),"$.version");String output=string(root.get("root"),"$.root");
    if(!root.has("nodes")||!root.get("nodes").isJsonArray())throw new IllegalArgumentException("$.nodes must be an array");
    JsonArray array=root.getAsJsonArray("nodes");
    if(array.isEmpty()||array.size()>DensityGraph.MAX_NODES)throw new IllegalArgumentException("$.nodes needs 1..128 entries");
    var nodes=new ArrayList<DensityGraph.Node>();
    for(int i=0;i<array.size();i++) {
      String path="$.nodes["+i+"]";JsonObject n=object(array.get(i),path);
      keys(n,Set.of("id","op","inputs","parameters","noise"),path);
      String id=string(n.get("id"),path+".id"),name=string(n.get("op"),path+".op");
      DensityGraph.Op op;try {op=DensityGraph.Op.valueOf(name.toUpperCase(Locale.ROOT));}catch(IllegalArgumentException e){throw new IllegalArgumentException(path+".op unknown operation "+name);}
      var inputs=new ArrayList<String>();var parameters=new ArrayList<Double>();
      if(n.has("inputs")) {if(!n.get("inputs").isJsonArray())throw new IllegalArgumentException(path+".inputs must be an array");
        var a=n.getAsJsonArray("inputs");if(a.size()>4)throw new IllegalArgumentException(path+".inputs exceeds arity budget");
        for(var v:a)inputs.add(string(v,path+".inputs"));}
      if(n.has("parameters")) {if(!n.get("parameters").isJsonArray())throw new IllegalArgumentException(path+".parameters must be an array");
        var a=n.getAsJsonArray("parameters");if(a.size()>2)throw new IllegalArgumentException(path+".parameters exceeds arity budget");
        for(var v:a)parameters.add(number(v,path+".parameters"));}
      NoiseSpec noise=null;
      if(n.has("noise")) {var decoded=NoiseProfileCodec.CODEC.parse(JsonOps.INSTANCE,n.get("noise"));
        noise=decoded.result().orElseThrow(()->new IllegalArgumentException(path+".noise: "+decoded.error().orElseThrow().message()));}
      try {nodes.add(new DensityGraph.Node(id,op,inputs,parameters,noise));}
      catch(IllegalArgumentException e){throw new IllegalArgumentException(path+": "+e.getMessage(),e);}
    }
    return new DensityGraph.Definition(version,output,nodes);
  }

  private static JsonElement encode(DensityGraph.Definition definition) {
    var root=new JsonObject();root.addProperty("version",definition.version());root.addProperty("root",definition.root());var nodes=new JsonArray();
    for(var n:definition.nodes()) {
      var j=new JsonObject();j.addProperty("id",n.id());j.addProperty("op",n.op().name().toLowerCase(Locale.ROOT));
      if(!n.inputs().isEmpty()){var a=new JsonArray();n.inputs().forEach(a::add);j.add("inputs",a);}
      if(!n.parameters().isEmpty()){var a=new JsonArray();n.parameters().forEach(a::add);j.add("parameters",a);}
      if(n.noise()!=null)j.add("noise",NoiseProfileCodec.CODEC.encodeStart(JsonOps.INSTANCE,n.noise()).getOrThrow());
      nodes.add(j);
    }
    root.add("nodes",nodes);return root;
  }
  private static JsonObject object(JsonElement j,String path) {if(j==null||!j.isJsonObject())throw new IllegalArgumentException(path+" must be an object");return j.getAsJsonObject();}
  private static void keys(JsonObject j,Set<String> allowed,String path) {for(var key:j.keySet())if(!allowed.contains(key))throw new IllegalArgumentException(path+" unknown key "+key);}
  private static String string(JsonElement j,String path) {if(j==null||!j.isJsonPrimitive()||!j.getAsJsonPrimitive().isString())throw new IllegalArgumentException(path+" must be a string");return j.getAsString();}
  private static double number(JsonElement j,String path) {if(j==null||!j.isJsonPrimitive()||!j.getAsJsonPrimitive().isNumber()||!Double.isFinite(j.getAsDouble()))throw new IllegalArgumentException(path+" must be a finite number");return j.getAsDouble();}
  private static int integer(JsonElement j,String path) {double v=number(j,path);if(v!=Math.rint(v)||v<Integer.MIN_VALUE||v>Integer.MAX_VALUE)throw new IllegalArgumentException(path+" must be an integer");return (int)v;}
  private DensityGraphCodec() {}
}
