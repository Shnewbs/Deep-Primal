package com.shnewbs.deepprimal.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.shnewbs.deepprimal.client.SnowFoliage;
import java.util.List;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockColors.class)
abstract class SnowFoliageMixin {
  @WrapMethod(method="register")
  private void deepprimal$snow(List<BlockTintSource> layers,Block[] blocks,Operation<Void> original) {
    boolean leaves=false;
    for(Block block:blocks)if(block instanceof LeavesBlock) {leaves=true;break;}
    original.call(leaves?layers.stream().map(s->s instanceof SnowFoliage?s:(BlockTintSource)new SnowFoliage(s)).toList():layers,blocks);
  }
}
