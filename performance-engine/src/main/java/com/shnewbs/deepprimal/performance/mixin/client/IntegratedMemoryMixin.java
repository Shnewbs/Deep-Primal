package com.shnewbs.deepprimal.performance.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.shnewbs.deepprimal.performance.MemorySafety;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(IntegratedServer.class)
public abstract class IntegratedMemoryMixin {
    @ModifyExpressionValue(method = "tickServer", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I", ordinal = 0))
    private int deepprimal$requestedView(int distance) {
        return MemorySafety.serverLimit(distance, (MinecraftServer)(Object)this);
    }
    @ModifyExpressionValue(method = "tickServer", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I", ordinal = 1))
    private int deepprimal$requestedSimulation(int distance) {
        return MemorySafety.serverLimit(distance, (MinecraftServer)(Object)this);
    }
}

