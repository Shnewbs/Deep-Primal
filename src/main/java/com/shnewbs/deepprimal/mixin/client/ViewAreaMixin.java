package com.shnewbs.deepprimal.mixin.client;

import com.shnewbs.deepprimal.performance.RuntimeMetrics;
import com.shnewbs.deepprimal.settings.ClientPerformanceConfig;
import com.shnewbs.deepprimal.client.VerticalView;
import com.shnewbs.deepprimal.core.ViewGrid;
import com.shnewbs.deepprimal.core.VerticalRange;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ViewArea.class)
public abstract class ViewAreaMixin implements VerticalView {
    @Shadow @Final protected LevelRenderer levelRenderer;
    @Shadow @Final protected Level level;
    @Shadow protected int sectionGridSizeX;
    @Shadow protected int sectionGridSizeY;
    @Shadow protected int sectionGridSizeZ;
    @Shadow private int viewDistance;
    @Shadow private SectionPos cameraSectionPos;
    @Shadow public SectionRenderDispatcher.RenderSection[] sections;
    @Unique private boolean deepprimal$hasRelocated;
    @Unique private int deepprimal$verticalRadius;

    @Inject(method = "setViewDistance", at = @At("RETURN"))
    private void deepprimal$sizeVerticalWindow(int distance, CallbackInfo callback) {
        deepprimal$verticalRadius = ClientPerformanceConfig.VERTICAL_RENDER_DISTANCE.get();
        sectionGridSizeY = VerticalRange.count(level.getSectionsCount(), deepprimal$verticalRadius);
    }

    @Override public int deepprimal$verticalRadius() {
        return deepprimal$verticalRadius == 0 ? level.getSectionsCount() : deepprimal$verticalRadius;
    }

    @Override public int deepprimal$minimumSectionY() {
        return VerticalRange.low(cameraSectionPos.y(), level.getMinSectionY(), level.getSectionsCount(), deepprimal$verticalRadius);
    }

    @Inject(method = "getLevelHeightAccessor", at = @At("HEAD"), cancellable = true)
    private void deepprimal$windowHeight(CallbackInfoReturnable<LevelHeightAccessor> callback) {
        if (deepprimal$verticalRadius > 0)
            callback.setReturnValue(LevelHeightAccessor.create(deepprimal$minimumSectionY() * 16, sectionGridSizeY * 16));
    }

    @Inject(method = "containsSection", at = @At("HEAD"), cancellable = true)
    private void deepprimal$containsVertical(int x, int y, int z, CallbackInfoReturnable<Boolean> callback) {
        if (deepprimal$verticalRadius > 0) callback.setReturnValue(deepprimal$contains(x, y, z));
    }

    @Unique private boolean deepprimal$contains(int x, int y, int z) {
        return y >= level.getMinSectionY() && y <= level.getMaxSectionY()
            && Math.abs((long)y - cameraSectionPos.y()) <= deepprimal$verticalRadius
            && Math.abs((long)x - cameraSectionPos.x()) <= viewDistance
            && Math.abs((long)z - cameraSectionPos.z()) <= viewDistance;
    }

    @Inject(method = "getRenderSection(III)Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher$RenderSection;",
        at = @At("HEAD"), cancellable = true)
    private void deepprimal$lookupVertical(int x, int y, int z,
            CallbackInfoReturnable<SectionRenderDispatcher.RenderSection> callback) {
        if (deepprimal$verticalRadius == 0) return;
        if (!deepprimal$contains(x, y, z)) { callback.setReturnValue(null); return; }
        int slotY = sectionGridSizeY == level.getSectionsCount()
            ? y - level.getMinSectionY() : Math.floorMod(y, sectionGridSizeY);
        int index = (Math.floorMod(z, sectionGridSizeZ) * sectionGridSizeY + slotY) * sectionGridSizeX
            + Math.floorMod(x, sectionGridSizeX);
        callback.setReturnValue(sections[index]);
    }

    @Inject(method = "repositionCamera", at = @At("HEAD"), cancellable = true)
    private void deepprimal$moveChangedColumns(SectionPos camera, CallbackInfo callback) {
        boolean fast = ClientPerformanceConfig.FAST_VIEW_RELOCATION.get();
        boolean window = sectionGridSizeY < level.getSectionsCount();
        if (!fast && !window) return;
        long moved = 0;
        if (!deepprimal$hasRelocated || !fast || camera.x() != cameraSectionPos.x() || camera.z() != cameraSectionPos.z()
                || (window && camera.y() != cameraSectionPos.y())) {
            int lowY = VerticalRange.low(camera.y(), level.getMinSectionY(), level.getSectionsCount(), deepprimal$verticalRadius);
            for (int x = 0; x < sectionGridSizeX; x++) {
                int targetX = ViewGrid.coordinate(camera.x(), viewDistance, x);
                for (int z = 0; z < sectionGridSizeZ; z++) {
                    int targetZ = ViewGrid.coordinate(camera.z(), viewDistance, z);
                    int base = z * sectionGridSizeY * sectionGridSizeX + x;
                    long existing = sections[base].getSectionNode();
                    boolean changedColumn = SectionPos.x(existing) != targetX || SectionPos.z(existing) != targetZ;
                    if (fast && !window && !changedColumn) continue;
                    for (int y = 0; y < sectionGridSizeY; y++) {
                        int targetY = window ? VerticalRange.coordinate(lowY, sectionGridSizeY, y) : y + level.getMinSectionY();
                        var section = sections[base + y * sectionGridSizeX];
                        long target = SectionPos.asLong(targetX, targetY, targetZ);
                        if (section.getSectionNode() == target) continue;
                        section.setSectionNode(target);
                        moved++;
                    }
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
