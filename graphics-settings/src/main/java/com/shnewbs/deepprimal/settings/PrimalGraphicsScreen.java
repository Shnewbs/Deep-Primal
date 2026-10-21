package com.shnewbs.deepprimal.settings;

import java.util.*;
import java.util.function.*;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ParticleStatus;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Original tabbed graphics UI. One staged model survives tab changes/resize; Apply is explicit. */
public final class PrimalGraphicsScreen extends Screen {
  private record Control(
      int tab,
      String key,
      String label,
      IntSupplier get,
      IntConsumer set,
      int min,
      int max,
      String help,
      String[] choices) {}

  private record Display(Control control, int y) {}

  private static final String[] TABS = {
    "General", "Quality", "Performance", "LOD", "Water", "Graphics API"
  };
  private final Screen parent;
  private final LinkedHashMap<String, Control> controls = new LinkedHashMap<>();
  private final Map<String, String> values = new LinkedHashMap<>();
  private final List<Display> visible = new ArrayList<>();
  private boolean loaded, updating;
  private int tab, scroll, left, top, panelWidth, contentX, contentWidth, rowHeight, shown;
  private Button apply;
  private String message = "Changes are staged until Apply.";

  public PrimalGraphicsScreen(Screen parent) {
    super(Component.literal("Deep Primal Graphics"));
    this.parent = parent;
  }

  private void add(
      int page,
      String key,
      String label,
      IntSupplier get,
      IntConsumer set,
      int min,
      int max,
      String help,
      String... choices) {
    controls.put(key, new Control(page, key, label, get, set, min, max, help, choices));
  }

  private void integer(
      int page,
      String key,
      String label,
      ModConfigSpec.IntValue value,
      int min,
      int max,
      String help) {
    add(page, key, label, value::get, value::set, min, max, help);
  }

  private void bool(
      int page, String key, String label, ModConfigSpec.BooleanValue value, String help) {
    add(
        page,
        key,
        label,
        () -> value.get() ? 1 : 0,
        v -> value.set(v == 1),
        0,
        1,
        help,
        "OFF",
        "ON");
  }

  private void define() {
    controls.clear();
    add(
        0,
        "render",
        "Render distance",
        () -> minecraft.options.renderDistance().get(),
        v -> minecraft.options.renderDistance().set(v),
        2,
        125,
        "Requested full-detail radius in chunks. Server delivery and memory guards can lower the"
            + " effective value.");
    add(
        0,
        "simulation",
        "Simulation distance",
        () -> minecraft.options.simulationDistance().get(),
        v -> minecraft.options.simulationDistance().set(v),
        2,
        32,
        "Server-side gameplay radius. LOD does not simulate distant chunks.");
    integer(
        0,
        "vertical",
        "Vertical distance",
        ClientPerformanceConfig.VERTICAL_RENDER_DISTANCE,
        0,
        96,
        "Radius above/below the camera in 16-block sections. 0 keeps the whole height. Does not"
            + " shrink server storage.");
    add(
        0,
        "fov",
        "Field of view",
        () -> minecraft.options.fov().get(),
        v -> minecraft.options.fov().set(v),
        30,
        110,
        "Camera field of view. LOD detail selection accounts for FOV and viewport size.");
    add(
        0,
        "brightness",
        "Brightness (%)",
        () -> (int) Math.round(minecraft.options.gamma().get() * 100),
        v -> minecraft.options.gamma().set(v / 100.),
        0,
        100,
        "Native brightness; LOD uses the same day/night lightmap.");
    add(
        0,
        "fpsLimit",
        "Frame rate limit",
        () -> minecraft.options.framerateLimit().get(),
        v -> minecraft.options.framerateLimit().set(v),
        10,
        260,
        "Maximum frame rate. 260 selects Minecraft's unlimited option.");
    add(
        0,
        "vsync",
        "VSync",
        () -> minecraft.options.enableVsync().get() ? 1 : 0,
        v -> minecraft.options.enableVsync().set(v == 1),
        0,
        1,
        "Synchronize frames with the display refresh rate.",
        "OFF",
        "ON");
    integer(
        0,
        "target",
        "Target FPS",
        ClientPerformanceConfig.TARGET_FPS,
        15,
        240,
        "Target used to pace optional background work. Does not promise or force this frame rate.");
    add(
        1,
        "clouds",
        "Cloud quality",
        () -> minecraft.options.cloudStatus().get().ordinal(),
        v -> minecraft.options.cloudStatus().set(CloudStatus.values()[v]),
        0,
        CloudStatus.values().length - 1,
        "Native cloud appearance.",
        Arrays.stream(CloudStatus.values()).map(Enum::name).toArray(String[]::new));
    add(
        1,
        "particles",
        "Particle quality",
        () -> minecraft.options.particles().get().ordinal(),
        v -> minecraft.options.particles().set(ParticleStatus.values()[v]),
        0,
        ParticleStatus.values().length - 1,
        "Particle admission affects visual density; gameplay stays unchanged.",
        Arrays.stream(ParticleStatus.values()).map(Enum::name).toArray(String[]::new));
    add(
        1,
        "shadows",
        "Entity shadows",
        () -> minecraft.options.entityShadows().get() ? 1 : 0,
        v -> minecraft.options.entityShadows().set(v == 1),
        0,
        1,
        "Native entity shadows.",
        "OFF",
        "ON");
    integer(
        1,
        "particleSpawns",
        "Particles per tick",
        ClientPerformanceConfig.PARTICLE_SPAWNS,
        0,
        8192,
        "Optional particle admission cap. 0 retains native behavior.");
    integer(
        1,
        "entities",
        "Entity range",
        ClientPerformanceConfig.ENTITY_LIMIT,
        0,
        512,
        "Visual range in blocks. 0 uses native limits; players, glowing and leashed entities are"
            + " exempt.");
    integer(
        1,
        "blockEntities",
        "Block entity range",
        ClientPerformanceConfig.BLOCK_ENTITY_LIMIT,
        0,
        256,
        "Visual range in blocks. 0 uses native limits.");
    integer(
        1,
        "particleRange",
        "Particle range",
        ClientPerformanceConfig.PARTICLE_LIMIT,
        0,
        256,
        "0 uses native distance limits.");
    bool(
        2,
        "adaptive",
        "Adaptive work",
        ClientPerformanceConfig.ADAPTIVE_WORK,
        "Back off optional work under frame/memory pressure.");
    bool(
        2,
        "pace",
        "Pace chunk rebuilds",
        ClientPerformanceConfig.PACE_REBUILDS,
        "Limit ordinary synchronous rebuilds; player edits remain immediate.");
    integer(
        2,
        "syncMicros",
        "Sync build CPU (µs)",
        ClientPerformanceConfig.SYNC_REBUILD_MICROS,
        250,
        8000,
        "CPU budget for ordinary synchronous section builds. Player edits stay immediate.");
    integer(
        2,
        "uploadMicros",
        "Upload CPU (µs)",
        ClientPerformanceConfig.UPLOAD_MICROS,
        100,
        4000,
        "Stop starting additional uploads after this CPU budget. One allocation cannot be"
            + " preempted.");
    integer(
        2,
        "minimized",
        "Minimized FPS",
        ClientPerformanceConfig.MINIMIZED_FPS,
        0,
        60,
        "0 uses native behavior. Does not change server simulation.");
    integer(
        2,
        "sync",
        "Sync sections/frame",
        ClientPerformanceConfig.SYNC_REBUILDS,
        1,
        16,
        "Maximum ordinary synchronous section rebuilds per frame.");
    integer(
        2,
        "uploadKiB",
        "Upload KiB/frame",
        ClientPerformanceConfig.UPLOAD_KIB,
        256,
        4096,
        "Shared LOD upload payload budget. Work is checked between allocations.");
    integer(
        2,
        "unfocused",
        "Unfocused FPS",
        ClientPerformanceConfig.UNFOCUSED_FPS,
        0,
        120,
        "Background frame cap. 0 uses native behavior. Yields to Dynamic FPS.");
    bool(
        2,
        "pause",
        "Pause background work",
        ClientPerformanceConfig.PAUSE_BACKGROUND,
        "Pause optional terrain capture/reduction/uploads while unfocused.");
    add(
        2,
        "dashboard",
        "Performance dashboard",
        () -> 0,
        v -> GraphicsActions.dashboard.run(),
        0,
        0,
        "Open the existing read-only localhost dashboard. It includes LOD capture, memory and"
            + " update counters.",
        "Open");
    bool(
        3,
        "lod",
        "Chunk LOD",
        LodConfig.ENABLED,
        "Render actual previously received terrain, trees and player builds beyond nearby chunks."
            + " No extra full chunks are requested.");
    integer(
        3,
        "lodRadius",
        "LOD radius (chunks)",
        LodConfig.RADIUS,
        16,
        512,
        "Maximum viewing radius for explored/cached terrain. Unexplored terrain has no invented"
            + " heightfield substitute.");
    integer(
        3,
        "lodError",
        "Detail size (pixels)",
        LodConfig.ERROR,
        1,
        12,
        "Projected cell width before using a coarser level. Lower retains more detail. Vertical air"
            + " gaps remain represented.");
    integer(
        3,
        "lodMemory",
        "LOD GPU memory (MiB)",
        LodConfig.MEMORY,
        4,
        256,
        "Resident LOD vertex/index payload limit. Whole-game RAM and driver memory are separate.");
    integer(
        3,
        "lodDisk",
        "Disk cache (MiB)",
        LodConfig.DISK,
        16,
        4096,
        "Compressed snapshot budget per world and dimension; oldest cached pages are pruned.");
    bool(
        3,
        "persist",
        "Persistent cache",
        LodConfig.PERSIST,
        "Store actual chunk snapshots using the server's persistent world identity. Existing worlds"
            + " do not share caches.");
    integer(
        3,
        "captures",
        "Capture interval (ticks)",
        LodConfig.CAPTURES,
        1,
        8,
        "Ticks between packed immutable chunk copies. Lower captures more often. One job at a time;"
            + " workers never read live worlds.");
    integer(
        3,
        "uploads",
        "LOD uploads/frame",
        LodConfig.UPLOADS,
        1,
        4,
        "Mesh parts uploaded per frame. An old complete mesh remains visible while a new one is"
            + " uploaded.");
    bool(
        4,
        "smooth",
        "Smooth water mesh",
        WaterVisualConfig.SMOOTH,
        "Shared-edge curved surfaces for Deep Primal's volume-based fluid.");
    bool(
        4,
        "waterColor",
        "Water colors",
        WaterVisualConfig.COLORS,
        "Elevation/biome-aware presentation where supported.");
    bool(
        4,
        "waterFog",
        "Underwater clarity",
        WaterVisualConfig.FOG,
        "Water visibility and tint controls.");
    bool(
        4,
        "wakes",
        "Wake particles",
        WaterVisualConfig.WAKES,
        "Existing wake effects; coastal breaking surf remains unfinished.");
    bool(
        4,
        "oceanWaves",
        "Ocean swell",
        WaterVisualConfig.WAVES,
        "Existing offshore swell overlay. Breaking shoreline surf remains unfinished.");
    bool(
        4,
        "cascades",
        "Cascade spray",
        WaterVisualConfig.CASCADES,
        "Existing river spray effects.");
    bool(
        4,
        "snow",
        "Snow visuals",
        WaterVisualConfig.SNOW,
        "Existing foliage/falling-snow effects.");
    integer(
        4,
        "waveRadius",
        "Swell radius (blocks)",
        WaterVisualConfig.WAVE_RADIUS,
        8,
        64,
        "Loaded-ocean surface overlay radius.");
    integer(
        4,
        "waterParticles",
        "Water particle budget",
        WaterVisualConfig.PARTICLES,
        0,
        128,
        "Shared optional water/snow particle budget per client tick.");
    add(
        5,
        "backend",
        "Graphics API",
        () -> RendererPreferences.savedBackend() == RendererPreferences.Backend.Vulkan ? 1 : 0,
        v -> {
          try {
            RendererPreferences.save(
                v == 1 ? RendererPreferences.Backend.Vulkan : RendererPreferences.Backend.OpenGL);
          } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
          }
        },
        0,
        1,
        "Requires a game restart. Vulkan must be installed; unsupported initialization recovers"
            + " with OpenGL.",
        "OpenGL",
        "Vulkan");
    bool(
        5,
        "batch",
        "Batch terrain draws",
        ClientPerformanceConfig.BATCHED_DRAWS,
        "Shared pipeline submission for terrain geometry. The graphics API remains a"
            + " separate module.");
  }

  private void load() {
    define();
    values.clear();
    controls.forEach((k, c) -> values.put(k, Integer.toString(c.get.getAsInt())));
    loaded = true;
  }

  private int number(Control c) {
    try {
      return Integer.parseInt(values.get(c.key));
    } catch (RuntimeException e) {
      return c.min;
    }
  }

  private boolean valid() {
    for (Control c : controls.values()) {
      try {
        int v = Integer.parseInt(values.get(c.key));
        if (v < c.min || v > c.max) return false;
      } catch (RuntimeException e) {
        return false;
      }
    }
    return true;
  }

  @Override
  protected void init() {
    if (!loaded) load();
    visible.clear();
    panelWidth = Math.min(900, width - 20);
    left = (width - panelWidth) / 2;
    top = 32;
    int rail = 94;
    contentX = left + rail + 12;
    contentWidth = panelWidth - rail - 24;
    boolean wide = contentWidth > 490;
    if (wide) contentWidth -= 190;
    rowHeight = 26;
    shown = Math.max(1, (height - 106) / rowHeight);
    for (int i = 0; i < TABS.length; i++) {
      final int t = i;
      var b =
          addRenderableWidget(
              Button.builder(
                      Component.literal((tab == i ? "› " : "") + TABS[i]),
                      v -> {
                        tab = t;
                        scroll = 0;
                        message = "Changes are staged until Apply.";
                        rebuildWidgets();
                      })
                  .bounds(left + 6, top + 14 + i * 25, rail - 8, 22)
                  .build());
      b.setTooltip(Tooltip.create(Component.literal("Open " + TABS[i] + " settings")));
    }
    var list = controls.values().stream().filter(c -> c.tab == tab).toList();
    scroll = Math.clamp(scroll, 0, Math.max(0, list.size() - shown));
    for (int i = 0; i < shown && scroll + i < list.size(); i++) {
      Control c = list.get(scroll + i);
      int y = top + 28 + i * rowHeight;
      visible.add(new Display(c, y));
      if (c.choices.length > 0) {
        var button =
            addRenderableWidget(
                Button.builder(
                        Component.literal(
                            c.choices[Math.clamp(number(c), 0, c.choices.length - 1)]),
                        b -> {
                          if (c.key.equals("dashboard")) {
                            GraphicsActions.dashboard.run();
                            return;
                          }
                          int v = (number(c) + 1) % c.choices.length;
                          if (c.key.equals("backend")
                              && !net.neoforged.fml.ModList.get().isLoaded("deepprimal_vulkan")) {
                            v = 0;
                            b.active = false;
                          }
                          values.put(c.key, Integer.toString(v));
                          b.setMessage(Component.literal(c.choices[v]));
                          refresh();
                        })
                    .bounds(contentX + contentWidth - 104, y, 100, 20)
                    .build());
        button.setTooltip(Tooltip.create(Component.literal(c.help)));
        if (!RendererOwnership.owned() && (c.tab == 3 || c.tab == 4)) button.active = false;
        if (c.key.equals("dashboard"))
          button.active = GraphicsActions.dashboardAvailable.getAsBoolean();
        if (c.key.equals("backend")
            && !net.neoforged.fml.ModList.get().isLoaded("deepprimal_vulkan")
            && number(c) == 0) button.active = false;
      } else {
        int fieldWidth = 48, sliderWidth = Math.max(44, Math.min(110, contentWidth / 3));
        EditBox field =
            addRenderableWidget(
                new EditBox(
                    font,
                    contentX + contentWidth - fieldWidth - 4,
                    y,
                    fieldWidth,
                    20,
                    Component.literal(c.label)));
        field.setMaxLength(5);
        field.setFilter(s -> s.chars().allMatch(Character::isDigit));
        field.setValue(values.get(c.key));
        field.setTooltip(
            Tooltip.create(Component.literal(c.help + " Range " + c.min + "–" + c.max + ".")));
        var slider =
            addRenderableWidget(
                new ValueSlider(
                    contentX + contentWidth - fieldWidth - sliderWidth - 10,
                    y,
                    sliderWidth,
                    c,
                    field));
        if (!RendererOwnership.owned() && (c.tab == 3 || c.tab == 4)) {
          field.active = false;
          slider.active = false;
        }
        field.setResponder(
            s -> {
              values.put(c.key, s);
              if (!updating) {
                updating = true;
                slider.sync(number(c));
                updating = false;
              }
              field.setTextColor(
                  s.matches("[0-9]+") && number(c) >= c.min && number(c) <= c.max
                      ? 0xffeeeeee
                      : 0xffff7777);
              refresh();
            });
      }
    }
    int bottom = height - 29, bw = Math.min(100, (panelWidth - 24) / 3);
    apply =
        addRenderableWidget(
            Button.builder(Component.literal("Apply"), b -> applyChanges())
                .bounds(left + panelWidth - bw * 3 - 16, bottom, bw, 20)
                .build());
    addRenderableWidget(
        Button.builder(
                Component.literal("Revert"),
                b -> {
                  load();
                  message = "Restored saved values.";
                  rebuildWidgets();
                })
            .bounds(left + panelWidth - bw * 2 - 10, bottom, bw, 20)
            .build());
    addRenderableWidget(
            Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(left + panelWidth - bw - 4, bottom, bw, 20)
                .build())
        .setTooltip(Tooltip.create(Component.literal("Close; unapplied changes are discarded.")));
    refresh();
  }

  private void refresh() {
    if (apply != null) {
      apply.active = valid();
      apply.setTooltip(Tooltip.create(Component.literal(message)));
    }
  }

  private void applyChanges() {
    if (!valid()) return;
    try {
      // Save backend first so an I/O error cannot silently claim all preferences were applied.
      int oldVertical = ClientPerformanceConfig.VERTICAL_RENDER_DISTANCE.get();
      boolean oldSmooth = WaterVisualConfig.SMOOTH.get();
      Control backend = controls.get("backend");
      backend.set.accept(number(backend));
      for (Control c : controls.values())
        if (c != backend && !c.key.equals("dashboard")) c.set.accept(number(c));
      ClientPerformanceConfig.SPEC.save();
      LodConfig.SPEC.save();
      WaterVisualConfig.SPEC.save();
      minecraft.options.save();
      if ((oldVertical != ClientPerformanceConfig.VERTICAL_RENDER_DISTANCE.get()
              || oldSmooth != WaterVisualConfig.SMOOTH.get())
          && minecraft.level != null) minecraft.levelRenderer.allChanged();
      load();
      message = "Saved. Graphics API changes require restart.";
      rebuildWidgets();
    } catch (RuntimeException e) {
      message = "Could not save settings: " + e.getClass().getSimpleName();
    }
  }

  public boolean mouseScrolled(double mx, double my, double dx, double dy) {
    int count = (int) controls.values().stream().filter(c -> c.tab == tab).count();
    int next = Math.clamp(scroll + (dy < 0 ? 1 : -1), 0, Math.max(0, count - shown));
    if (next != scroll) {
      scroll = next;
      rebuildWidgets();
      return true;
    }
    return super.mouseScrolled(mx, my, dx, dy);
  }

  @Override
  public void onClose() {
    minecraft.setScreen(parent);
  }

  @Override
  public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
    // Draw authored panels before widgets; labels and controls use native scaling and fonts.
    g.fill(left, top, panelWidth + left, height - 36, 0xed12191c);
    g.fill(left, top, left + 98, height - 36, 0xf020292c);
    g.fill(left, top, left + panelWidth, top + 2, 0xff70c49a);
    g.centeredText(font, title, width / 2, 12, 0xfff1f4f2);
    g.text(font, Component.literal(TABS[tab]), contentX, top + 10, 0xff8edbb4);
    super.extractRenderState(g, mx, my, delta);
    int count = (int) controls.values().stream().filter(c -> c.tab == tab).count();
    if (count > shown)
      g.text(
          font,
          Component.literal(
              (scroll + 1) + "–" + Math.min(scroll + shown, count) + " / " + count + " · scroll"),
          contentX + contentWidth - 100,
          top + 10,
          0xff93aa9c);
    String help = "Hover a setting for its purpose. Scroll to see more controls.";
    for (Display row : visible) {
      Control c = row.control;
      int labelWidth = Math.max(45, contentWidth - (c.choices.length > 0 ? 114 : 180));
      String label = font.plainSubstrByWidth(c.label, labelWidth);
      g.text(font, Component.literal(label), contentX, row.y + 6, 0xffe0e8e4);
      if (mx >= contentX && mx < contentX + contentWidth && my >= row.y && my < row.y + 23)
        help = c.help;
    }
    if (contentWidth > 0 && panelWidth - (contentX - left) - contentWidth > 180) {
      int hx = contentX + contentWidth + 16;
      g.text(font, Component.literal("About this setting"), hx, top + 12, 0xff8edbb4);
      g.textWithWordWrap(font, Component.literal(help), hx, top + 32, 162, 0xffb9c8c1);
    }
    String note =
        !RendererOwnership.owned()
            ? "Renderer delegated to " + RendererOwnership.owner()
            : tab == 3
                ? GraphicsActions.lodStatus.get()
                : tab == 5
                    ? "Active API: "
                        + com.mojang.blaze3d.systems.RenderSystem.getDevice().getBackendName()
                    : "Requested render: "
                        + values.get("render")
                        + " | effective: "
                        + minecraft.options.getEffectiveRenderDistance();
    g.text(
        font,
        Component.literal(font.plainSubstrByWidth(note, panelWidth - 12)),
        left + 6,
        height - 55,
        0xff93aa9c);
    if (panelWidth < 440)
      g.centeredText(
          font,
          Component.literal(font.plainSubstrByWidth(message, width - 20)),
          width / 2,
          23,
          0xffa9bbb0);
    else
      g.text(
          font,
          Component.literal(font.plainSubstrByWidth(message, Math.max(80, panelWidth - 330))),
          left + 6,
          height - 23,
          0xffa9bbb0);
  }

  private final class ValueSlider extends AbstractSliderButton {
    final Control control;
    final EditBox field;

    ValueSlider(int x, int y, int width, Control c, EditBox field) {
      super(x, y, width, 20, Component.empty(), (number(c) - c.min) / (double) (c.max - c.min));
      control = c;
      this.field = field;
      updateMessage();
      setTooltip(Tooltip.create(Component.literal(c.help)));
    }

    void sync(int n) {
      value = Math.clamp((n - control.min) / (double) (control.max - control.min), 0, 1);
      updateMessage();
    }

    protected void updateMessage() {
      if (control != null)
        setMessage(
            Component.literal(
                Integer.toString(
                    control.min + (int) Math.round(value * (control.max - control.min)))));
    }

    protected void applyValue() {
      if (control == null) return;
      int n = control.min + (int) Math.round(value * (control.max - control.min));
      values.put(control.key, Integer.toString(n));
      updating = true;
      field.setValue(Integer.toString(n));
      updating = false;
      refresh();
    }
  }
}
