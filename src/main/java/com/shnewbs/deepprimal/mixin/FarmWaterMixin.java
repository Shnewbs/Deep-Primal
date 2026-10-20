package com.shnewbs.deepprimal.mixin;

import com.shnewbs.deepprimal.water.RiverConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Player-only irrigation bridge; the block remains water and continues hydrating crops. */
@Mixin(LiquidBlock.class)
abstract class FarmWaterMixin {
  @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
  private void deepprimal$farm(
      BlockState state,
      BlockGetter level,
      BlockPos pos,
      CollisionContext context,
      CallbackInfoReturnable<VoxelShape> ci) {
    // Block shape-cache construction occurs before configuration is loaded.
    if (!(context instanceof EntityCollisionContext c)
        || !(c.getEntity() instanceof Player p)
        || !RiverConfig.FARM_WALKING.get()
        || !state.getFluidState().is(net.minecraft.tags.FluidTags.WATER)
        || !state.getFluidState().isSource()
        || p.getY() < pos.getY() + .85
        || p.isShiftKeyDown()) return;
    for (Direction d : Direction.Plane.HORIZONTAL)
      if (level.getBlockState(pos.relative(d)).is(Blocks.FARMLAND)) {
        ci.setReturnValue(Shapes.box(0, .875, 0, 1, .9375, 1));
        return;
      }
  }
}
