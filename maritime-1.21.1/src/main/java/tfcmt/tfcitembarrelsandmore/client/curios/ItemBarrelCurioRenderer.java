package tfcmt.tfcitembarrelsandmore.client.curios;



import net.dries007.tfc.common.blocks.devices.BarrelBlock;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import org.jetbrains.annotations.Nullable;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlocks;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.ItemBarrelBlock;

public class ItemBarrelCurioRenderer
implements top.theillusivec4.curios.api.client.ICurioRenderer {
    @Override
    public <T extends LivingEntity, M extends net.minecraft.client.model.EntityModel<T>> void render(
        ItemStack stack, top.theillusivec4.curios.api.SlotContext slot, com.mojang.blaze3d.vertex.PoseStack pose,
        net.minecraft.client.renderer.entity.RenderLayerParent<T, M> parent, net.minecraft.client.renderer.MultiBufferSource buffers,
        int light, float limbSwing, float limbAmount, float partialTick, float age, float headYaw, float headPitch) {
        BlockState state = resolveBarrelState(stack);
        if (state == null || !slot.visible()) return;
        pose.pushPose();
        if (parent.getModel() instanceof net.minecraft.client.model.HumanoidModel<?> humanoid) humanoid.body.translateAndRotate(pose);
        pose.translate(0.0, 0.5, 0.18);
        pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(180));
        pose.scale(0.6f, 0.6f, 0.6f);
        pose.translate(-0.5, -0.5, -1.0);
        net.minecraft.client.Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
            state, pose, buffers, light, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }

    @Nullable
    public BlockState getBlock(LivingEntity entity, ItemStack stack) {
        return ItemBarrelCurioRenderer.resolveBarrelState(stack);
    }

    @Nullable
    private static BlockState resolveBarrelState(ItemStack stack) {
        Item item = stack.getItem();
        if (!(item instanceof BlockItem)) {
            return null;
        }
        BlockItem bi = (BlockItem)item;
        if (!(bi.getBlock() instanceof ItemBarrelBlock)) {
            return null;
        }
        TIBAMBlocks.Wood woodType = null;
        for (TIBAMBlocks.Wood w : TIBAMBlocks.Wood.values()) {
            if (TIBAMBlocks.ITEM_BARRELS.get((Object)w).get() != bi.getBlock()) continue;
            woodType = w;
            break;
        }
        if (woodType == null) {
            return null;
        }
        Block tfcBarrel = (Block)BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(woodType.getMod(), "wood/barrel/" + woodType.getId()));
        if (!(tfcBarrel instanceof BarrelBlock)) {
            return null;
        }
        BarrelBlock barrelBlock = (BarrelBlock)tfcBarrel;
        return (BlockState)barrelBlock.defaultBlockState().setValue((Property)BarrelBlock.SEALED, Boolean.valueOf(ItemBarrelCurioRenderer.isSealed(stack)));
    }

    private static boolean isSealed(ItemStack stack) {
        if (!tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.has(stack)) {
            return false;
        }
        CompoundTag bet = tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.get(stack).getCompound("BlockEntityTag");
        return bet.contains("Sealed") && bet.getBoolean("Sealed");
    }
}

