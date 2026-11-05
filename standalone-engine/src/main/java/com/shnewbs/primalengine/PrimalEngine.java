package com.shnewbs.primalengine;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.*;

@Mod(PrimalEngine.ID)
public final class PrimalEngine {
  public static final String ID = "primalengine";
  public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> GENERATORS =
      DeferredRegister.create(Registries.CHUNK_GENERATOR, ID);
  public static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOMES =
      DeferredRegister.create(Registries.BIOME_SOURCE, ID);
  public static final DeferredRegister<Fluid> FLUIDS =
      DeferredRegister.create(Registries.FLUID, ID);
  public static final DeferredHolder<Fluid, EngineFluid> WATER =
      FLUIDS.register("water", EngineFluid::new);
  public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
  public static final DeferredBlock<EngineWaterBlock> WATER_BLOCK =
      BLOCKS.registerBlock(
          "water",
          EngineWaterBlock::new,
          () ->
              net.minecraft.world.level.block.state.BlockBehaviour.Properties.ofFullCopy(
                  net.minecraft.world.level.block.Blocks.WATER));
  public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
  public static final DeferredItem<EngineBucket> BUCKET =
      ITEMS.registerItem("water_bucket", EngineBucket::new, p -> p.stacksTo(1));

  public PrimalEngine(IEventBus bus) {
    GENERATORS.register("continental", () -> ContinentalGenerator.CODEC);
    BIOMES.register("regional", () -> RegionalBiomes.CODEC);
    GENERATORS.register(bus);
    BIOMES.register(bus);
    FLUIDS.register(bus);
    BLOCKS.register(bus);
    ITEMS.register(bus);
  }
}
