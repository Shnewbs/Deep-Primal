package com.shnewbs.deepprimal.mixin.client;

import com.shnewbs.deepprimal.performance.DistanceRuntime;
import com.shnewbs.deepprimal.core.DistancePolicy;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Options.class)
public abstract class OptionsMixin {
    @ModifyArgs(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;<init>(Ljava/lang/String;Lnet/minecraft/client/OptionInstance$TooltipSupplier;Lnet/minecraft/client/OptionInstance$CaptionBasedToString;Lnet/minecraft/client/OptionInstance$ValueSet;Ljava/lang/Object;Ljava/util/function/Consumer;)V"))
    private void deepprimal$distanceRanges(Args args) {
        String caption = args.get(0);
        if ("options.renderDistance".equals(caption)) args.set(3, new OptionInstance.IntRange(2, DistancePolicy.MAX_RENDER, false));
        if ("options.simulationDistance".equals(caption)) args.set(3, new OptionInstance.IntRange(2, DistancePolicy.MAX_SIMULATION, false));
    }
    @Inject(method = "getEffectiveRenderDistance", at = @At("RETURN"), cancellable = true)
    private void deepprimal$sessionRenderLimit(CallbackInfoReturnable<Integer> callback) {
        if (Minecraft.getInstance().getSingleplayerServer() != null)
            callback.setReturnValue(Math.min(callback.getReturnValue(), DistanceRuntime.trackingCapacity()));
    }
    @Inject(method = "<init>", at = @At("RETURN"))
    private void deepprimal$rememberDistance(CallbackInfo callback) {
        DistanceRuntime.clientRequest(((Options)(Object)this).renderDistance().get());
    }
}
