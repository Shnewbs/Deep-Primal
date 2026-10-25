package com.shnewbs.deepprimal.engine;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.shnewbs.deepprimal.core.GradientPalette;
import com.shnewbs.deepprimal.core.density.DensityGraph;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;
import net.minecraft.world.level.block.state.BlockState;

/** Inline, immutable surface snapshot for the unregistered density preview only. */
public record GroundSurfaceProfile(
    int version,
    List<Material> materials,
    List<Double> boundaries,
    double variation,
    double scale,
    long salt,
    int soilDepth,
    String channel,
    double minimum,
    double maximum) {
  private static final Set<String> KEYS = Set.of(
      "version", "materials", "boundaries", "variation", "scale", "salt",
      "soil_depth", "channel", "minimum", "maximum");

  public enum Material {
    DIRT("deepprimal:ground_dirt"),
    GRASS("deepprimal:ground_grass"),
    PODZOL("deepprimal:ground_podzol");

    private final String id;

    Material(String id) {
      this.id = id;
    }

    private BlockState state() {
      return switch (this) {
        case DIRT -> GroundMaterials.DIRT.get().defaultBlockState();
        case GRASS -> GroundMaterials.GRASS.get().defaultBlockState();
        case PODZOL -> GroundMaterials.PODZOL.get().defaultBlockState();
      };
    }

    private static Material fromId(String id) {
      for (var material : values())
        if (material.id.equals(id)) return material;
      throw new IllegalArgumentException("unknown ground material " + id);
    }
  }

  public static final Codec<GroundSurfaceProfile> CODEC = Codec.PASSTHROUGH.comapFlatMap(
      dynamic -> {
        try {
          return DataResult.success(decode(dynamic.convert(JsonOps.INSTANCE).getValue()));
        } catch (IllegalArgumentException | IllegalStateException | ArithmeticException failure) {
          return DataResult.error(() -> "ground surface profile: " + failure.getMessage());
        }
      },
      profile -> new Dynamic<>(JsonOps.INSTANCE, encode(profile)));

  public GroundSurfaceProfile {
    materials = List.copyOf(materials);
    boundaries = List.copyOf(boundaries);
    if (version != 1) throw new IllegalArgumentException("version must be 1");
    if (soilDepth < 1 || soilDepth > 16)
      throw new IllegalArgumentException("soil_depth must be within 1..16");
    if (channel == null || !channel.matches("[a-z][a-z0-9_]{0,63}"))
      throw new IllegalArgumentException("channel must be a density node ID");
    if (!Double.isFinite(minimum) || !Double.isFinite(maximum)
        || minimum >= maximum || Math.abs(minimum) > 1e12 || Math.abs(maximum) > 1e12)
      throw new IllegalArgumentException("minimum and maximum must increase within +/-1e12");
    gradient(0, materials.size(), boundaries, variation, scale, salt);
  }

  /** The supplied seed must be the same world seed used to compile the density graph. */
  public Compiled compile(long seed, DensityGraph graph) {
    Objects.requireNonNull(graph, "graph");
    graph.channel(channel, 0, 0, 0);
    return new Compiled(this, seed, graph);
  }

  public static final class Compiled {
    private final GroundSurfaceProfile profile;
    private final DensityGraph graph;
    private final GradientPalette gradient;
    private final List<BlockState> states;

    private Compiled(GroundSurfaceProfile profile, long seed, DensityGraph graph) {
      this.profile = profile;
      this.graph = graph;
      gradient = gradient(seed, profile.materials.size(), profile.boundaries,
          profile.variation, profile.scale, profile.salt);
      states = profile.materials.stream().map(Material::state).toList();
    }

    public DensityGraph graph() {
      return graph;
    }

    public int soilDepth() {
      return profile.soilDepth;
    }

    public BlockState surface(int x, int z) {
      double value = graph.channel(profile.channel, x, 0, z);
      double progression = Math.clamp(
          (value - profile.minimum) / (profile.maximum - profile.minimum), 0, 1);
      return states.get(gradient.index(progression, x, z));
    }
  }

  private static GradientPalette gradient(long seed, int size, List<Double> boundaries,
      double variation, double scale, long salt) {
    return new GradientPalette(seed, IntStream.range(0, size).toArray(),
        boundaries.stream().mapToDouble(Double::doubleValue).toArray(), variation, scale, salt);
  }

  private static GroundSurfaceProfile decode(JsonElement value) {
    if (!value.isJsonObject()) throw new IllegalArgumentException("$ must be an object");
    var root = value.getAsJsonObject();
    for (String key : root.keySet())
      if (!KEYS.contains(key)) throw new IllegalArgumentException("$ unknown key " + key);
    var materials = new ArrayList<Material>();
    for (var item : array(root, "materials", 2, 64))
      materials.add(Material.fromId(string(item, "$.materials")));
    var boundaries = new ArrayList<Double>();
    for (var item : array(root, "boundaries", 1, 63))
      boundaries.add(number(item, "$.boundaries"));
    return new GroundSurfaceProfile(
        integer(root.get("version"), "$.version"), materials, boundaries,
        number(root.get("variation"), "$.variation"), number(root.get("scale"), "$.scale"),
        Long.parseLong(string(root.get("salt"), "$.salt")),
        integer(root.get("soil_depth"), "$.soil_depth"),
        string(root.get("channel"), "$.channel"),
        number(root.get("minimum"), "$.minimum"), number(root.get("maximum"), "$.maximum"));
  }

  private static JsonObject encode(GroundSurfaceProfile profile) {
    var root = new JsonObject();
    root.addProperty("version", profile.version);
    var materials = new JsonArray();
    profile.materials.forEach(material -> materials.add(material.id));
    root.add("materials", materials);
    var boundaries = new JsonArray();
    profile.boundaries.forEach(boundaries::add);
    root.add("boundaries", boundaries);
    root.addProperty("variation", profile.variation);
    root.addProperty("scale", profile.scale);
    root.addProperty("salt", Long.toString(profile.salt));
    root.addProperty("soil_depth", profile.soilDepth);
    root.addProperty("channel", profile.channel);
    root.addProperty("minimum", profile.minimum);
    root.addProperty("maximum", profile.maximum);
    return root;
  }

  private static JsonArray array(JsonObject root, String key, int minimum, int maximum) {
    var value = root.get(key);
    if (value == null || !value.isJsonArray()
        || value.getAsJsonArray().size() < minimum || value.getAsJsonArray().size() > maximum)
      throw new IllegalArgumentException("$." + key + " needs " + minimum + ".." + maximum + " entries");
    return value.getAsJsonArray();
  }

  private static String string(JsonElement value, String path) {
    if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString())
      throw new IllegalArgumentException(path + " must be a string");
    return value.getAsString();
  }

  private static double number(JsonElement value, String path) {
    if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()
        || !Double.isFinite(value.getAsDouble()))
      throw new IllegalArgumentException(path + " must be a finite number");
    return value.getAsDouble();
  }

  private static int integer(JsonElement value, String path) {
    number(value, path);
    try {
      return value.getAsBigDecimal().intValueExact();
    } catch (ArithmeticException failure) {
      throw new IllegalArgumentException(path + " must be an exact integer", failure);
    }
  }
}
