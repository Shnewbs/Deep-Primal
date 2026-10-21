package com.shnewbs.deepprimal.engine;

import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.HitResult;

/** Full measured sixteen-unit bucket. Empty vanilla buckets collect only full engine cells. */
public final class EngineBucket extends Item {
  public EngineBucket(Properties p) {
    super(p);
  }

  public InteractionResult use(Level level, Player player, InteractionHand hand) {
    var hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
    if (hit.getType() != HitResult.Type.BLOCK) return InteractionResult.PASS;
    var p = hit.getBlockPos().relative(hit.getDirection());
    if (!level.mayInteract(player, p)
        || !player.mayUseItemAt(p, hit.getDirection(), player.getItemInHand(hand))
        || !level.getBlockState(p).isAir()) return InteractionResult.FAIL;
    if (!level.isClientSide())
      level.setBlock(p, EngineRegistry.WATER_BLOCK.get().defaultBlockState(), 3);
    return InteractionResult.SUCCESS.heldItemTransformedTo(
        player.hasInfiniteMaterials()
            ? player.getItemInHand(hand)
            : Items.BUCKET.getDefaultInstance());
  }
}
