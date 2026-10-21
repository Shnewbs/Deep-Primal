package com.shnewbs.deepprimal.engine;

import com.mojang.serialization.MapCodec;
import com.shnewbs.deepprimal.DeepPrimal;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/** Built-in components registered in Deep Primal's namespace and lifecycle. */
public final class EngineRegistry {
  public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> GENERATORS =
      DeferredRegister.create(Registries.CHUNK_GENERATOR, DeepPrimal.ID);
  public static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOMES =
      DeferredRegister.create(Registries.BIOME_SOURCE, DeepPrimal.ID);
  public static final DeferredRegister<Fluid> FLUIDS =
      DeferredRegister.create(Registries.FLUID, DeepPrimal.ID);
  public static final DeferredHolder<Fluid, EngineFluid> WATER =
      FLUIDS.register("engine_water", EngineFluid::new);
  public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(DeepPrimal.ID);
  public static final DeferredBlock<EngineWaterBlock> WATER_BLOCK =
      BLOCKS.registerBlock(
          "engine_water",
          EngineWaterBlock::new,
          () ->
              net.minecraft.world.level.block.state.BlockBehaviour.Properties.ofFullCopy(
                  net.minecraft.world.level.block.Blocks.WATER));
  public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DeepPrimal.ID);
  public static final DeferredItem<EngineBucket> BUCKET =
      ITEMS.registerItem("engine_water_bucket", EngineBucket::new, p -> p.stacksTo(1));

  public static void register(IEventBus bus) {
    GENERATORS.register("continental_v1", () -> EngineChunkGenerator.CODEC);
    BIOMES.register("regional_v1", () -> RegionBiomeSource.CODEC);
    GENERATORS.register(bus);
    BIOMES.register(bus);
    FLUIDS.register(bus);
    BLOCKS.register(bus);
    ITEMS.register(bus);
  }

  private EngineRegistry() {}
}
