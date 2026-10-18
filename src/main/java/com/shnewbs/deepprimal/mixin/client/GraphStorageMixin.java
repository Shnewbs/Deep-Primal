package com.shnewbs.deepprimal.mixin.client;

import com.shnewbs.deepprimal.client.VerticalView;
import net.minecraft.client.renderer.ViewArea;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(targets = "net.minecraft.client.renderer.SectionOcclusionGraph$GraphStorage")
public abstract class GraphStorageMixin {
    @ModifyArgs(method = "<init>", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/Octree;<init>(Lnet/minecraft/core/SectionPos;III)V"))
    private void deepprimal$fitVerticalOctree(Args args, ViewArea area) {
        VerticalView vertical = (VerticalView)area;
        args.set(1, Math.max(area.getViewDistance(), vertical.deepprimal$verticalRadius()));
        args.set(3, vertical.deepprimal$minimumSectionY() * 16);
    }
}
