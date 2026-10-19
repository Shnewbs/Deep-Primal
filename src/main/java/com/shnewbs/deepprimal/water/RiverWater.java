package com.shnewbs.deepprimal.water;

import com.shnewbs.deepprimal.DeepPrimal;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class RiverWater {
  public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(DeepPrimal.ID);
  public static final DeferredBlock<RiverWaterBlock> BLOCK =
      BLOCKS.registerBlock(
          "river_water",
          RiverWaterBlock::new,
          () ->
              net.minecraft.world.level.block.state.BlockBehaviour.Properties.ofFullCopy(
                  Blocks.WATER));

  private RiverWater() {}
}
