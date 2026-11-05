package com.shnewbs.deepprimal;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.AddPackFindersEvent;

/** Creation-time defaults. Saved dimensions keep their serialized generator and height. */
public final class WorldgenConfig {
  private static final PackSource OPTIONAL_PROFILE = PackSource.create(name -> name, false);
  public static final ModConfigSpec SPEC;
  public static final ModConfigSpec.BooleanValue OWN_ENGINE;
  public static final ModConfigSpec.BooleanValue EXTRA_DEPTH;
  public static final ModConfigSpec.BooleanValue RIVERS_AND_COASTS;
  public static final ModConfigSpec.BooleanValue MOUNTAIN_STREAMS;
  public static final ModConfigSpec.BooleanValue CONTAINED_RIVERS;

  public static final ModConfigSpec.BooleanValue OCEAN_TRENCHES;
  public static final ModConfigSpec.BooleanValue NATURAL_RIVER_STEPS;
  public static final ModConfigSpec.BooleanValue SUPPORTED_RIVER_STEPS;

  static {
    var b = new ModConfigSpec.Builder();
    OWN_ENGINE =
        b.comment(
                "Use Deep Primal continental_v7 for new worlds. Saved worlds retain their"
                    + " generator.")
            .define("builtInTerrainEngine", true);
    EXTRA_DEPTH =
        b.comment(
                "Use Y=-384..639 (1024 blocks) for NEW Deep Primal worlds instead of -128..639 (768"
                    + " blocks).",
                "Restart Minecraft/the server after changing this setting. Existing worlds retain"
                    + " their saved height.",
                "Extra depth increases generation and memory costs; it does not raise the mountain"
                    + " ceiling.")
            .define("extraUndergroundDepth", false);
    RIVERS_AND_COASTS =
        b.comment(
                "Legacy creation-profile switches below apply only when builtInTerrainEngine is"
                    + " false.",
                "Generate river valleys, narrow tributary streams and coastal beaches in NEW"
                    + " worlds.",
                "Existing worlds retain their saved noise settings. Disable to use the legacy alpha"
                    + " 10 terrain profile.")
            .define("riversAndCoasts", true);
    MOUNTAIN_STREAMS =
        b.comment(
                "Route elevated streams through mountain valleys and gorges in NEW worlds.",
                "Requires riversAndCoasts. Disable before creation to use the frozen alpha 11"
                    + " lowland profile.",
                "Existing saves retain their stored terrain profile; restart after changing this"
                    + " setting.")
            .define("mountainStreams", true);
    CONTAINED_RIVERS =
        b.comment(
                "Use contained, terrain-following stepped rivers with stored currents in NEW"
                    + " worlds.",
                "Requires mountainStreams. Disable to select the frozen alpha 12 terrain profile.",
                "Existing saves keep their stored generation profile; this does not repair explored"
                    + " chunks.")
            .define("containedRivers", true);
    NATURAL_RIVER_STEPS =
        b.comment(
                "Partial fluid surfaces, shallower streams and tapered spring/lake headwaters for"
                    + " NEW worlds. Existing saves keep their serialized terrain.")
            .define("naturalRiverSteps", true);
    OCEAN_TRENCHES =
        b.comment(
                "Deeper ocean relief for NEW worlds. Requires contained rivers; saved generators"
                    + " retain their IDs.")
            .define("deepOceanTrenches", true);
    SUPPORTED_RIVER_STEPS =
        b.comment(
                "Use bank-supported, stable channel surfaces in NEW worlds. Existing saves keep"
                    + " their stored profile.")
            .define("supportedRiverSteps", true);
    SPEC = b.build();
  }

  public static void packs(AddPackFindersEvent event) {
    if (event.getPackType() != PackType.SERVER_DATA) return;
    // Preserve saved pack IDs as well as saved noise-setting IDs. Only the
    // selected profile is required for new worlds; saved dimensions stay locked.
    String profile = EXTRA_DEPTH.get() ? "deep" : "standard";
    if (RIVERS_AND_COASTS.get())
      profile =
          (MOUNTAIN_STREAMS.get()
                  ? CONTAINED_RIVERS.get()
                      ? NATURAL_RIVER_STEPS.get()
                          ? SUPPORTED_RIVER_STEPS.get()
                              ? OCEAN_TRENCHES.get() ? "supported_" : "supported_rivers_"
                              : OCEAN_TRENCHES.get() ? "natural_" : "natural_rivers_"
                          : OCEAN_TRENCHES.get() ? "ocean_" : "contained_"
                      : "routed_"
                  : "waterways_")
              + profile;
    if (OWN_ENGINE.get()) profile = "engine_" + (EXTRA_DEPTH.get() ? "deep" : "standard");
    for (String available :
        new String[] {
          "engine_standard",
          "engine_deep",
          "engine_deltas_standard",
          "engine_deltas_deep",
          "standard",
          "deep",
          "waterways_standard",
          "waterways_deep",
          "routed_standard",
          "routed_deep",
          "contained_standard",
          "contained_deep",
          "ocean_standard",
          "ocean_deep",
          "natural_standard",
          "natural_deep",
          "natural_rivers_standard",
          "natural_rivers_deep",
          "supported_standard",
          "supported_deep",
          "supported_rivers_standard",
          "supported_rivers_deep"
        }) {
      event.addPackFinders(
          Identifier.fromNamespaceAndPath(DeepPrimal.ID, "worldgen/" + available),
          PackType.SERVER_DATA,
          Component.literal(displayName(available)),
          available.equals(profile) ? PackSource.BUILT_IN : OPTIONAL_PROFILE,
          available.equals(profile),
          Pack.Position.TOP);
    }
  }

  private static String displayName(String profile) {
    boolean deep = profile.equals("deep") || profile.endsWith("_deep");
    String family;
    if (profile.startsWith("engine_deltas_")) family = "Legacy Deltas";
    else if (profile.startsWith("engine_")) family = "Engine";
    else if (profile.startsWith("supported_rivers_")) family = "Supported Rivers";
    else if (profile.startsWith("supported_")) family = "Supported";
    else if (profile.startsWith("natural_rivers_")) family = "Natural Rivers";
    else if (profile.startsWith("natural_")) family = "Natural";
    else if (profile.startsWith("ocean_")) family = "Ocean";
    else if (profile.startsWith("contained_")) family = "Contained";
    else if (profile.startsWith("routed_")) family = "Routed";
    else if (profile.startsWith("waterways_")) family = "Waterways";
    else family = "Legacy";
    return "DP " + family + ": " + (deep ? "Deep" : "Standard");
  }

  private WorldgenConfig() {}
}
