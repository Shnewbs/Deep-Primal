package com.shnewbs.deepprimal.validation;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.shnewbs.deepprimal.engine.GroundSurfaceProfile;
import java.util.ArrayList;
import java.util.List;

final class GroundSurfaceProfileChecks {
  static void run() {
    int checks = 0;
    var materials = new ArrayList<>(List.of(GroundSurfaceProfile.Material.DIRT,
        GroundSurfaceProfile.Material.GRASS, GroundSurfaceProfile.Material.PODZOL));
    var boundaries = new ArrayList<>(List.of(.35, .7));
    var template = new GroundSurfaceProfile(
        1, materials, boundaries, .04, 96, 34001, 4, "eroded", 0, 1);
    materials.clear();
    boundaries.clear();
    require(template.materials().size() == 3 && template.boundaries().size() == 2,
        "Defensive profile snapshot");
    checks++;
    try {
      template.boundaries().set(0, .4);
      throw new AssertionError("Mutable profile boundaries");
    } catch (UnsupportedOperationException expected) {
      checks++;
    }
    try {
      template.materials().clear();
      throw new AssertionError("Mutable profile materials");
    } catch (UnsupportedOperationException expected) {
      checks++;
    }
    for (long salt : new long[] {0, 1, -1, 9007199254740993L, Long.MIN_VALUE, Long.MAX_VALUE}) {
      var profile = new GroundSurfaceProfile(1, template.materials(), template.boundaries(),
          template.variation(), template.scale(), salt, template.soilDepth(),
          template.channel(), template.minimum(), template.maximum());
      var json = GroundSurfaceProfile.CODEC.encodeStart(JsonOps.INSTANCE, profile).getOrThrow();
      var decoded = GroundSurfaceProfile.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
      require(decoded.equals(profile), "Exact salt and full profile roundtrip");
      checks++;
    }
    JsonObject valid = GroundSurfaceProfile.CODEC.encodeStart(JsonOps.INSTANCE, template)
        .getOrThrow().getAsJsonObject();
    var invalid = new ArrayList<JsonElement>();
    invalid.add(JsonNull.INSTANCE);
    invalid.add(JsonParser.parseString("[]"));
    for (String key : valid.keySet()) {
      var removed = valid.deepCopy();
      removed.remove(key);
      invalid.add(removed);
      var nulled = valid.deepCopy();
      nulled.add(key, JsonNull.INSTANCE);
      invalid.add(nulled);
    }
    for (String[] edit : new String[][] {
        {"version", "2"}, {"version", "1.00000000000000000001"}, {"version", "\"1\""},
        {"soil_depth", "0"}, {"soil_depth", "17"}, {"soil_depth", "2.25"},
        {"channel", "\"absent field\""}, {"channel", "true"},
        {"minimum", "1"}, {"minimum", "-1e13"}, {"maximum", "1e999"},
        {"variation", "-0.1"}, {"variation", "0.26"}, {"variation", "\"0.04\""},
        {"scale", "7"}, {"scale", "8193"}, {"scale", "true"},
        {"salt", "1"}, {"salt", "\"1.5\""}, {"salt", "\"9223372036854775808\""},
        {"materials", "[]"}, {"materials", "[\"deepprimal:ground_grass\"]"},
        {"materials", "[\"minecraft:dirt\",\"deepprimal:ground_grass\"]"},
        {"materials", "[true,\"deepprimal:ground_grass\"]"},
        {"boundaries", "[]"}, {"boundaries", "[0.7,0.35]"},
        {"boundaries", "[0,0.7]"}, {"boundaries", "[0.35,1]"},
        {"boundaries", "[0.35]"}, {"boundaries", "[0.35,\"0.7\"]"},
        {"boundaries", "[0.35,1e999]"}, {"unexpected", "1"}}) {
      var changed = valid.deepCopy();
      changed.add(edit[0], JsonParser.parseString(edit[1]));
      invalid.add(changed);
    }
    for (var bad : invalid) {
      var result = GroundSurfaceProfile.CODEC.parse(JsonOps.INSTANCE, bad);
      require(result.error().isPresent() && result.result().isEmpty(),
          "Invalid profile accepted: " + bad);
      checks++;
    }
    var maximumMaterials = new ArrayList<GroundSurfaceProfile.Material>();
    var maximumBoundaries = new ArrayList<Double>();
    for (int i = 0; i < 64; i++) {
      maximumMaterials.add(template.materials().get(i % 3));
      if (i > 0) maximumBoundaries.add(i / 64.);
    }
    var maximum = new GroundSurfaceProfile(
        1, maximumMaterials, maximumBoundaries, .25, 8192, Long.MIN_VALUE, 16, "x", -1, 1);
    require(GroundSurfaceProfile.CODEC.parse(JsonOps.INSTANCE,
        GroundSurfaceProfile.CODEC.encodeStart(JsonOps.INSTANCE, maximum).getOrThrow())
        .getOrThrow().equals(maximum), "Maximum bounded profile");
    checks++;
    var overBudget = valid.deepCopy();
    var oversized = new com.google.gson.JsonArray();
    for (int i = 0; i < 65; i++) oversized.add("deepprimal:ground_dirt");
    overBudget.add("materials", oversized);
    require(GroundSurfaceProfile.CODEC.parse(JsonOps.INSTANCE, overBudget).error().isPresent(),
        "Oversize profile rejected");
    checks++;
    System.out.println("GROUND_SURFACE_PROFILE_PASS checks=" + checks + " rejected=" + invalid.size()
        + " immutable=true exactLongSalts=true bounded=true");
  }

  private static void require(boolean condition, String message) {
    if (!condition) throw new AssertionError(message);
  }

  private GroundSurfaceProfileChecks() {}
}
