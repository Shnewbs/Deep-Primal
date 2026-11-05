package com.shnewbs.deepprimal.mixin.client;

import com.shnewbs.deepprimal.client.VerticalView;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import net.minecraft.client.renderer.ViewArea;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SectionOcclusionGraph.class)
public abstract class SectionOcclusionGraphMixin {
    @Redirect(method = "getRelativeFrom", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/ViewArea;getViewDistance()I"))
    private int deepprimal$independentVerticalRange(ViewArea area) {
        return ((VerticalView)area).deepprimal$verticalRadius();
    }
}
