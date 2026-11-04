package com.shnewbs.deepprimal.client;

import com.shnewbs.deepprimal.engine.GroundMaterials;
import com.shnewbs.deepprimal.core.SoilTexture;
import java.util.List;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

/** Separate surface and soil indices keep climate tint off the mineral substrate. */
public final class GroundMaterialColors {
  public static final int SOIL = 0xb6a080;

  public static void register(RegisterColorHandlersEvent.BlockTintSources event) {
    var soil = BlockTintSources.constant(SOIL);
    event.register(List.of(surface(BlockTintSources.grass()), soil, side(BlockTintSources.grass())), GroundMaterials.GRASS.get());
    event.register(List.of(soil, soil, soil), GroundMaterials.DIRT.get());
    event.register(List.of(surface(BlockTintSources.dryFoliage()), soil, side(BlockTintSources.dryFoliage())), GroundMaterials.PODZOL.get());
    for (var block : GroundMaterials.soilsList()) {
      var mineral =
          block.texture() == SoilTexture.SAND
              ? SandColorMap.INSTANCE
              : BlockTintSources.constant(block.texture().color());
      var top = switch (block.cover()) {
        case BARE -> mineral;
        case GRASS -> BlockTintSources.grass();
        case PODZOL -> BlockTintSources.dryFoliage();
      };
      event.register(List.of(soilParticle(top, block.texture().color()), mineral,
          block.cover() == GroundMaterials.Cover.BARE ? mineral : side(top, block.texture().color())), block);
    }
  }

  private static BlockTintSource side(BlockTintSource delegate) {
    return side(delegate, SOIL);
  }

  private static BlockTintSource side(BlockTintSource delegate, int soil) {
    return com.shnewbs.deepprimal.settings.RendererOwnership.owned()
        ? soilParticle(delegate, soil) : BlockTintSources.constant(0xffffffff);
  }

  private static BlockTintSource surface(BlockTintSource delegate) {
    return soilParticle(delegate, SOIL);
  }

  private static BlockTintSource soilParticle(BlockTintSource delegate, int soil) {
    return new BlockTintSource() {
      public int color(BlockState state) {
        return delegate.color(state);
      }

      public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        return delegate.colorInWorld(state, level, pos);
      }

      public int colorAsTerrainParticle(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        return soil;
      }
    };
  }

  private GroundMaterialColors() {}
}
