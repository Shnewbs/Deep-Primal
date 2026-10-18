package com.shnewbs.deepprimal.mixin.client;

import com.shnewbs.deepprimal.RuntimeMetrics;
import com.shnewbs.deepprimal.client.ClientPerformanceConfig;
import com.shnewbs.deepprimal.core.ViewGrid;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ViewArea.class)
public abstract class ViewAreaMixin {
    @Shadow @Final protected LevelRenderer levelRenderer;
    @Shadow @Final protected Level level;
    @Shadow protected int sectionGridSizeX;
    @Shadow protected int sectionGridSizeY;
    @Shadow protected int sectionGridSizeZ;
    @Shadow private int viewDistance;
    @Shadow private SectionPos cameraSectionPos;
    @Shadow public SectionRenderDispatcher.RenderSection[] sections;
    @Unique private boolean deepprimal$hasRelocated;

    @Inject(method = "repositionCamera", at = @At("HEAD"), cancellable = true)
    private void deepprimal$moveChangedColumns(SectionPos camera, CallbackInfo callback) {
        if (!ClientPerformanceConfig.FAST_VIEW_RELOCATION.get()) return;
        long moved = 0;
        if (!deepprimal$hasRelocated || camera.x() != cameraSectionPos.x() || camera.z() != cameraSectionPos.z()) {
            for (int x = 0; x < sectionGridSizeX; x++) {
                int targetX = ViewGrid.coordinate(camera.x(), viewDistance, x);
                for (int z = 0; z < sectionGridSizeZ; z++) {
                    int targetZ = ViewGrid.coordinate(camera.z(), viewDistance, z);
                    int base = z * sectionGridSizeY * sectionGridSizeX + x;
                    long existing = sections[base].getSectionNode();
                    if (SectionPos.x(existing) == targetX && SectionPos.z(existing) == targetZ) continue;
                    for (int y = 0; y < sectionGridSizeY; y++) {
                        sections[base + y * sectionGridSizeX].setSectionNode(
                            SectionPos.asLong(targetX, y + level.getMinSectionY(), targetZ));
                    }
                    moved += sectionGridSizeY;
                }
            }
            RuntimeMetrics.VIEW_COLUMNS.add((long)sectionGridSizeX * sectionGridSizeZ);
        }
        RuntimeMetrics.VIEW_SECTIONS_MOVED.add(moved);
        deepprimal$hasRelocated = true;
        cameraSectionPos = camera;
        levelRenderer.getSectionOcclusionGraph().invalidate();
        callback.cancel();
    }
}
