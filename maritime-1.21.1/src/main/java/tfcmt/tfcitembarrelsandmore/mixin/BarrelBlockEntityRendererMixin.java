package tfcmt.tfcitembarrelsandmore.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dries007.tfc.client.render.blockentity.BarrelBlockEntityRenderer;
import net.dries007.tfc.common.blockentities.BarrelBlockEntity;
import net.dries007.tfc.common.blocks.devices.BarrelBlock;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfcmt.tfcitembarrelsandmore.client.BraceClientCache;
import tfcmt.tfcitembarrelsandmore.client.renderer.BraceRenderer;
import tfcmt.tfcitembarrelsandmore.common.BraceData;

@Mixin(value={BarrelBlockEntityRenderer.class})
public class BarrelBlockEntityRendererMixin {
    private BraceData tfcmt$activeBrace;

    @Inject(method={"render(Lnet/dries007/tfc/common/blockentities/BarrelBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"}, at={@At(value="HEAD")}, remap=false)
    private void tfcmt$injectLiftAtHead(BarrelBlockEntity be, float partialTick, PoseStack ps, MultiBufferSource buffers, int packedLight, int packedOverlay, CallbackInfo ci) {
        this.tfcmt$activeBrace = BarrelBlockEntityRendererMixin.resolveBracedSideBarrel(be);
        if (this.tfcmt$activeBrace == null) {
            return;
        }
        ps.pushPose();
        ps.translate(0.0, 0.125, 0.0);
    }

    @Inject(method={"render(Lnet/dries007/tfc/common/blockentities/BarrelBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"}, at={@At(value="RETURN")}, remap=false)
    private void tfcmt$injectBraceAtReturn(BarrelBlockEntity be, float partialTick, PoseStack ps, MultiBufferSource buffers, int packedLight, int packedOverlay, CallbackInfo ci) {
        if (this.tfcmt$activeBrace == null) {
            return;
        }
        ps.popPose();
        Direction facing = (Direction)be.getBlockState().getValue((Property)BarrelBlock.FACING);
        BraceRenderer.render(ps, buffers, this.tfcmt$activeBrace, facing, packedLight, packedOverlay, 0.0f);
    }

    private static BraceData resolveBracedSideBarrel(BarrelBlockEntity be) {
        BraceData brace = BraceClientCache.get(be.getBlockPos());
        if (brace == null || !brace.isValid()) {
            return null;
        }
        BlockState state = be.getBlockState();
        if (!state.hasProperty((Property)BarrelBlock.FACING)) {
            return null;
        }
        if (!((Direction)state.getValue((Property)BarrelBlock.FACING)).getAxis().isHorizontal()) {
            return null;
        }
        return brace;
    }
}

