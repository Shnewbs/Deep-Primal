package com.shnewbs.deepprimal.settings;

import com.shnewbs.deepprimal.core.DistancePolicy;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ParticleStatus;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

/** Staged, paged controls. Presets expose visual tradeoffs and never alter simulation. */
public final class PerformanceSettingsScreen extends Screen {
  private record Control(String label, IntValue value, int min, int max, String help) {}

  private static final List<Control> HORIZON =
      List.of(
          new Control(
              "Target FPS",
              ClientPerformanceConfig.TARGET_FPS,
              15,
              240,
              "Optional-work target; respects a lower configured FPS cap."),
          new Control(
              "Mesh memory (MiB)",
              ClientPerformanceConfig.HORIZON_MEMORY_MIB,
              1,
              256,
              "Cached vertices plus indices. Whole-game/driver memory is separate."),
          new Control(
              "Detail uploads / frame",
              ClientPerformanceConfig.UPLOAD_LEVELS,
              1,
              8,
              "A finished tile keeps its old mesh until all new levels are ready."),
          new Control(
              "Upload budget (KiB)",
              ClientPerformanceConfig.UPLOAD_KIB,
              256,
              4096,
              "Maximum cached-terrain payload started per frame; transition ring is separate."),
          new Control(
              "Upload CPU budget (us)",
              ClientPerformanceConfig.UPLOAD_MICROS,
              100,
              4000,
              "Checked between uploads. A single GPU allocation cannot be preempted."));
  private static final List<Control> SCENE =
      List.of(
          new Control(
              "Entity range (blocks)",
              ClientPerformanceConfig.ENTITY_LIMIT,
              0,
              512,
              "0 = native. Players, glowing and leashed entities are exempt; visual only."),
          new Control(
              "Block-entity range",
              ClientPerformanceConfig.BLOCK_ENTITY_LIMIT,
              0,
              256,
              "0 = native. Global renderers such as beacon beams are exempt."),
          new Control(
              "Particle range",
              ClientPerformanceConfig.PARTICLE_LIMIT,
              0,
              256,
              "0 = native. Optional distance limit at particle admission."),
          new Control(
              "Particles / tick",
              ClientPerformanceConfig.PARTICLE_SPAWNS,
              0,
              8192,
              "0 = native. Visual admission cap; gameplay ticks are unchanged."));
  private static final List<Control> BACKGROUND =
      List.of(
          new Control(
              "Unfocused FPS",
              ClientPerformanceConfig.UNFOCUSED_FPS,
              0,
              120,
              "0 = native. Never raises an existing lower cap. Yields to Dynamic FPS."),
          new Control(
              "Minimized FPS",
              ClientPerformanceConfig.MINIMIZED_FPS,
              0,
              60,
              "0 = native. Limits client rendering, never server simulation."));
  private static final List<Control> REBUILDS =
      List.of(
          new Control(
              "Sync sections / frame",
              ClientPerformanceConfig.SYNC_REBUILDS,
              1,
              16,
              "Ordinary nearby builds beyond this limit use native workers. Player edits stay"
                  + " immediate."),
          new Control(
              "Sync CPU budget (us)",
              ClientPerformanceConfig.SYNC_REBUILD_MICROS,
              250,
              8000,
              "Checked between native section builds. One build cannot be preempted."));
  private static final String[] PAGES = {
    "Terrain work", "Scene quality", "Background", "Profiles", "Chunk rebuilds", "Graphics API"
  };
  private final Screen parent;
  private final Map<IntValue, String> values = new LinkedHashMap<>();
  private final Map<Control, EditBox> fields = new LinkedHashMap<>();
  private boolean adaptive, pauseBackground, shadows, batched, staged, paceRebuilds;
  private CloudStatus clouds;
  private ParticleStatus particles;
  private int page, left, top, panelWidth;
  private Button apply;

  public PerformanceSettingsScreen(Screen parent) {
    super(Component.literal("Deep Primal performance"));
    this.parent = parent;
  }

  private static List<Control> allControls() {
    return java.util.stream.Stream.of(HORIZON, SCENE, BACKGROUND, REBUILDS)
        .flatMap(List::stream)
        .toList();
  }

  private List<Control> controls() {
    return page == 0
        ? HORIZON
        : page == 1 ? SCENE : page == 2 ? BACKGROUND : page == 4 ? REBUILDS : List.of();
  }

  @Override
  protected void init() {
    if (!staged) {
      for (Control c : allControls()) values.put(c.value, Integer.toString(c.value.get()));
      adaptive = ClientPerformanceConfig.ADAPTIVE_WORK.get();
      batched = ClientPerformanceConfig.BATCHED_DRAWS.get();
      pauseBackground = ClientPerformanceConfig.PAUSE_BACKGROUND.get();
      paceRebuilds = ClientPerformanceConfig.PACE_REBUILDS.get();
      shadows = minecraft.options.entityShadows().get();
      clouds = minecraft.options.cloudStatus().get();
      particles = minecraft.options.particles().get();
      staged = true;
    }
    fields.clear();
    panelWidth = Math.min(330, width - 24);
    left = (width - panelWidth) / 2;
    top = Math.max(24, (height - 208) / 2);
    addRenderableWidget(
        Button.builder(Component.literal("‹"), b -> changePage(-1))
            .bounds(left, top, 24, 20)
            .build());
    addRenderableWidget(
                Button.builder(Component.literal(PAGES[page]), b -> {})
                    .bounds(left + 28, top, panelWidth - 56, 20)
                    .build())
            .active =
        false;
    addRenderableWidget(
        Button.builder(Component.literal("›"), b -> changePage(1))
            .bounds(left + panelWidth - 24, top, 24, 20)
            .build());
    int row = 0;
    for (Control control : controls()) {
      EditBox field =
          addRenderableWidget(
              new EditBox(
                  font,
                  left + panelWidth - 78,
                  top + 28 + row++ * 22,
                  78,
                  20,
                  Component.literal(control.label)));
      field.setMaxLength(5);
      field.setFilter(s -> s.chars().allMatch(ch -> ch >= '0' && ch <= '9'));
      field.setValue(values.get(control.value));
      field.setTooltip(
          Tooltip.create(
              Component.literal(control.help + " Range " + control.min + "–" + control.max + ".")));
      field.setResponder(
          s -> {
            values.put(control.value, s);
            refresh();
          });
      fields.put(control, field);
      if ((page == 1 || page == 4) && !RendererOwnership.owned()
          || page == 2 && RendererOwnership.installed("dynamic_fps")) field.active = false;
    }
    if (page == 0) {
      toggleAt("Adaptive", adaptive, 140, left, panelWidth / 2 - 5, value -> adaptive = value);
      toggleAt(
          "Batch draws",
          batched,
          140,
          left + panelWidth / 2 + 5,
          panelWidth / 2 - 5,
          value -> batched = value);
    }
    if (page == 1) {
      addRenderableWidget(
          Button.builder(
                  cloudLabel(),
                  b -> {
                    var options = CloudStatus.values();
                    clouds = options[(clouds.ordinal() + 1) % options.length];
                    b.setMessage(cloudLabel());
                  })
              .bounds(left, top + 116, panelWidth, 20)
              .build());
      toggle("Entity shadows", shadows, 138, value -> shadows = value);
      addRenderableWidget(
          Button.builder(
                  particleLabel(),
                  b -> {
                    var options = ParticleStatus.values();
                    particles = options[(particles.ordinal() + 1) % options.length];
                    b.setMessage(particleLabel());
                  })
              .bounds(left, top + 160, panelWidth, 20)
              .build());
    }
    if (page == 4) toggle("Paced rebuilds", paceRebuilds, 82, value -> paceRebuilds = value);
    if (page == 2)
      toggle(
          "Pause background terrain work", pauseBackground, 82, value -> pauseBackground = value);
    if (page == 3)
      for (int i = 0; i < 3; i++) {
        final int preset = i;
        addRenderableWidget(
            Button.builder(
                    Component.literal(
                        new String[] {
                              "Balanced",
                              "Speed — reduced visual detail",
                              "Quality — higher resource use"
                            }
                            [i]),
                    b -> preset(preset))
                .bounds(left, top + 28 + i * 28, panelWidth, 20)
                .build());
      }
    if (page == 5)
      addRenderableWidget(
          Button.builder(
                  Component.literal("Open graphics settings"),
                  button -> minecraft.setScreen(new GraphicsSettingsScreen(this)))
              .bounds(left, top + 28, panelWidth, 20)
              .build());
    apply =
        addRenderableWidget(
            Button.builder(Component.literal("Apply"), b -> applyChanges())
                .bounds(left, top + 184, panelWidth / 2 - 5, 20)
                .build());
    addRenderableWidget(
        Button.builder(Component.literal("Cancel"), b -> onClose())
            .bounds(left + panelWidth / 2 + 5, top + 184, panelWidth / 2 - 5, 20)
            .build());
    refresh();
  }

  private void toggle(
      String name, boolean initial, int y, java.util.function.Consumer<Boolean> setter) {
    toggleAt(name, initial, y, left, panelWidth, setter);
  }

  private void toggleAt(
      String name,
      boolean initial,
      int y,
      int x,
      int buttonWidth,
      java.util.function.Consumer<Boolean> setter) {
    final boolean[] value = {initial};
    addRenderableWidget(
        Button.builder(
                Component.literal(name + ": " + (initial ? "ON" : "OFF")),
                b -> {
                  value[0] = !value[0];
                  setter.accept(value[0]);
                  b.setMessage(Component.literal(name + ": " + (value[0] ? "ON" : "OFF")));
                })
            .bounds(x, top + y, buttonWidth, 20)
            .build());
  }

  private Component cloudLabel() {
    return Component.literal("Cloud quality: " + clouds.name());
  }

  private Component particleLabel() {
    return Component.literal("Particle quality: " + particles.name());
  }

  private void changePage(int direction) {
    page = Math.floorMod(page + direction, PAGES.length);
    rebuildWidgets();
  }

  private boolean valid() {
    return allControls().stream()
        .allMatch(c -> DistancePolicy.parse(values.get(c.value), c.min, c.max).isPresent());
  }

  private void refresh() {
    if (apply == null) return;
    apply.active = valid();
    fields.forEach(
        (control, field) ->
            field.setTextColor(
                DistancePolicy.parse(field.getValue(), control.min, control.max).isPresent()
                    ? 0xffeeeeee
                    : 0xffff7777));
  }

  private void set(IntValue value, int number) {
    values.put(value, Integer.toString(number));
  }

  private void preset(int choice) {
    adaptive = pauseBackground = batched = paceRebuilds = true;
    set(ClientPerformanceConfig.SYNC_REBUILDS, choice == 1 ? 1 : choice == 2 ? 4 : 2);
    set(ClientPerformanceConfig.SYNC_REBUILD_MICROS, choice == 1 ? 500 : choice == 2 ? 3000 : 1500);
    set(ClientPerformanceConfig.TARGET_FPS, 60);
    set(ClientPerformanceConfig.HORIZON_MEMORY_MIB, choice == 1 ? 16 : choice == 2 ? 64 : 32);
    set(ClientPerformanceConfig.UPLOAD_LEVELS, choice == 1 ? 1 : choice == 2 ? 4 : 2);
    set(ClientPerformanceConfig.UPLOAD_KIB, choice == 1 ? 256 : choice == 2 ? 512 : 256);
    set(ClientPerformanceConfig.UPLOAD_MICROS, choice == 1 ? 500 : choice == 2 ? 2000 : 1000);
    set(ClientPerformanceConfig.ENTITY_LIMIT, choice == 1 ? 64 : 0);
    set(ClientPerformanceConfig.BLOCK_ENTITY_LIMIT, choice == 1 ? 32 : 0);
    set(ClientPerformanceConfig.PARTICLE_LIMIT, choice == 1 ? 32 : choice == 2 ? 0 : 64);
    set(ClientPerformanceConfig.PARTICLE_SPAWNS, choice == 1 ? 512 : choice == 2 ? 0 : 2048);
    set(ClientPerformanceConfig.UNFOCUSED_FPS, 20);
    set(ClientPerformanceConfig.MINIMIZED_FPS, 5);
    shadows = choice != 1;
    clouds = choice == 1 ? CloudStatus.OFF : CloudStatus.FANCY;
    particles = choice == 1 ? ParticleStatus.MINIMAL : ParticleStatus.ALL;
    page = 0;
    rebuildWidgets();
  }

  private void applyChanges() {
    if (!valid()) return;
    for (Control c : allControls()) c.value.set(Integer.parseInt(values.get(c.value)));
    ClientPerformanceConfig.BATCHED_DRAWS.set(batched);
    ClientPerformanceConfig.PACE_REBUILDS.set(paceRebuilds);
    ClientPerformanceConfig.ADAPTIVE_WORK.set(adaptive);
    ClientPerformanceConfig.PAUSE_BACKGROUND.set(pauseBackground);
    ClientPerformanceConfig.SPEC.save();
    minecraft.options.entityShadows().set(shadows);
    minecraft.options.cloudStatus().set(clouds);
    minecraft.options.particles().set(particles);
    minecraft.options.save();
    onClose();
  }

  @Override
  public void onClose() {
    minecraft.setScreen(parent);
  }

  @Override
  public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
    super.extractRenderState(g, mx, my, delta);
    g.centeredText(font, title, width / 2, top - 20, 0xffffffff);
    int row = 0;
    for (Control c : controls())
      g.text(font, Component.literal(c.label), left, top + 34 + row++ * 22, 0xffdddddd);
    String note =
        page == 0
            ? "Backend: " + com.mojang.blaze3d.systems.RenderSystem.getDevice().getBackendName()
            : page == 5
                ? "Graphics preferences are managed independently. Open graphics settings to choose"
                    + " the API for the next launch."
                : page == 1 && !RendererOwnership.owned()
                    ? "Distance controls delegated to " + RendererOwnership.owner()
                    : page == 2
                        ? (RendererOwnership.installed("dynamic_fps")
                                ? "FPS throttling delegated to Dynamic FPS. "
                                : "")
                            + "Simulation keeps its configured behavior."
                        : page == 3
                            ? "Profiles stage changes until Apply. Speed reduces visual effects and"
                                + " admission ranges. Terrain and simulation distances stay"
                                + " unchanged."
                            : page == 4
                                ? "Extra nearby rebuilds use native workers. Player edits stay"
                                    + " immediate; block simulation is unchanged."
                                : "0 uses native visual limits; no gameplay ticks are stopped.";
    if (page != 1)
      g.textWithWordWrap(
          font,
          Component.literal(note),
          left,
          top + (page == 0 ? 164 : 122),
          panelWidth,
          0xffaaaaaa);
  }
}
