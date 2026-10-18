package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.client.render.HorizonRenderer;
import com.shnewbs.deepprimal.render.RendererOwnership;
import com.shnewbs.deepprimal.mixin.client.LevelRendererAccessor;
import net.minecraft.client.Minecraft;

/** Real native GPU and mixin handoff checks; excluded from the distributed JAR. */
public final class RendererClientChecks {
    public static void run(Minecraft mc) throws Exception {
        boolean pack = mc.getResourceManager().listPacks().anyMatch(p -> p.packId().contains("renderer/native"));
        if (RendererOwnership.owned()) {
            if (!pack) throw new AssertionError("Native renderer resource pack missing");
            var renderer = HorizonRenderer.INSTANCE;
            if (renderer.cachedTiles < 12 || renderer.drawnTriangles <= 0 || renderer.visibleTiles <= 0)
                throw new AssertionError("Horizon mesh not rendered: " + renderer.status + " cache=" + renderer.cachedTiles + " visible=" + renderer.visibleTiles + " triangles=" + renderer.drawnTriangles);
            if (mc.level.getChunkSource().getLoadedChunksCount() == 0) throw new AssertionError("Near chunks were not delivered");
            mc.options.cloudStatus().set(net.minecraft.client.CloudStatus.OFF);
            var screen = new com.shnewbs.deepprimal.client.DistanceScreen(null); mc.setScreen(screen);
            var inputs = screen.children().stream().filter(net.minecraft.client.gui.components.EditBox.class::isInstance)
                .map(net.minecraft.client.gui.components.EditBox.class::cast).toList();
            var apply = screen.children().stream().filter(net.minecraft.client.gui.components.Button.class::isInstance)
                .map(net.minecraft.client.gui.components.Button.class::cast).filter(b -> b.getMessage().getString().equals("Apply")).findFirst().orElseThrow();
            int original = com.shnewbs.deepprimal.client.ClientPerformanceConfig.FAR_RENDER_DISTANCE.get();
            inputs.get(3).setValue("257"); if (apply.active) throw new AssertionError("Invalid far radius accepted");
            inputs.get(3).setValue("192"); if (!apply.active) throw new AssertionError("Valid far radius rejected");
            screen.onClose(); if (com.shnewbs.deepprimal.client.ClientPerformanceConfig.FAR_RENDER_DISTANCE.get() != original) throw new AssertionError("Cancel changed far radius");
            var view = ((LevelRendererAccessor)mc.levelRenderer).deepprimal$viewArea();
            if (view.getViewDistance() != mc.options.getEffectiveRenderDistance()) throw new AssertionError("Native near grid changed");
            System.out.println("NATIVE_RENDERER_CHECKS_PASS near=" + view.getViewDistance() + " far=" + HorizonRenderer.farDistance()
                + " cached=" + renderer.cachedTiles + " visible=" + renderer.visibleTiles + " triangles=" + renderer.drawnTriangles
                + " clientChunks=" + mc.level.getChunkSource().getLoadedChunksCount() + " gpuUploadBytes=" + renderer.uploadedBytes + " owner=" + RendererOwnership.owner());
        } else {
            if (pack || java.util.Arrays.stream(mc.levelRenderer.getClass().getInterfaces()).anyMatch(i -> i.getName().endsWith("LevelRendererAccessor"))) throw new AssertionError("Renderer handoff retained native resources or patches");
            if (HorizonRenderer.farDistance() != 0) throw new AssertionError("Horizon remains active with replacement");
            mc.setScreen(new com.shnewbs.deepprimal.client.DistanceScreen(null));
            System.out.println("RENDERER_HANDOFF_CHECKS_PASS owner=" + RendererOwnership.owner() + " nativePack=false nativeMixin=false horizon=0");
            mc.setScreen(null);
        }
    }
    private RendererClientChecks() {}
}
