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

  public static ItemStack filled(com.shnewbs.deepprimal.core.WaterAccount.Parcel parcel) {
    var stack = EngineRegistry.BUCKET.get().getDefaultInstance();
    var tag = new net.minecraft.nbt.CompoundTag();
    tag.putLong("deepprimal_salt", parcel.salt());
    tag.putLong("deepprimal_sediment", parcel.sediment());
    stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
        net.minecraft.world.item.component.CustomData.of(tag));
    return stack;
  }
  public static com.shnewbs.deepprimal.core.WaterAccount.Parcel contents(ItemStack stack) {
    var tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
        net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
    return new com.shnewbs.deepprimal.core.WaterAccount.Parcel(4096,
        Math.clamp(tag.getLongOr("deepprimal_salt", 0), 0, com.shnewbs.deepprimal.core.WaterAccount.MAX * 1024),
        Math.clamp(tag.getLongOr("deepprimal_sediment", 0), 0, com.shnewbs.deepprimal.core.WaterAccount.MAX * 1024));
  }

  public InteractionResult use(Level level, Player player, InteractionHand hand) {
    var hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
    if (hit.getType() != HitResult.Type.BLOCK) return InteractionResult.PASS;
    var p = hit.getBlockPos().relative(hit.getDirection());
    if (!level.mayInteract(player, p)
        || !player.mayUseItemAt(p, hit.getDirection(), player.getItemInHand(hand))
        || !level.getBlockState(p).isAir()) return InteractionResult.FAIL;
    if (level instanceof net.minecraft.server.level.ServerLevel server
        && !HydrologyRuntime.place(server, p, contents(player.getItemInHand(hand))))
      return InteractionResult.FAIL;
    return InteractionResult.SUCCESS.heldItemTransformedTo(
        player.hasInfiniteMaterials()
            ? player.getItemInHand(hand)
            : Items.BUCKET.getDefaultInstance());
  }
}
