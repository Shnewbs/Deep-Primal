package com.shnewbs.deepprimal.core;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Datapack-loadable configuration for water, terrain, soil, and vegetation parameters.
 * Values can be overridden by datapacks placing a file at data/deepprimal/config/water_terrain.json
 */
public final class WaterTerrainConfig {
  public static final WaterTerrainConfig DEFAULTS = new WaterTerrainConfig();

  public final OceanConfig ocean;
  public final LakeConfig lake;
  public final RiverConfig river;
  public final SoilConfig soil;
  public final MountainStrataConfig mountainStrata;
  public final VegetationConfig vegetation;

  private WaterTerrainConfig() {
    this.ocean = new OceanConfig();
    this.lake = new LakeConfig();
    this.river = new RiverConfig();
    this.soil = new SoilConfig();
    this.mountainStrata = new MountainStrataConfig();
    this.vegetation = new VegetationConfig();
  }

  private WaterTerrainConfig(OceanConfig ocean, LakeConfig lake, RiverConfig river,
      SoilConfig soil, MountainStrataConfig mountainStrata, VegetationConfig vegetation) {
    this.ocean = ocean;
    this.lake = lake;
    this.river = river;
    this.soil = soil;
    this.mountainStrata = mountainStrata;
    this.vegetation = vegetation;
  }

  public static WaterTerrainConfig load(Path configDir) {
    Path configFile = configDir.resolve("data/deepprimal/config/water_terrain.json");
    if (!Files.exists(configFile)) {
      return DEFAULTS;
    }
    try (JsonReader reader = new JsonReader(new InputStreamReader(Files.newInputStream(configFile)))) {
      Gson gson = new GsonBuilder().create();
      JsonObject obj = gson.fromJson(reader, JsonObject.class);
      return parse(obj);
    } catch (Exception e) {
      System.err.println("[DeepPrimal] Failed to load water_terrain config: " + e.getMessage());
      return DEFAULTS;
    }
  }

  private static WaterTerrainConfig parse(JsonObject obj) {
    OceanConfig ocean = parseOcean(obj.getAsJsonObject("ocean"));
    LakeConfig lake = parseLake(obj.getAsJsonObject("lake"));
    RiverConfig river = parseRiver(obj.getAsJsonObject("river"));
    SoilConfig soil = parseSoil(obj.getAsJsonObject("soil"));
    MountainStrataConfig mountainStrata = parseMountainStrata(obj.getAsJsonObject("mountain_strata"));
    VegetationConfig vegetation = parseVegetation(obj.getAsJsonObject("vegetation"));
    return new WaterTerrainConfig(ocean, lake, river, soil, mountainStrata, vegetation);
  }

  private static OceanConfig parseOcean(JsonObject obj) {
    if (obj == null) return new OceanConfig();
    OceanConfig c = new OceanConfig();
    c.baseDepthMultiplier = getDouble(obj, "base_depth_multiplier", c.baseDepthMultiplier);
    c.trenchDepthMultiplier = getDouble(obj, "trench_depth_multiplier", c.trenchDepthMultiplier);
    c.minimumFloorOffset = getDouble(obj, "minimum_floor_offset", c.minimumFloorOffset);
    return c;
  }

  private static LakeConfig parseLake(JsonObject obj) {
    if (obj == null) return new LakeConfig();
    LakeConfig c = new LakeConfig();
    c.baseDepth = getDouble(obj, "base_depth", c.baseDepth);
    c.cellDepthMultiplier = getDouble(obj, "cell_depth_multiplier", c.cellDepthMultiplier);
    c.spillDepthMultiplier = getDouble(obj, "spill_depth_multiplier", c.spillDepthMultiplier);
    c.minDepth = getDouble(obj, "min_depth", c.minDepth);
    c.maxDepth = getDouble(obj, "max_depth", c.maxDepth);
    return c;
  }

  private static RiverConfig parseRiver(JsonObject obj) {
    if (obj == null) return new RiverConfig();
    RiverConfig c = new RiverConfig();
    c.depthMultiplier = getDouble(obj, "depth_multiplier", c.depthMultiplier);
    c.channelRevision = getInt(obj, "channel_revision", c.channelRevision);
    return c;
  }

  private static SoilConfig parseSoil(JsonObject obj) {
    if (obj == null) return new SoilConfig();
    SoilConfig c = new SoilConfig();
    c.shoreSandWeightGroundScale = getDouble(obj, "shore_sand_weight_ground_scale", c.shoreSandWeightGroundScale);
    c.shoreSandWeightGroundCenter = getDouble(obj, "shore_sand_weight_ground_center", c.shoreSandWeightGroundCenter);
    c.shoreSandWeightRainScale = getDouble(obj, "shore_sand_weight_rain_scale", c.shoreSandWeightRainScale);
    c.shoreSandWeightRainCenter = getDouble(obj, "shore_sand_weight_rain_center", c.shoreSandWeightRainCenter);
    c.shoreSandWeightTempScale = getDouble(obj, "shore_sand_weight_temp_scale", c.shoreSandWeightTempScale);
    c.shoreSandWeightTempCenter = getDouble(obj, "shore_sand_weight_temp_center", c.shoreSandWeightTempCenter);
    c.shoreSandWeightTempFactor = getDouble(obj, "shore_sand_weight_temp_factor", c.shoreSandWeightTempFactor);
    return c;
  }

  private static MountainStrataConfig parseMountainStrata(JsonObject obj) {
    if (obj == null) return new MountainStrataConfig();
    MountainStrataConfig c = new MountainStrataConfig();
    c.minSurfaceY = getInt(obj, "min_surface_y", c.minSurfaceY);
    c.minSlope = getDouble(obj, "min_slope", c.minSlope);
    c.maxDepthBelowSurface = getInt(obj, "max_depth_below_surface", c.maxDepthBelowSurface);
    c.bandThicknessBase = getDouble(obj, "band_thickness_base", c.bandThicknessBase);
    c.bandThicknessVariation = getDouble(obj, "band_thickness_variation", c.bandThicknessVariation);
    c.bandOffsetFaultScale = getDouble(obj, "band_offset_fault_scale", c.bandOffsetFaultScale);
    c.bandOffsetNoiseScale = getDouble(obj, "band_offset_noise_scale", c.bandOffsetNoiseScale);
    return c;
  }

  private static VegetationConfig parseVegetation(JsonObject obj) {
    if (obj == null) return new VegetationConfig();
    VegetationConfig c = new VegetationConfig();
    c.emergentPlantChance = getDouble(obj, "emergent_plant_chance", c.emergentPlantChance);
    c.submergedPlantChance = getDouble(obj, "submerged_plant_chance", c.submergedPlantChance);
    c.floatingPlantChance = getDouble(obj, "floating_plant_chance", c.floatingPlantChance);
    c.waterRootedTreeChance = getDouble(obj, "water_rooted_tree_chance", c.waterRootedTreeChance);
    c.propRootChance = getDouble(obj, "prop_root_chance", c.propRootChance);
    c.maxWaterDepthForRoots = getInt(obj, "max_water_depth_for_roots", c.maxWaterDepthForRoots);
    return c;
  }

  private static double getDouble(JsonObject obj, String key, double defaultValue) {
    return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsDouble() : defaultValue;
  }

  private static int getInt(JsonObject obj, String key, int defaultValue) {
    return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsInt() : defaultValue;
  }

  public static class OceanConfig {
    public double baseDepthMultiplier = 70.0;
    public double trenchDepthMultiplier = 150.0;
    public double minimumFloorOffset = 40.0;
  }

  public static class LakeConfig {
    public double baseDepth = 8.0;
    public double cellDepthMultiplier = 1.5;
    public double spillDepthMultiplier = 0.2;
    public double minDepth = 10.0;
    public double maxDepth = 50.0;
  }

  public static class RiverConfig {
    public double depthMultiplier = 1.0;
    public int channelRevision = 8;
  }

  public static class SoilConfig {
    public double shoreSandWeightGroundScale = 12.0;
    public double shoreSandWeightGroundCenter = 97.0;
    public double shoreSandWeightRainScale = 0.16;
    public double shoreSandWeightRainCenter = 0.28;
    public double shoreSandWeightTempScale = 0.2;
    public double shoreSandWeightTempCenter = 0.42;
    public double shoreSandWeightTempFactor = 0.5;
  }

  public static class MountainStrataConfig {
    public int minSurfaceY = 130;
    public double minSlope = 0.6;
    public int maxDepthBelowSurface = 150;
    public double bandThicknessBase = 16.0;
    public double bandThicknessVariation = 8.0;
    public double bandOffsetFaultScale = 15.0;
    public double bandOffsetNoiseScale = 4.0;
  }

  public static class VegetationConfig {
    public double emergentPlantChance = 0.008;
    public double submergedPlantChance = 0.015;
    public double floatingPlantChance = 0.015;
    public double waterRootedTreeChance = 0.008;
    public double propRootChance = 0.6;
    public int maxWaterDepthForRoots = 3;
  }
}