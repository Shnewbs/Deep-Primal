package com.shnewbs.deepprimal.performance.mixin;

import com.shnewbs.deepprimal.performance.MemorySafety;

import com.shnewbs.deepprimal.core.DistancePolicy;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    @org.spongepowered.asm.mixin.Shadow @org.spongepowered.asm.mixin.Final private net.minecraft.server.MinecraftServer server;
    @ModifyVariable(method = "setViewDistance", at = @At("HEAD"), argsOnly = true)
    private int deepprimal$renderLimit(int value) { return com.shnewbs.deepprimal.performance.MemorySafety.serverLimit(DistancePolicy.clampRender(value), server); }
    @ModifyVariable(method = "setSimulationDistance", at = @At("HEAD"), argsOnly = true)
    private int deepprimal$simulationLimit(int value) { return com.shnewbs.deepprimal.performance.MemorySafety.serverLimit(DistancePolicy.clampSimulation(value), server); }
}
