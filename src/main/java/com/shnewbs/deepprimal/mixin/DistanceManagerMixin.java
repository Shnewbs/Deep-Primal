package com.shnewbs.deepprimal.mixin;

import com.shnewbs.deepprimal.DistanceRuntime;
import net.minecraft.server.level.DistanceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(DistanceManager.class)
public abstract class DistanceManagerMixin {
    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/DistanceManager$PlayerTicketTracker;<init>(Lnet/minecraft/server/level/DistanceManager;I)V"), index = 1)
    private int deepprimal$trackingCapacity(int nativeCapacity) { return DistanceRuntime.trackingCapacity(); }
}
