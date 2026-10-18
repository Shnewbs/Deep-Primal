package com.shnewbs.deepprimal.mixin;

import com.shnewbs.deepprimal.core.DistancePolicy;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    @ModifyVariable(method = "setViewDistance", at = @At("HEAD"), argsOnly = true)
    private int deepprimal$renderLimit(int value) { return DistancePolicy.clampRender(value); }
    @ModifyVariable(method = "setSimulationDistance", at = @At("HEAD"), argsOnly = true)
    private int deepprimal$simulationLimit(int value) { return DistancePolicy.clampSimulation(value); }
}
