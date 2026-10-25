package com.shnewbs.deepprimal.client;

import com.shnewbs.deepprimal.engine.GroundMaterials;
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
  }

  private static BlockTintSource side(BlockTintSource delegate) {
    return com.shnewbs.deepprimal.settings.RendererOwnership.owned()
        ? surface(delegate) : BlockTintSources.constant(0xffffffff);
  }

  private static BlockTintSource surface(BlockTintSource delegate) {
    return new BlockTintSource() {
      public int color(BlockState state) {
        return delegate.color(state);
      }

      public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        return delegate.colorInWorld(state, level, pos);
      }

      public int colorAsTerrainParticle(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        return SOIL;
      }
    };
  }

  private GroundMaterialColors() {}
}
