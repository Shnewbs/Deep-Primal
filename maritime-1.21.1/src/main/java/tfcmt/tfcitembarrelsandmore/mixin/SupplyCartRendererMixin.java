package tfcmt.tfcitembarrelsandmore.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


import tfcmt.tfcitembarrelsandmore.compat.astikorcarts.BarrelCartHandler;

@Pseudo
@Mixin(targets="tfcastikorcarts.client.renderer.entity.TFCSupplyCartRenderer", remap=false)
public class SupplyCartRendererMixin {
    private static final float[] BX = new float[]{-0.22f, 0.22f, -0.22f, 0.22f};
    private static final float[] BZ = new float[]{0.14f, 0.14f, -0.14f, -0.14f};
    private static final float BY = 0.28f;
    private static final float SCALE = 0.36f;

    @Inject(method={"renderContents(Ltfcastikorcarts/common/entities/carts/TFCSupplyCartEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void tibam$renderBarrels(@org.spongepowered.asm.mixin.injection.Coerce Object entity, float partialTick, PoseStack pose, MultiBufferSource buffers, int packedLight, CallbackInfo ci) {
        if (!BarrelCartHandler.isBarrelMode(entity)) {
            return;
        }
        ci.cancel();
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        for (int i = 0; i < 4; ++i) {
            Item item;
            var inventory = BarrelCartHandler.inventory(entity);
            if (inventory == null) return;
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty() || !((item = stack.getItem()) instanceof BlockItem)) continue;
            BlockItem bi = (BlockItem)item;
            BlockState state = bi.getBlock().defaultBlockState();
            pose.pushPose();
            pose.translate(BX[i], 0.28f, BZ[i]);
            pose.scale(0.36f, 0.36f, 0.36f);
            pose.mulPose(Axis.ZP.rotationDegrees(90.0f));
            pose.translate(-0.5, -0.5, -0.5);
            dispatcher.getModelRenderer().renderModel(pose.last(), buffers.getBuffer(RenderType.solid()), state, dispatcher.getBlockModel(state), 1.0f, 1.0f, 1.0f, packedLight, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.solid());
            pose.popPose();
        }
    }
}

