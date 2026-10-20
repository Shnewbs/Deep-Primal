package com.shnewbs.deepprimal;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.AddPackFindersEvent;

/** Creation-time defaults. Saved dimensions keep their own type and noise settings. */
public final class WorldgenConfig {
  private static final PackSource OPTIONAL_PROFILE = PackSource.create(name -> name, false);
  public static final ModConfigSpec SPEC;
  public static final ModConfigSpec.BooleanValue EXTRA_DEPTH;
  public static final ModConfigSpec.BooleanValue RIVERS_AND_COASTS;
  public static final ModConfigSpec.BooleanValue MOUNTAIN_STREAMS;
  public static final ModConfigSpec.BooleanValue CONTAINED_RIVERS;

  public static final ModConfigSpec.BooleanValue OCEAN_TRENCHES;

  static {
    var b = new ModConfigSpec.Builder();
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
    OCEAN_TRENCHES =
        b.comment(
                "Deeper ocean relief for NEW worlds. Requires contained rivers; saved generators"
                    + " retain their IDs.")
            .define("deepOceanTrenches", true);
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
                      ? OCEAN_TRENCHES.get() ? "ocean_" : "contained_"
                      : "routed_"
                  : "waterways_")
              + profile;
    for (String available :
        new String[] {
          "standard",
          "deep",
          "waterways_standard",
          "waterways_deep",
          "routed_standard",
          "routed_deep",
          "contained_standard",
          "contained_deep",
          "ocean_standard",
          "ocean_deep"
        }) {
      event.addPackFinders(
          Identifier.fromNamespaceAndPath(DeepPrimal.ID, "worldgen/" + available),
          PackType.SERVER_DATA,
          Component.literal("Deep Primal world generation: " + available),
          available.equals(profile) ? PackSource.BUILT_IN : OPTIONAL_PROFILE,
          available.equals(profile),
          Pack.Position.TOP);
    }
  }

  private WorldgenConfig() {}
}
