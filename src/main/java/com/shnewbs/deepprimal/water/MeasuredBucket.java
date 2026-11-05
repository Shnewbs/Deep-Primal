package com.shnewbs.deepprimal.water;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/** Partial bucket for the optional finite solver. Water units are native persisted item data. */
public final class MeasuredBucket extends Item {
  public MeasuredBucket(Properties properties) {
    super(properties.stacksTo(1));
  }

  public static int units(ItemStack stack) {
    return Math.clamp(
        stack
            .getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
            .copyTag()
            .getIntOr("water_units", 0),
        0,
        8);
  }

  private static void units(ItemStack stack, int amount) {
    CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt("water_units", amount));
  }

  @Override
  public boolean isBarVisible(ItemStack stack) {
    return units(stack) > 0;
  }

  @Override
  public int getBarWidth(ItemStack stack) {
    return Math.round(units(stack) * 13f / 8);
  }

  @Override
  public int getBarColor(ItemStack stack) {
    return 0x55b7e0;
  }

  @Override
  public InteractionResult use(Level level, Player player, InteractionHand hand) {
    if (!FiniteWater.enabled()) return InteractionResult.PASS;
    var hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
    if (hit.getType() != HitResult.Type.BLOCK) return InteractionResult.PASS;
    var stack = player.getItemInHand(hand);
    int stored = units(stack);
    var clicked = hit.getBlockPos();
    var target =
        FiniteWater.units(level.getBlockState(clicked)) > 0
            ? clicked
            : clicked.relative(hit.getDirection());
    if (!level.mayInteract(player, target)
        || !player.mayUseItemAt(target, hit.getDirection(), stack)) return InteractionResult.FAIL;
    if (level instanceof net.minecraft.server.level.ServerLevel server) {
      boolean collect =
          !player.isShiftKeyDown()
              && stored < 8
              && FiniteWater.units(level.getBlockState(clicked)) > 0;
      int changed =
          collect
              ? FiniteWater.exchange(server, clicked, -(8 - stored), false)
              : FiniteWater.exchange(
                  server, target, player.isShiftKeyDown() ? Math.min(1, stored) : stored, false);
      if (changed == 0) return InteractionResult.PASS;
      units(stack, stored - changed);
      ((net.minecraft.server.level.ServerPlayer) player)
          .sendSystemMessage(
              Component.translatable("item.deepprimal.measured_bucket.amount", units(stack), 8),
              true);
    }
    return InteractionResult.SUCCESS;
  }
}
