package tfcmt.tfcitembarrelsandmore.mixin.client;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfcmt.tfcitembarrelsandmore.common.blockentities.ExplorationLampBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.items.ExplorationLampBlockItem;

@Mixin(value={Entity.class})
public abstract class EntityDynamicLightMixin {
    private static final double BEAM_DISTANCE = 3.0;

    @Inject(method={"sdl$getDynamicLightX"}, at={@At(value="HEAD")}, cancellable=true, remap=false)
    private void tfcmt$offsetLightX(CallbackInfoReturnable<Double> cir) {
        Vec3 offset = this.tfcmt$searchBeamOffset();
        if (offset != null) {
            cir.setReturnValue((Object)offset.x);
        }
    }

    @Inject(method={"sdl$getDynamicLightY"}, at={@At(value="HEAD")}, cancellable=true, remap=false)
    private void tfcmt$offsetLightY(CallbackInfoReturnable<Double> cir) {
        Vec3 offset = this.tfcmt$searchBeamOffset();
        if (offset != null) {
            cir.setReturnValue((Object)offset.y);
        }
    }

    @Inject(method={"sdl$getDynamicLightZ"}, at={@At(value="HEAD")}, cancellable=true, remap=false)
    private void tfcmt$offsetLightZ(CallbackInfoReturnable<Double> cir) {
        Vec3 offset = this.tfcmt$searchBeamOffset();
        if (offset != null) {
            cir.setReturnValue((Object)offset.z);
        }
    }

    private Vec3 tfcmt$searchBeamOffset() {
        EntityDynamicLightMixin entityDynamicLightMixin = this;
        if (!(entityDynamicLightMixin instanceof Player)) {
            return null;
        }
        Player player = (Player)entityDynamicLightMixin;
        ItemStack offhand = player.getItemInHand(InteractionHand.OFF_HAND);
        if (!(offhand.getItem() instanceof ExplorationLampBlockItem)) {
            return null;
        }
        if (!ExplorationLampBlockItem.isLit(offhand)) {
            return null;
        }
        if (ExplorationLampBlockItem.getModeFromStack(offhand) != ExplorationLampBlockEntity.Mode.SEARCH) {
            return null;
        }
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        return eye.add(look.scale(3.0));
    }
}

