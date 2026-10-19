package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.settings.ClientPerformanceConfig;
import com.shnewbs.deepprimal.client.DistanceScreen;
import com.shnewbs.deepprimal.core.VerticalRange;
import com.shnewbs.deepprimal.mixin.client.LevelRendererAccessor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.core.SectionPos;

/** Real tall-world rendering, vertical ring lookups, boundaries, and staged widgets. */
public final class TerrainClientChecks {
    public static void run(Minecraft mc) throws Exception {
        int horizontal = mc.options.getEffectiveRenderDistance();
        int fullCount = (2 * horizontal + 1) * (2 * horizontal + 1) * mc.level.getSectionsCount();
        ClientPerformanceConfig.VERTICAL_RENDER_DISTANCE.set(0); mc.levelRenderer.allChanged();
        check(area(mc).sections.length == fullCount, "full-height allocation");
        var screen = new DistanceScreen(null); mc.setScreen(screen);
        List<EditBox> inputs = screen.children().stream().filter(EditBox.class::isInstance).map(EditBox.class::cast).toList();
        Button apply = screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast)
            .filter(button -> button.getMessage().getString().equals("Apply")).findFirst().orElseThrow();
        inputs.get(2).setValue("97"); check(!apply.active, "invalid vertical radius rejected");
        inputs.get(2).setValue("2"); check(apply.active, "valid vertical radius accepted");
        screen.onClose(); check(ClientPerformanceConfig.VERTICAL_RENDER_DISTANCE.get() == 0, "cancel preserves vertical radius");
        mc.setScreen(screen);
        inputs = screen.children().stream().filter(EditBox.class::isInstance).map(EditBox.class::cast).toList();
        inputs.get(2).setValue("2");
        Method commit = DistanceScreen.class.getDeclaredMethod("applyChanges"); commit.setAccessible(true); commit.invoke(screen);
        check(ClientPerformanceConfig.VERTICAL_RENDER_DISTANCE.get() == 2, "Apply stores vertical radius");
        ViewArea area = area(mc); SectionPos base = area.getCameraSectionPos();
        check(area.sections.length == (2 * horizontal + 1) * (2 * horizontal + 1) * 5, "bounded vertical allocation");
        Method lookup = ViewArea.class.getDeclaredMethod("getRenderSection", int.class, int.class, int.class); lookup.setAccessible(true);
        int checked = 0;
        for (boolean fast : new boolean[]{true, false}) {
            ClientPerformanceConfig.FAST_VIEW_RELOCATION.set(fast);
            for (SectionPos camera : new SectionPos[]{base, SectionPos.of(-3, -2, -7), SectionPos.of(12, 28, -40),
                    SectionPos.of(500, mc.level.getMinSectionY(), -500), SectionPos.of(0, mc.level.getMaxSectionY(), 0)}) {
                area.repositionCamera(camera);
                int low = VerticalRange.low(camera.y(), mc.level.getMinSectionY(), mc.level.getSectionsCount(), 2);
                var unique = new HashSet<Long>();
                for (var section : area.sections) {
                    long node = section.getSectionNode(); int x = SectionPos.x(node), y = SectionPos.y(node), z = SectionPos.z(node);
                    check(unique.add(node), "unique live vertical section nodes");
                    check(y >= low && y < low + 5, "live node within allocated window");
                    if (Math.abs(y - camera.y()) <= 2) check(lookup.invoke(area, x, y, z) == section, "live lookup resolves exact node");
                    checked++;
                }
                check(lookup.invoke(area, camera.x(), camera.y() + 3, camera.z()) == null, "upper vertical limit");
                check(lookup.invoke(area, camera.x(), camera.y() - 3, camera.z()) == null, "lower vertical limit");
            }
        }
        mc.options.renderDistance().set(2);
        ClientPerformanceConfig.VERTICAL_RENDER_DISTANCE.set(8); mc.levelRenderer.allChanged();
        ViewArea tall = area(mc); SectionPos camera = tall.getCameraSectionPos();
        check(tall.getViewDistance() == 2 && tall.sections.length == 25 * 17, "vertical radius exceeds horizontal radius");
        int direction = camera.y() + 8 <= mc.level.getMaxSectionY() ? 1 : -1;
        var edge = lookup.invoke(tall, camera.x(), camera.y() + 7 * direction, camera.z());
        Method relative = net.minecraft.client.renderer.SectionOcclusionGraph.class.getDeclaredMethod("getRelativeFrom",
            long.class, net.minecraft.client.renderer.chunk.SectionRenderDispatcher.RenderSection.class, net.minecraft.core.Direction.class);
        relative.setAccessible(true);
        check(relative.invoke(mc.levelRenderer.getSectionOcclusionGraph(), camera.asLong(), edge,
            direction == 1 ? net.minecraft.core.Direction.UP : net.minecraft.core.Direction.DOWN) != null,
            "occlusion propagation exceeds horizontal radius vertically");
        Class<?> storageClass = Class.forName("net.minecraft.client.renderer.SectionOcclusionGraph$GraphStorage");
        var constructor = storageClass.getDeclaredConstructor(ViewArea.class); constructor.setAccessible(true);
        Object storage = constructor.newInstance(tall);
        var treeField = storageClass.getDeclaredField("sectionTree"); treeField.setAccessible(true);
        Object tree = treeField.get(storage);
        var rootField = tree.getClass().getDeclaredField("root"); rootField.setAccessible(true); Object root = rootField.get(tree);
        var boundsField = root.getClass().getDeclaredField("boundingBox"); boundsField.setAccessible(true);
        var bounds = (net.minecraft.world.level.levelgen.structure.BoundingBox)boundsField.get(root);
        for (var section : tall.sections) {
            int y = SectionPos.y(section.getSectionNode()) * 16;
            check(y >= bounds.minY() && y + 15 <= bounds.maxY(), "octree covers the entire vertical window");
        }
        mc.options.renderDistance().set(horizontal);
        ClientPerformanceConfig.FAST_VIEW_RELOCATION.set(true);
        ClientPerformanceConfig.VERTICAL_RENDER_DISTANCE.set(0); ClientPerformanceConfig.SPEC.save();
        mc.levelRenderer.allChanged(); check(area(mc).sections.length == fullCount, "full-height restore");
        mc.setScreen(null);
        System.out.println("TERRAIN_CLIENT_CHECKS_PASS verticalNodes=" + checked + " fullSections=" + fullCount
            + " boundedSections=" + area.sections.length + " worldSections=" + mc.level.getSectionsCount() + " independentVertical=true");
    }
    private static ViewArea area(Minecraft mc) { return ((LevelRendererAccessor)mc.levelRenderer).deepprimal$viewArea(); }
    private static void check(boolean result, String message) { if (!result) throw new AssertionError(message); }
}
