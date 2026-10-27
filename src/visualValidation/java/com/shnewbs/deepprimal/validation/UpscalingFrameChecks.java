package com.shnewbs.deepprimal.validation;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.shnewbs.deepprimal.core.UpscalingPolicy;
import com.shnewbs.deepprimal.settings.LodConfig;
import com.shnewbs.deepprimal.settings.RendererOwnership;
import com.shnewbs.deepprimal.settings.UpscalingConfig;
import com.shnewbs.deepprimal.settings.UpscalingRuntime;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.ConfirmExperimentalFeaturesScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

/**
 * Native world-frame acceptance fixture. Creates its own disposable Creative superflat world;
 * exercises real GameRenderer mixins, GPU passes, native HUD target, resize/reload and disconnect.
 * This is functional coverage, not a performance benchmark or DLSS hardware acceptance.
 */
@Mod(value = "deepprimal", dist = Dist.CLIENT)
public final class UpscalingFrameChecks {
  private enum Phase {
    TITLE, CREATE, JOIN, FSR, FSR_CAPTURE, SPATIAL, DLSS, DLSS_CAPTURE,
    OFF, ODD_FSR, ODD_CAPTURE, RELOADED_FSR, RELOAD_CAPTURE, DISCONNECTED, DONE
  }

  private static final long STAGE_TIMEOUT = 120_000_000_000L;
  private static final int MIN_FRAMES = 8;
  private Phase phase = Phase.TITLE;
  private long started, phaseStarted, hostFrames, phaseHostFrames, passFrames, fsrFrames;
  private boolean prepared, restored, reloadObserved;
  private Screen acceptedWarning;
  private RenderTarget nativeTarget;
  private volatile boolean capturePending;
  private String captureName;
  private int captureWidth, captureHeight;
  private CompletableFuture<Void> reload;
  private final StringBuilder evidence = new StringBuilder();
  private UpscalingPolicy.Mode oldMode;
  private UpscalingPolicy.Quality oldQuality;
  private double oldScale, oldSharpness;
  private boolean oldHideGui, oldPause, oldLod, oldVsync;
  private int oldRenderDistance, oldSimulationDistance, oldLimit, oldWidth, oldHeight;

  public UpscalingFrameChecks() {
    NeoForge.EVENT_BUS.register(this);
  }

  private static void require(boolean value, String message) {
    if (!value) throw new AssertionError(message);
  }

  private static boolean enabled() {
    return Boolean.getBoolean("deepprimal.validationUpscalingFrames");
  }

  private Path output() {
    return Path.of(System.getProperty("deepprimal.validationCapture", "build/upscaling-frames"));
  }

  private void report(String line) throws IOException {
    evidence.append(line).append('\n');
    Files.createDirectories(output());
    Files.writeString(output().resolve("upscaling-frames-result.txt"), evidence);
    System.out.println(line);
  }

  private void next(Phase next) {
    phase = next;
    phaseStarted = System.nanoTime();
    phaseHostFrames = hostFrames;
    var d = UpscalingRuntime.diagnostics();
    passFrames = d.completedFrames();
    fsrFrames = d.fsrFrames();
  }

  private void select(UpscalingPolicy.Mode mode, UpscalingPolicy.Quality quality, double scale) {
    UpscalingConfig.MODE.set(mode);
    UpscalingConfig.QUALITY.set(quality);
    UpscalingConfig.RENDER_SCALE.set(scale);
    UpscalingConfig.SHARPNESS.set(.35);
    UpscalingConfig.applyChanges();
  }

  private void prepare(Minecraft mc) throws IOException {
    require(RendererOwnership.owned(), "Fixture requires Deep Primal renderer ownership");
    String expectedBackend = System.getProperty("deepprimal.graphicsBackend");
    String backend = RenderSystem.getDevice().getBackendName();
    if (expectedBackend != null)
      require(backend.equalsIgnoreCase(expectedBackend), "Requested backend is active: " + backend);
    oldMode = UpscalingConfig.MODE.get();
    oldQuality = UpscalingConfig.QUALITY.get();
    oldScale = UpscalingConfig.RENDER_SCALE.get();
    oldSharpness = UpscalingConfig.SHARPNESS.get();
    oldHideGui = mc.options.hideGui;
    oldPause = mc.options.pauseOnLostFocus;
    oldLod = LodConfig.ENABLED.get();
    oldVsync = mc.options.enableVsync().get();
    oldRenderDistance = mc.options.renderDistance().get();
    oldSimulationDistance = mc.options.simulationDistance().get();
    oldLimit = mc.options.framerateLimit().get();
    oldWidth = mc.getWindow().getScreenWidth();
    oldHeight = mc.getWindow().getScreenHeight();
    prepared = true;
    mc.options.hideGui = false;
    mc.options.pauseOnLostFocus = false;
    mc.options.renderDistance().set(3);
    mc.options.simulationDistance().set(5);
    mc.options.framerateLimit().set(30);
    mc.options.enableVsync().set(false);
    LodConfig.ENABLED.set(false);
    select(UpscalingPolicy.Mode.NONE, UpscalingPolicy.Quality.QUALITY, .63);
    GLFW.glfwSetWindowSize(mc.getWindow().handle(), 800, 600);
    report("UPSCALING_FRAME_ENV backend=" + backend
        + " fixture=creative-superflat scope=functional-native-world-path hardwarePerformance=false");
    next(Phase.CREATE);
    CreateWorldScreen.testWorld(mc, () -> mc.setScreen(new TitleScreen()));
    require(mc.screen instanceof CreateWorldScreen, "Native test-world creation form opened");
    var ui = ((CreateWorldScreen) mc.screen).getUiState();
    ui.setName("Upscaling-frame-fixture-" + System.currentTimeMillis());
    ui.setSeed("20261027");
    ui.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);
    ui.setAllowCommands(true);
    ui.setGenerateStructures(false);
  }

  private void restore(Minecraft mc) {
    if (!prepared || restored) return;
    restored = true;
    UpscalingConfig.MODE.set(oldMode);
    UpscalingConfig.QUALITY.set(oldQuality);
    UpscalingConfig.RENDER_SCALE.set(oldScale);
    UpscalingConfig.SHARPNESS.set(oldSharpness);
    UpscalingConfig.SPEC.save();
    UpscalingConfig.applyChanges();
    LodConfig.ENABLED.set(oldLod);
    mc.options.hideGui = oldHideGui;
    mc.options.pauseOnLostFocus = oldPause;
    mc.options.renderDistance().set(oldRenderDistance);
    mc.options.simulationDistance().set(oldSimulationDistance);
    mc.options.framerateLimit().set(oldLimit);
    mc.options.enableVsync().set(oldVsync);
    GLFW.glfwSetWindowSize(mc.getWindow().handle(), oldWidth, oldHeight);
  }

  private static void press(Minecraft mc, String label) {
    for (var child : mc.screen.children()) {
      if (child instanceof Button button && button.getMessage().getString().equals(label)) {
        require(button.active, "Button enabled: " + label);
        button.onPress(null);
        return;
      }
    }
    throw new AssertionError("Missing button " + label + " on " + mc.screen.getClass());
  }

  private void acceptWorldWarning(Minecraft mc) {
    if (mc.level != null || mc.screen == acceptedWarning || mc.screen == null) return;
    if (!(mc.screen instanceof ConfirmExperimentalFeaturesScreen
        || mc.screen instanceof BackupConfirmScreen || mc.screen instanceof ConfirmScreen)) return;
    acceptedWarning = mc.screen;
    for (var child : mc.screen.children()) {
      if (child instanceof Button button && button.active
          && (button.getMessage().getString().equals("Proceed")
              || button.getMessage().getString().equals("I know what I'm doing!")
              || button.getMessage().getString().equals("Yes"))) {
        button.onPress(null);
        return;
      }
    }
  }

  private boolean enoughFrames() {
    return hostFrames - phaseHostFrames >= MIN_FRAMES;
  }

  private boolean enoughPasses(UpscalingPolicy.Mode mode) {
    var d = UpscalingRuntime.diagnostics();
    return enoughFrames() && d.completedFrames() - passFrames >= MIN_FRAMES
        && (mode != UpscalingPolicy.Mode.FSR || d.fsrFrames() - fsrFrames >= MIN_FRAMES);
  }

  /** Called after complete frames, outside the world-target override. */
  private void nativeHud(Minecraft mc) {
    var target = mc.getMainRenderTarget();
    require(target == nativeTarget, "Native main-target identity survives upscaling");
    require(target.width == mc.getWindow().getWidth()
            && target.height == mc.getWindow().getHeight(),
        "Main/HUD target retains physical display dimensions");
    require(!mc.options.hideGui, "HUD stays visible");
    var state = mc.gameRenderer.getGameRenderState();
    require(!state.optionsRenderState.hideGui, "Extracted HUD remains enabled");
    require(state.windowRenderState.width == target.width
            && state.windowRenderState.height == target.height,
        "World's temporary render dimensions are restored before GUI rendering");
    require(UpscalingRuntime.frameTarget() == null
            && !UpscalingRuntime.diagnostics().worldTargetBound(),
        "World target cannot leak into later HUD or menu rendering");
  }

  private void scaledFrame(Minecraft mc, UpscalingPolicy.Mode mode, double expectedScale)
      throws IOException {
    nativeHud(mc);
    var d = UpscalingRuntime.diagnostics();
    require(d.activeMode() == mode && UpscalingConfig.getActiveMode() == mode,
        "Only successfully dispatched requested mode is active: " + mode);
    require(Math.abs(UpscalingConfig.activeScale() - expectedScale) < 1e-9,
        "Actual mode reports requested scale");
    require(d.sourceWidth() == (int) Math.ceil(nativeTarget.width * expectedScale)
            && d.sourceHeight() == (int) Math.ceil(nativeTarget.height * expectedScale),
        "World target uses rounded-up render dimensions");
    require(d.outputWidth() == nativeTarget.width && d.outputHeight() == nativeTarget.height,
        "Upscaler writes full display extent");
    require(d.worldAllocated(), "Reduced world target exists");
    require(mode != UpscalingPolicy.Mode.FSR || d.fsrAllocated(), "Real FSR resources exist");
    require(UpscalingConfig.unavailableReason(mode).isEmpty(), "Supported mode has no runtime failure");
    report("UPSCALING_FRAME_PASS phase=" + phase + " mode=" + mode
        + " source=" + d.sourceWidth() + "x" + d.sourceHeight()
        + " output=" + d.outputWidth() + "x" + d.outputHeight()
        + " dispatches=" + (d.completedFrames() - passFrames)
        + " fsrDispatches=" + (d.fsrFrames() - fsrFrames)
        + " hostFrames=" + (hostFrames - phaseHostFrames) + " nativeHud=true targetRestored=true");
  }

  private void nativeFrame(Minecraft mc, String reason) throws IOException {
    nativeHud(mc);
    var d = UpscalingRuntime.diagnostics();
    require(UpscalingConfig.getActiveMode() == UpscalingPolicy.Mode.NONE
            && UpscalingConfig.activeScale() == 1.0,
        "Native fallback/off reports native resolution");
    require(d.completedFrames() == passFrames && d.fsrFrames() == fsrFrames,
        "Native fallback/off never dispatches an upscaler");
    require(!d.worldAllocated() && !d.fsrAllocated(), "Inactive upscaler releases its targets");
    report("UPSCALING_NATIVE_PASS phase=" + phase + " reason=" + reason
        + " output=" + nativeTarget.width + "x" + nativeTarget.height
        + " hostFrames=" + (hostFrames - phaseHostFrames)
        + " nativeHud=true dispatches=0 resourcesReleased=true");
  }

  private void capture(Minecraft mc, String name) throws IOException {
    Files.createDirectories(output());
    captureName = name;
    captureWidth = nativeTarget.width;
    captureHeight = nativeTarget.height;
    capturePending = true;
    Screenshot.grab(output().toFile(), name, mc.getMainRenderTarget(), 1,
        message -> capturePending = false);
  }

  /** Readback proves complete native-size output was submitted and contains a rendered image. */
  private void verifyCapture() throws IOException {
    var file = output().resolve("screenshots").resolve(captureName);
    require(Files.isRegularFile(file), "Native capture saved: " + captureName);
    var image = javax.imageio.ImageIO.read(file.toFile());
    require(image != null && image.getWidth() == captureWidth && image.getHeight() == captureHeight,
        "Captured framebuffer has native output dimensions");
    int samples = 0, nonBlack = 0;
    var colors = new HashSet<Integer>();
    for (int y = 8; y < image.getHeight() * 3 / 4; y += 8) {
      for (int x = 8; x < image.getWidth() - 8; x += 8) {
        int rgb = image.getRGB(x, y) & 0xffffff;
        samples++;
        if (rgb != 0) nonBlack++;
        colors.add(rgb);
      }
    }
    require(samples > 100 && nonBlack > samples * .8 && colors.size() > 8,
        "World readback contains nonblank geometry/sky instead of an empty frame");
    report("UPSCALING_READBACK_PASS file=" + captureName + " width=" + image.getWidth()
        + " height=" + image.getHeight() + " sampledColors=" + colors.size()
        + " nonBlack=" + nonBlack + "/" + samples);
  }

  @SubscribeEvent
  public void rendered(RenderFrameEvent.Post event) {
    if (enabled() && phase != Phase.DONE) hostFrames++;
  }

  @SubscribeEvent
  public void tick(ClientTickEvent.Post event) {
    if (!enabled() || phase == Phase.DONE) return;
    Minecraft mc = Minecraft.getInstance();
    try {
      long now = System.nanoTime();
      if (started == 0) { started = now; phaseStarted = now; }
      if (now - started > 600_000_000_000L || now - phaseStarted > STAGE_TIMEOUT)
        throw new AssertionError("Upscaling frame validation timed out at " + phase
            + "; diagnostics=" + UpscalingRuntime.diagnostics()
            + "; reason=" + UpscalingConfig.unavailableReason());
      if (phase == Phase.TITLE && mc.getOverlay() == null
          && mc.screen instanceof AccessibilityOnboardingScreen) {
        mc.screen.onClose();
        return;
      }
      if (mc.getOverlay() != null) return;
      switch (phase) {
        case TITLE -> {
          if (mc.screen instanceof TitleScreen) prepare(mc);
        }
        case CREATE -> {
          if (mc.screen instanceof CreateWorldScreen && enoughFrames()) {
            next(Phase.JOIN);
            press(mc, "Create New World");
          }
        }
        case JOIN -> {
          acceptWorldWarning(mc);
          if (mc.level == null || mc.player == null || mc.screen != null) return;
          require(mc.getSingleplayerServer() != null, "Fixture is a real integrated world");
          require(mc.getSingleplayerServer().overworld().getChunkSource().getGenerator()
                  instanceof FlatLevelSource,
              "Bounded native superflat fixture selected");
          nativeTarget = mc.getMainRenderTarget();
          select(UpscalingPolicy.Mode.FSR, UpscalingPolicy.Quality.QUALITY, .63);
          next(Phase.FSR);
        }
        case FSR -> {
          if (!enoughPasses(UpscalingPolicy.Mode.FSR)) return;
          scaledFrame(mc, UpscalingPolicy.Mode.FSR, 1.0 / 1.5);
          capture(mc, "01-fsr-quality.png");
          next(Phase.FSR_CAPTURE);
        }
        case FSR_CAPTURE -> {
          if (capturePending) return;
          verifyCapture();
          select(UpscalingPolicy.Mode.SPATIAL, UpscalingPolicy.Quality.CUSTOM, .63);
          next(Phase.SPATIAL);
        }
        case SPATIAL -> {
          if (!enoughPasses(UpscalingPolicy.Mode.SPATIAL)) return;
          scaledFrame(mc, UpscalingPolicy.Mode.SPATIAL, .63);
          require(UpscalingRuntime.diagnostics().fsrFrames() == fsrFrames,
              "Spatial mode does not invoke FSR");
          select(UpscalingPolicy.Mode.DLSS, UpscalingPolicy.Quality.QUALITY, .63);
          next(Phase.DLSS);
        }
        case DLSS -> {
          if (!enoughFrames()) return;
          require(UpscalingConfig.unavailableReason().contains("DLSS"),
              "Unsupported DLSS explains its unavailable temporal provider");
          nativeFrame(mc, "DLSS unavailable");
          capture(mc, "02-dlss-native-fallback.png");
          next(Phase.DLSS_CAPTURE);
        }
        case DLSS_CAPTURE -> {
          if (capturePending) return;
          verifyCapture();
          select(UpscalingPolicy.Mode.NONE, UpscalingPolicy.Quality.QUALITY, .63);
          next(Phase.OFF);
        }
        case OFF -> {
          if (!enoughFrames()) return;
          nativeFrame(mc, "Off");
          select(UpscalingPolicy.Mode.FSR, UpscalingPolicy.Quality.QUALITY, .63);
          GLFW.glfwSetWindowSize(mc.getWindow().handle(), 801, 601);
          next(Phase.ODD_FSR);
        }
        case ODD_FSR -> {
          if (mc.getWindow().getWidth() != 801 || mc.getWindow().getHeight() != 601
              || !enoughPasses(UpscalingPolicy.Mode.FSR)) return;
          scaledFrame(mc, UpscalingPolicy.Mode.FSR, 1.0 / 1.5);
          capture(mc, "03-fsr-odd-size.png");
          next(Phase.ODD_CAPTURE);
        }
        case ODD_CAPTURE -> {
          if (capturePending) return;
          verifyCapture();
          next(Phase.RELOADED_FSR);
          reload = mc.reloadResourcePacks();
        }
        case RELOADED_FSR -> {
          if (reload == null || !reload.isDone()) return;
          reload.join();
          if (!reloadObserved) {
            reloadObserved = true;
            next(Phase.RELOADED_FSR);
            return;
          }
          if (!enoughPasses(UpscalingPolicy.Mode.FSR)) return;
          scaledFrame(mc, UpscalingPolicy.Mode.FSR, 1.0 / 1.5);
          capture(mc, "04-fsr-reloaded.png");
          next(Phase.RELOAD_CAPTURE);
        }
        case RELOAD_CAPTURE -> {
          if (capturePending) return;
          verifyCapture();
          next(Phase.DISCONNECTED);
          mc.disconnectWithSavingScreen();
        }
        case DISCONNECTED -> {
          if (mc.level != null || !enoughFrames()) return;
          var d = UpscalingRuntime.diagnostics();
          require(!d.worldAllocated() && !d.fsrAllocated() && !d.worldTargetBound(),
              "Disconnect closes every upscaler target and restores target routing");
          require(d.activeMode() == UpscalingPolicy.Mode.NONE && UpscalingConfig.activeScale() == 1,
              "Disconnected menus report native rendering");
          require(mc.getMainRenderTarget() == nativeTarget,
              "Native main target survives world disconnect");
          report("UPSCALING_DISCONNECT_PASS resourcesReleased=true targetRestored=true nativeMenus=true");
          restore(mc);
          report("UPSCALING_FRAMES_PASS fsrQuality=true spatial63=true dlssNativeFallback=true off=true"
              + " oddResize=801x601 shaderReload=true nativeHud=true readbacks=4 disconnect=true"
              + " ownerGpuPerformance=false dlssHardwareExecution=false");
          next(Phase.DONE);
          mc.stop();
        }
        case DONE -> {}
      }
    } catch (Throwable error) {
      try { report("UPSCALING_FRAMES_FAIL phase=" + phase + " error=" + error); }
      catch (IOException outputError) { error.addSuppressed(outputError); }
      try { restore(mc); }
      catch (Throwable restoreError) { error.addSuppressed(restoreError); }
      phase = Phase.DONE;
      throw new RuntimeException("Native upscaling frame fixture failed", error);
    }
  }
}
