package com.shnewbs.deepprimal.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.shnewbs.deepprimal.core.DistancePolicy;
import com.shnewbs.deepprimal.core.VerticalRange;
import com.shnewbs.deepprimal.mixin.client.OptionsAccessor;
import com.shnewbs.deepprimal.performance.DistanceRuntime;
import com.shnewbs.deepprimal.settings.ClientPerformanceConfig;
import java.util.OptionalInt;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/** Edits are staged until Apply; keyboard entry and sliders share the same validation. */
public final class DistanceScreen extends Screen {
  private final Screen parent;
  private EditBox renderInput, simulationInput, verticalInput, farInput;
  private DistanceSlider renderSlider, simulationSlider, verticalSlider, farSlider;
  private Button apply;
  private boolean fastRelocation;
  private int left, top, panelWidth;

  public DistanceScreen(Screen parent) {
    super(Component.translatable("deepprimal.distance.title"));
    this.parent = parent;
  }

  @Override
  protected void init() {
    if (!com.shnewbs.deepprimal.settings.RendererOwnership.owned()) {
      addDashboard(width / 2 - 100, height / 2 + 4, 200);
      addRenderableWidget(
          Button.builder(
                  Component.literal("Performance…"),
                  b ->
                      minecraft.setScreen(
                          new com.shnewbs.deepprimal.settings.PerformanceSettingsScreen(this)))
              .bounds(width / 2 - 100, height / 2 + 28, 200, 20)
              .build());
      addRenderableWidget(
          Button.builder(CommonComponents.GUI_BACK, b -> onClose())
              .bounds(width / 2 - 100, height / 2 + 52, 200, 20)
              .build());
      return;
    }
    String oldRender = renderInput == null ? null : renderInput.getValue(),
        oldSimulation = simulationInput == null ? null : simulationInput.getValue();
    String oldVertical = verticalInput == null ? null : verticalInput.getValue(),
        oldFar = farInput == null ? null : farInput.getValue();
    panelWidth = Math.min(350, width - 32);
    left = (width - panelWidth) / 2;
    top = Math.max(28, (height - 210) / 2);
    if (oldRender == null) fastRelocation = ClientPerformanceConfig.FAST_VIEW_RELOCATION.get();
    renderInput =
        addRenderableWidget(
            new EditBox(
                font,
                left + panelWidth - 58,
                top,
                58,
                20,
                Component.translatable("options.renderDistance")));
    simulationInput =
        addRenderableWidget(
            new EditBox(
                font,
                left + panelWidth - 58,
                top + 24,
                58,
                20,
                Component.translatable("options.simulationDistance")));
    verticalInput =
        addRenderableWidget(
            new EditBox(
                font,
                left + panelWidth - 58,
                top + 48,
                58,
                20,
                Component.translatable("deepprimal.distance.vertical")));
    farInput =
        addRenderableWidget(
            new EditBox(
                font,
                left + panelWidth - 58,
                top + 72,
                58,
                20,
                Component.translatable("deepprimal.distance.far")));
    for (EditBox input : new EditBox[] {renderInput, simulationInput, verticalInput, farInput}) {
      input.setMaxLength(3);
      input.setFilter(text -> text.chars().allMatch(c -> c >= '0' && c <= '9'));
    }
    renderInput.setValue(
        oldRender == null ? Integer.toString(minecraft.options.renderDistance().get()) : oldRender);
    simulationInput.setValue(
        oldSimulation == null
            ? Integer.toString(minecraft.options.simulationDistance().get())
            : oldSimulation);
    verticalInput.setValue(
        oldVertical == null
            ? Integer.toString(ClientPerformanceConfig.VERTICAL_RENDER_DISTANCE.get())
            : oldVertical);
    farInput.setValue(
        oldFar == null
            ? Integer.toString(ClientPerformanceConfig.FAR_RENDER_DISTANCE.get())
            : oldFar);
    renderSlider =
        addRenderableWidget(
            new DistanceSlider(
                left,
                top,
                panelWidth - 68,
                "options.renderDistance",
                2,
                DistancePolicy.MAX_RENDER,
                minecraft.options.renderDistance().get(),
                renderInput));
    simulationSlider =
        addRenderableWidget(
            new DistanceSlider(
                left,
                top + 24,
                panelWidth - 68,
                "options.simulationDistance",
                2,
                DistancePolicy.MAX_SIMULATION,
                minecraft.options.simulationDistance().get(),
                simulationInput));
    verticalSlider =
        addRenderableWidget(
            new DistanceSlider(
                left,
                top + 48,
                panelWidth - 68,
                "deepprimal.distance.vertical",
                0,
                VerticalRange.MAX_RADIUS,
                ClientPerformanceConfig.VERTICAL_RENDER_DISTANCE.get(),
                verticalInput));
    farSlider =
        addRenderableWidget(
            new DistanceSlider(
                left,
                top + 72,
                panelWidth - 68,
                "deepprimal.distance.far",
                0,
                com.shnewbs.deepprimal.core.FarTerrain.MAX_DISTANCE,
                ClientPerformanceConfig.FAR_RENDER_DISTANCE.get(),
                farInput));
    farInput.setTooltip(Tooltip.create(Component.translatable("deepprimal.distance.far_help")));
    farSlider.setTooltip(Tooltip.create(Component.translatable("deepprimal.distance.far_help")));
    var verticalTip = Tooltip.create(Component.translatable("deepprimal.distance.vertical_help"));
    verticalSlider.setTooltip(verticalTip);
    verticalInput.setTooltip(verticalTip);
    renderInput.setResponder(text -> refresh());
    simulationInput.setResponder(text -> refresh());
    verticalInput.setResponder(text -> refresh());
    farInput.setResponder(text -> refresh());
    addRenderableWidget(
        Button.builder(
                relocationLabel(),
                button -> {
                  fastRelocation = !fastRelocation;
                  button.setMessage(relocationLabel());
                })
            .bounds(left, top + 136, panelWidth / 2 - 5, 20)
            .build());
    addRenderableWidget(
        Button.builder(
                Component.literal("Water & snow…"),
                b ->
                    minecraft.setScreen(
                        new com.shnewbs.deepprimal.settings.WaterVisualSettingsScreen(this)))
            .bounds(left + panelWidth / 2 + 5, top + 136, panelWidth / 2 - 5, 20)
            .build());
    apply =
        addRenderableWidget(
            Button.builder(
                    Component.translatable("deepprimal.distance.apply"), button -> applyChanges())
                .bounds(left, top + 182, panelWidth / 2 - 5, 20)
                .build());
    addRenderableWidget(
        Button.builder(CommonComponents.GUI_CANCEL, button -> onClose())
            .bounds(left + panelWidth / 2 + 5, top + 182, panelWidth / 2 - 5, 20)
            .build());
    addRenderableWidget(
        Button.builder(
                Component.literal("Performance…"),
                b ->
                    minecraft.setScreen(
                        new com.shnewbs.deepprimal.settings.PerformanceSettingsScreen(this)))
            .bounds(left, top + 158, panelWidth / 2 - 5, 20)
            .build());
    addDashboard(left + panelWidth / 2 + 5, top + 158, panelWidth / 2 - 5);
    refresh();
  }

  private void addDashboard(int x, int y, int buttonWidth) {
    Button button =
        addRenderableWidget(
            Button.builder(
                    Component.literal(
                        buttonWidth < 200 ? "Local dashboard" : "Local performance dashboard"),
                    b ->
                        net.minecraft.util.Util.getPlatform()
                            .openUri(
                                java.net.URI.create(
                                    com.shnewbs.deepprimal.diagnostics.LocalDashboard.url())))
                .bounds(x, y, buttonWidth, 20)
                .build());
    button.active = com.shnewbs.deepprimal.diagnostics.LocalDashboard.running();
  }

  private Component relocationLabel() {
    return Component.translatable(
        "deepprimal.distance.fast_relocation",
        fastRelocation ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
  }

  private OptionalInt renderValue() {
    return DistancePolicy.parse(renderInput.getValue(), DistancePolicy.MAX_RENDER);
  }

  private OptionalInt simulationValue() {
    return DistancePolicy.parse(simulationInput.getValue(), DistancePolicy.MAX_SIMULATION);
  }

  private OptionalInt verticalValue() {
    return DistancePolicy.parse(verticalInput.getValue(), 0, VerticalRange.MAX_RADIUS);
  }

  private OptionalInt farValue() {
    return DistancePolicy.parse(
        farInput.getValue(), 0, com.shnewbs.deepprimal.core.FarTerrain.MAX_DISTANCE);
  }

  private void refresh() {
    var render = renderValue();
    var simulation = simulationValue();
    var vertical = verticalValue();
    apply.active =
        render.isPresent()
            && simulation.isPresent()
            && vertical.isPresent()
            && farValue().isPresent();
    farInput.setTextColor(farValue().isPresent() ? 0xffeeeeee : 0xffff7777);
    farValue().ifPresent(farSlider::setDistance);
    renderInput.setTextColor(render.isPresent() ? 0xffeeeeee : 0xffff7777);
    simulationInput.setTextColor(simulation.isPresent() ? 0xffeeeeee : 0xffff7777);
    verticalInput.setTextColor(vertical.isPresent() ? 0xffeeeeee : 0xffff7777);
    render.ifPresent(renderSlider::setDistance);
    simulation.ifPresent(simulationSlider::setDistance);
    vertical.ifPresent(verticalSlider::setDistance);
  }

  private void applyChanges() {
    var render = renderValue();
    var simulation = simulationValue();
    var vertical = verticalValue();
    if (render.isEmpty() || simulation.isEmpty() || vertical.isEmpty() || farValue().isEmpty())
      return;
    boolean changedVertical =
        vertical.getAsInt() != ClientPerformanceConfig.VERTICAL_RENDER_DISTANCE.get();
    DistanceRuntime.clientRequest(render.getAsInt());
    minecraft.options.renderDistance().set(render.getAsInt());
    minecraft.options.simulationDistance().set(simulation.getAsInt());
    ClientPerformanceConfig.FAST_VIEW_RELOCATION.set(fastRelocation);
    ClientPerformanceConfig.VERTICAL_RENDER_DISTANCE.set(vertical.getAsInt());
    ClientPerformanceConfig.FAR_RENDER_DISTANCE.set(farValue().getAsInt());
    ClientPerformanceConfig.SPEC.save();
    minecraft.options.save();
    if (changedVertical && minecraft.level != null) minecraft.levelRenderer.allChanged();
    onClose();
  }

  @Override
  public void onClose() {
    minecraft.setScreen(parent);
  }

  @Override
  public void extractRenderState(
      GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    if (!com.shnewbs.deepprimal.settings.RendererOwnership.owned()) {
      graphics.centeredText(font, title, width / 2, height / 2 - 42, 0xffffffff);
      graphics.textWithWordWrap(
          font,
          Component.literal(
              "Renderer: "
                  + com.shnewbs.deepprimal.settings.RendererOwnership.owner()
                  + ". Deep Primal rendering yields automatically. Use that renderer’s controls."),
          width / 2 - 160,
          height / 2 - 15,
          320,
          0xffdddddd);
      return;
    }
    graphics.centeredText(font, title, width / 2, top - 24, 0xffffffff);
    Component line;
    var render = renderValue();
    if (render.isEmpty()
        || simulationValue().isEmpty()
        || verticalValue().isEmpty()
        || farValue().isEmpty()) {
      line =
          Component.translatable(
              "deepprimal.distance.invalid",
              DistancePolicy.MAX_RENDER,
              DistancePolicy.MAX_SIMULATION);
    } else {
      int serverRange = ((OptionsAccessor) minecraft.options).deepprimal$serverRenderDistance();
      int memoryLimit =
          com.shnewbs.deepprimal.performance.MemorySafety.limit(
              DistancePolicy.MAX_RENDER,
              minecraft.level == null ? 64 : minecraft.level.getSectionsCount());
      if (render.getAsInt() > memoryLimit || simulationValue().getAsInt() > memoryLimit)
        line =
            Component.literal(
                "Memory guard: render/simulation ≤"
                    + memoryLimit
                    + " chunks. Horizon is separate.");
      else if (minecraft.getSingleplayerServer() != null
          && render.getAsInt() > DistanceRuntime.trackingCapacity())
        line =
            Component.translatable(
                "deepprimal.distance.reopen", DistanceRuntime.trackingCapacity());
      else if (minecraft.level != null && serverRange > 0)
        line = Component.translatable("deepprimal.distance.server", serverRange);
      else
        line =
            Component.translatable(
                "deepprimal.distance.ranges",
                DistancePolicy.MAX_RENDER,
                DistancePolicy.MAX_SIMULATION);
    }
    graphics.textWithWordWrap(font, line, left, top + 98, panelWidth, 0xffdddddd);
    graphics.text(
        font,
        Component.translatable(
            "deepprimal.distance.backend", RenderSystem.getDevice().getBackendName()),
        left,
        top + 120,
        0xffaaaaaa);
  }

  private static final class DistanceSlider extends AbstractSliderButton {
    private final String label;
    private final int minimum;
    private final int maximum;
    private final EditBox input;

    DistanceSlider(
        int x,
        int y,
        int width,
        String label,
        int minimum,
        int maximum,
        int initial,
        EditBox input) {
      super(
          x,
          y,
          width,
          20,
          Component.empty(),
          (initial - (double) minimum) / (maximum - (double) minimum));
      this.label = label;
      this.minimum = minimum;
      this.maximum = maximum;
      this.input = input;
      updateMessage();
    }

    int distance() {
      return (int) Math.round(minimum + value * (maximum - minimum));
    }

    void setDistance(int distance) {
      value = (distance - (double) minimum) / (maximum - (double) minimum);
      updateMessage();
    }

    @Override
    protected void updateMessage() {
      setMessage(
          label.equals("deepprimal.distance.vertical")
              ? Component.translatable(
                  "deepprimal.distance.vertical_value", distance(), distance() * 16)
              : Component.translatable(label).append(": " + distance()));
      if (label.equals("deepprimal.distance.vertical") && distance() == 0)
        setMessage(Component.translatable("deepprimal.distance.vertical_full"));
    }

    @Override
    protected void applyValue() {
      input.setValue(Integer.toString(distance()));
    }
  }
}
