package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.DeepPrimal;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Opt-in material samples; frozen terrain generators never place these blocks. */
public final class GroundMaterials {
  private static final DeferredRegister.Blocks BLOCKS =
      DeferredRegister.createBlocks(DeepPrimal.ID);
  private static final DeferredRegister.Items ITEMS =
      DeferredRegister.createItems(DeepPrimal.ID);

  public static final DeferredBlock<Block> GRASS = material("ground_grass", SoundType.GRASS);
  public static final DeferredBlock<Block> DIRT = material("ground_dirt", SoundType.GRAVEL);
  public static final DeferredBlock<Block> PODZOL = material("ground_podzol", SoundType.GRAVEL);

  private static DeferredBlock<Block> material(String name, SoundType sound) {
    var block = BLOCKS.registerBlock(
        name, Block::new,
        () -> BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(.5F).sound(sound));
    ITEMS.registerSimpleBlockItem(block);
    return block;
  }

  public static void register(IEventBus bus) {
    BLOCKS.register(bus);
    ITEMS.register(bus);
    bus.addListener(GroundMaterials::creative);
  }

  private static void creative(BuildCreativeModeTabContentsEvent event) {
    if (event.getTabKey().equals(CreativeModeTabs.NATURAL_BLOCKS)) {
      event.accept(GRASS.get());
      event.accept(DIRT.get());
      event.accept(PODZOL.get());
    }
  }

  private GroundMaterials() {}
}
