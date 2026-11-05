package tfcmt.tfcitembarrelsandmore.mixin;

import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.blocks.devices.SealableDeviceBlock;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfcmt.tfcitembarrelsandmore.common.BraceData;
import tfcmt.tfcitembarrelsandmore.common.TIBAMCommonEvents;
import tfcmt.tfcitembarrelsandmore.common.network.TIBAMNetwork;

@Mixin(value={SealableDeviceBlock.class})
public class SealableDeviceBlockMixin {
    @Inject(method={"beforeRemove(Lnet/dries007/tfc/common/blockentities/InventoryBlockEntity;)V"}, at={@At(value="HEAD")}, remap=false)
    private void tfcmt$onBeforeRemove(InventoryBlockEntity blockEntity, CallbackInfo ci) {
        Level level = blockEntity.getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }
        BlockPos pos = blockEntity.getBlockPos();
        BraceData brace = TIBAMCommonEvents.getBraceData(level, pos);
        if (brace == null || !brace.isValid()) {
            return;
        }
        ResourceLocation itemId = brace.type.equals("wood") ? ResourceLocation.fromNamespaceAndPath(brace.mod, "wood/lumber/" + brace.variant) : ResourceLocation.fromNamespaceAndPath("tfc", "metal/rod/" + brace.variant);
        Item item = (Item)BuiltInRegistries.ITEM.get(itemId);
        if (item != null && item != Items.AIR) {
            Helpers.spawnItem((Level)level, (BlockPos)pos, (ItemStack)new ItemStack((ItemLike)item));
        }
        TIBAMCommonEvents.removeBraceData(level, pos);
        TIBAMNetwork.removeBrace(pos, (ServerLevel)level);
    }
}

