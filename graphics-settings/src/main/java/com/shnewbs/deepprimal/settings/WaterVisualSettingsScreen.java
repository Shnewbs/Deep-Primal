package com.shnewbs.deepprimal.settings;

import com.shnewbs.deepprimal.core.DistancePolicy;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;

/** All edits are staged, including resize/reinitialization; Cancel never writes configuration. */
public final class WaterVisualSettingsScreen extends Screen {
  private static final String[] LABELS = {
    "Water colors",
    "Underwater clarity",
    "Ocean waves",
    "Wake particles",
    "Cascade spray",
    "Snow visuals"
  };
  private static final ModConfigSpec.BooleanValue[] VALUES = {
    WaterVisualConfig.COLORS,
    WaterVisualConfig.FOG,
    WaterVisualConfig.WAVES,
    WaterVisualConfig.WAKES,
    WaterVisualConfig.CASCADES,
    WaterVisualConfig.SNOW
  };
  private final Screen parent;
  private final boolean[] staged = new boolean[VALUES.length];
  private String particles, radius;
  private EditBox particlesInput, radiusInput;
  private Button apply;

  public WaterVisualSettingsScreen(Screen parent) {
    super(Component.literal("Water & snow visuals"));
    this.parent = parent;
    for (int i = 0; i < VALUES.length; i++) staged[i] = VALUES[i].get();
    particles = Integer.toString(WaterVisualConfig.PARTICLES.get());
    radius = Integer.toString(WaterVisualConfig.WAVE_RADIUS.get());
  }

  protected void init() {
    if (particlesInput != null) {
      particles = particlesInput.getValue();
      radius = radiusInput.getValue();
    }
    int left = width / 2 - 150, top = Math.max(28, height / 2 - 112);
    for (int i = 0; i < VALUES.length; i++) {
      final int index = i;
      addRenderableWidget(
          Button.builder(
                  label(i),
                  b -> {
                    staged[index] = !staged[index];
                    b.setMessage(label(index));
                  })
              .bounds(left + (i % 2) * 152, top + (i / 2) * 24, 148, 20)
              .build());
    }
    particlesInput =
        addRenderableWidget(
            new EditBox(font, left + 220, top + 80, 80, 20, Component.literal("Particle budget")));
    radiusInput =
        addRenderableWidget(
            new EditBox(font, left + 220, top + 104, 80, 20, Component.literal("Wave radius")));
    for (var field : new EditBox[] {particlesInput, radiusInput}) {
      field.setMaxLength(3);
      field.setFilter(t -> t.chars().allMatch(Character::isDigit));
    }
    particlesInput.setValue(particles);
    radiusInput.setValue(radius);
    apply =
        addRenderableWidget(
            Button.builder(Component.literal("Apply"), b -> save())
                .bounds(left, top + 150, 148, 20)
                .build());
    addRenderableWidget(
        Button.builder(Component.literal("Cancel"), b -> onClose())
            .bounds(left + 152, top + 150, 148, 20)
            .build());
    particlesInput.setResponder(t -> validate());
    radiusInput.setResponder(t -> validate());
    validate();
  }

  private Component label(int i) {
    return Component.literal(LABELS[i] + ": " + (staged[i] ? "ON" : "OFF"));
  }

  private void validate() {
    if (apply != null)
      apply.active =
          DistancePolicy.parse(particlesInput.getValue(), 0, 128).isPresent()
              && DistancePolicy.parse(radiusInput.getValue(), 8, 64).isPresent();
  }

  private void save() {
    validate();
    if (!apply.active) return;
    for (int i = 0; i < VALUES.length; i++) VALUES[i].set(staged[i]);
    WaterVisualConfig.PARTICLES.set(Integer.parseInt(particlesInput.getValue()));
    WaterVisualConfig.WAVE_RADIUS.set(Integer.parseInt(radiusInput.getValue()));
    WaterVisualConfig.SPEC.save();
    if (minecraft.level != null) {
      minecraft.level.clearTintCaches();
      minecraft.levelRenderer.allChanged();
    }
    onClose();
  }

  public void onClose() {
    minecraft.setScreen(parent);
  }

  public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float dt) {
    super.extractRenderState(g, mx, my, dt);
    int left = width / 2 - 150, top = Math.max(28, height / 2 - 112);
    g.centeredText(font, title, width / 2, top - 20, 0xffffffff);
    g.text(font, Component.literal("Particle budget (0–128)"), left, top + 86, 0xffdddddd);
    g.text(font, Component.literal("Wave radius (8–64 blocks)"), left, top + 110, 0xffdddddd);
    g.textWithWordWrap(
        font,
        Component.literal("Visuals only. Server water and snow rules use world settings."),
        left,
        top + 128,
        300,
        0xffaaaaaa);
  }
}
