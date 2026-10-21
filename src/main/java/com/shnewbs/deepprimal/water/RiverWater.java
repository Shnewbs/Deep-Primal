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

  public static final DeferredBlock<ChannelWaterBlock> CHANNEL =
      BLOCKS.registerBlock(
          "channel_water",
          ChannelWaterBlock::new,
          () ->
              net.minecraft.world.level.block.state.BlockBehaviour.Properties.ofFullCopy(
                  Blocks.WATER));

  public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DeepPrimal.ID);
  public static final net.neoforged.neoforge.registries.DeferredItem<MeasuredBucket>
      MEASURED_BUCKET = ITEMS.registerItem("measured_bucket", MeasuredBucket::new);

  private RiverWater() {}
}
