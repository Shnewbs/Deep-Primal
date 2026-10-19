package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.client.DistanceScreen;
import com.shnewbs.deepprimal.mixin.client.LevelRendererAccessor;
import com.shnewbs.deepprimal.performance.DistanceRuntime;
import com.shnewbs.deepprimal.performance.RuntimeMetrics;
import com.shnewbs.deepprimal.settings.ClientPerformanceConfig;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.core.SectionPos;

/**
 * Exercises real widgets, persistence, live section nodes and the native fallback on a joined
 * client.
 */
public final class DistanceClientChecks {
  public static void run(Minecraft mc) throws Exception {
    if (mc.getSingleplayerServer() != null)
      check(DistanceRuntime.trackingCapacity() == 33, "integrated tracking capacity");
    check(mc.options.renderDistance().get() == 33, "loaded render distance above native 32");
    mc.options.renderDistance().set(96);
    mc.options.simulationDistance().set(2);
    mc.options.save();
    mc.options.renderDistance().set(2);
    mc.options.simulationDistance().set(32);
    mc.options.load();
    check(
        mc.options.renderDistance().get() == 96 && mc.options.simulationDistance().get() == 2,
        "distance save/load");
    mc.options.renderDistance().set(33);
    mc.options.save();
    DistanceScreen screen = new DistanceScreen(new VideoSettingsScreen(null, mc, mc.options));
    mc.setScreen(screen);
    List<EditBox> inputs =
        screen.children().stream()
            .filter(EditBox.class::isInstance)
            .map(EditBox.class::cast)
            .toList();
    Button apply =
        screen.children().stream()
            .filter(Button.class::isInstance)
            .map(Button.class::cast)
            .filter(button -> button.getMessage().getString().equals("Apply"))
            .findFirst()
            .orElseThrow();
    inputs.get(0).setValue("97");
    check(!apply.active, "invalid render rejected");
    inputs.get(0).setValue("48");
    inputs.get(1).setValue("33");
    check(!apply.active, "invalid simulation rejected");
    inputs.get(1).setValue("2");
    check(apply.active, "valid typed distances accepted");
    if (mc.getSingleplayerServer() != null)
      check(DistanceRuntime.trackingCapacity() == 33, "integrated tracking capacity");
    check(mc.options.renderDistance().get() == 33, "staged edits leave live setting unchanged");
    screen.onClose();
    check(mc.options.renderDistance().get() == 33, "cancel preserves live setting");
    mc.setScreen(screen);
    inputs =
        screen.children().stream()
            .filter(EditBox.class::isInstance)
            .map(EditBox.class::cast)
            .toList();
    inputs.get(0).setValue("48");
    inputs.get(1).setValue("2");
    Method applyChanges = DistanceScreen.class.getDeclaredMethod("applyChanges");
    applyChanges.setAccessible(true);
    applyChanges.invoke(screen);
    check(
        mc.options.renderDistance().get() == 48 && mc.options.simulationDistance().get() == 2,
        "Apply commits typed values");
    mc.options.load();
    check(mc.options.renderDistance().get() == 48, "typed render persists");
    if (mc.getSingleplayerServer() != null) {
      var advertised = mc.options.getClass().getDeclaredField("serverRenderDistance");
      advertised.setAccessible(true);
      int previous = advertised.getInt(mc.options);
      advertised.setInt(mc.options, 96);
      check(
          mc.options.getEffectiveRenderDistance() == 33,
          "integrated render respects allocated tracking capacity");
      advertised.setInt(mc.options, previous);
    }
    ViewArea area = ((LevelRendererAccessor) mc.levelRenderer).deepprimal$viewArea();
    check(area.getViewDistance() == 33, "server-delivered render distance above 32");
    SectionPos base = area.getCameraSectionPos();
    for (SectionPos target :
        new SectionPos[] {
          SectionPos.of(base.x() + 1, base.y(), base.z()),
          SectionPos.of(base.x(), base.y() + 1, base.z()),
          SectionPos.of(-3, base.y(), -3),
          SectionPos.of(200, base.y(), -300)
        }) {
      ClientPerformanceConfig.FAST_VIEW_RELOCATION.set(true);
      area.repositionCamera(target);
      long[] optimized = nodes(area);
      area.repositionCamera(base);
      ClientPerformanceConfig.FAST_VIEW_RELOCATION.set(false);
      area.repositionCamera(target);
      check(
          Arrays.equals(optimized, nodes(area)), "all live section nodes match native relocation");
      area.repositionCamera(base);
    }
    // An actual spawn can equal the constructor's sentinel camera coordinates.
    var initialized = ViewArea.class.getDeclaredField("deepprimal$hasRelocated");
    initialized.setAccessible(true);
    var cameraField = ViewArea.class.getDeclaredField("cameraSectionPos");
    cameraField.setAccessible(true);
    SectionPos sentinel = SectionPos.of(area.getViewDistance() + 1, 0, area.getViewDistance() + 1);
    initialized.setBoolean(area, false);
    cameraField.set(area, sentinel);
    ClientPerformanceConfig.FAST_VIEW_RELOCATION.set(true);
    area.repositionCamera(sentinel);
    long[] firstMove = nodes(area);
    area.repositionCamera(base);
    ClientPerformanceConfig.FAST_VIEW_RELOCATION.set(false);
    area.repositionCamera(sentinel);
    check(Arrays.equals(firstMove, nodes(area)), "first relocation at constructor sentinel");
    area.repositionCamera(base);
    long nativeNanos = timeMoves(area, base, false), optimizedNanos = timeMoves(area, base, true);
    ClientPerformanceConfig.FAST_VIEW_RELOCATION.set(true);
    area.repositionCamera(base);
    mc.setScreen(new DistanceScreen(new VideoSettingsScreen(null, mc, mc.options)));
    System.out.println(
        "DISTANCE_CHECKS_PASS sections="
            + area.sections.length
            + " nativeMoveNanos="
            + nativeNanos
            + " optimizedMoveNanos="
            + optimizedNanos
            + " farChunk33=true integrated="
            + (mc.getSingleplayerServer() != null)
            + " maxSendBatch="
            + RuntimeMetrics.MAX_SEND_BATCH.get());
  }

  private static long timeMoves(ViewArea area, SectionPos base, boolean optimized) {
    ClientPerformanceConfig.FAST_VIEW_RELOCATION.set(optimized);
    area.repositionCamera(base);
    for (int i = 1; i <= 10; i++)
      area.repositionCamera(SectionPos.of(base.x() + i, base.y(), base.z()));
    area.repositionCamera(base);
    long started = System.nanoTime();
    for (int i = 1; i <= 30; i++)
      area.repositionCamera(SectionPos.of(base.x() + i, base.y(), base.z()));
    long elapsed = System.nanoTime() - started;
    area.repositionCamera(base);
    return elapsed;
  }

  private static long[] nodes(ViewArea area) {
    long[] result = new long[area.sections.length];
    for (int i = 0; i < result.length; i++) result[i] = area.sections[i].getSectionNode();
    return result;
  }

  private static void check(boolean result, String message) {
    if (!result) throw new AssertionError(message);
  }
}
