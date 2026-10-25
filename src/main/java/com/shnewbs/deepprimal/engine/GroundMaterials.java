package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.DeepPrimal;
import com.shnewbs.deepprimal.core.SoilTexture;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Versioned soil families plus retained sample registry identities. */
public final class GroundMaterials {
  public enum Cover { BARE, GRASS, PODZOL }
  private static final DeferredRegister.Blocks BLOCKS =
      DeferredRegister.createBlocks(DeepPrimal.ID);
  private static final DeferredRegister.Items ITEMS =
      DeferredRegister.createItems(DeepPrimal.ID);

  public static final DeferredBlock<Block> GRASS = material("ground_grass", SoundType.GRASS);
  public static final DeferredBlock<Block> DIRT = material("ground_dirt", SoundType.GRAVEL);
  public static final DeferredBlock<Block> PODZOL = material("ground_podzol", SoundType.GRAVEL);
  private static final Map<SoilTexture, Map<Cover, DeferredBlock<SoilBlock>>> SOILS = soils();

  public static final class SoilBlock extends Block {
    private final SoilTexture texture;
    private final Cover cover;

    private SoilBlock(Properties properties, SoilTexture texture, Cover cover) {
      super(properties);
      this.texture = texture;
      this.cover = cover;
    }

    public SoilTexture texture() { return texture; }
    public Cover cover() { return cover; }
  }

  private static Map<SoilTexture, Map<Cover, DeferredBlock<SoilBlock>>> soils() {
    var result = new EnumMap<SoilTexture, Map<Cover, DeferredBlock<SoilBlock>>>(SoilTexture.class);
    for (var texture : SoilTexture.values()) {
      var covers = new EnumMap<Cover, DeferredBlock<SoilBlock>>(Cover.class);
      for (var cover : Cover.values()) {
        String name = name(texture, cover);
        var block = BLOCKS.registerBlock(name, p -> new SoilBlock(p, texture, cover),
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(.5F)
                .sound(cover == Cover.GRASS ? SoundType.GRASS
                    : texture == SoilTexture.SAND ? SoundType.SAND : SoundType.GRAVEL));
        ITEMS.registerSimpleBlockItem(block);
        covers.put(cover, block);
      }
      result.put(texture, Map.copyOf(covers));
    }
    return Map.copyOf(result);
  }

  public static String name(SoilTexture texture, Cover cover) {
    return "soil_" + texture.id() + (cover == Cover.BARE ? "" : cover == Cover.GRASS ? "_grass" : "_podzol");
  }

  public static SoilBlock soil(SoilTexture texture, Cover cover) {
    return SOILS.get(texture).get(cover).get();
  }

  public static List<SoilBlock> soilsList() {
    return java.util.Arrays.stream(SoilTexture.values())
        .flatMap(texture -> java.util.Arrays.stream(Cover.values()).map(cover -> soil(texture, cover))).toList();
  }

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
      soilsList().forEach(event::accept);
    }
  }

  private GroundMaterials() {}
}
