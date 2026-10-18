package com.shnewbs.deepprimal.client;

import com.shnewbs.deepprimal.DistanceRuntime;
import com.mojang.blaze3d.systems.RenderSystem;
import com.shnewbs.deepprimal.core.DistancePolicy;
import com.shnewbs.deepprimal.core.VerticalRange;
import com.shnewbs.deepprimal.mixin.client.OptionsAccessor;
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
    private EditBox renderInput, simulationInput, verticalInput;
    private DistanceSlider renderSlider, simulationSlider, verticalSlider;
    private Button apply;
    private boolean fastRelocation;
    private int left, top, panelWidth;

    public DistanceScreen(Screen parent) {
        super(Component.translatable("deepprimal.distance.title"));
        this.parent = parent;
    }

    @Override protected void init() {
        panelWidth = Math.min(350, width - 32);
        left = (width - panelWidth) / 2; top = Math.max(28, (height - 210) / 2);
        fastRelocation = ClientPerformanceConfig.FAST_VIEW_RELOCATION.get();
        renderInput = addRenderableWidget(new EditBox(font, left + panelWidth - 58, top, 58, 20,
            Component.translatable("options.renderDistance")));
        simulationInput = addRenderableWidget(new EditBox(font, left + panelWidth - 58, top + 30, 58, 20,
            Component.translatable("options.simulationDistance")));
        verticalInput = addRenderableWidget(new EditBox(font, left + panelWidth - 58, top + 60, 58, 20,
            Component.translatable("deepprimal.distance.vertical")));
        for (EditBox input : new EditBox[]{renderInput, simulationInput, verticalInput}) {
            input.setMaxLength(3);
            input.setFilter(text -> text.chars().allMatch(c -> c >= '0' && c <= '9'));
        }
        renderInput.setValue(Integer.toString(minecraft.options.renderDistance().get()));
        simulationInput.setValue(Integer.toString(minecraft.options.simulationDistance().get()));
        verticalInput.setValue(Integer.toString(ClientPerformanceConfig.VERTICAL_RENDER_DISTANCE.get()));
        renderSlider = addRenderableWidget(new DistanceSlider(left, top, panelWidth - 68,
            "options.renderDistance", 2, DistancePolicy.MAX_RENDER, minecraft.options.renderDistance().get(), renderInput));
        simulationSlider = addRenderableWidget(new DistanceSlider(left, top + 30, panelWidth - 68,
            "options.simulationDistance", 2, DistancePolicy.MAX_SIMULATION, minecraft.options.simulationDistance().get(), simulationInput));
        verticalSlider = addRenderableWidget(new DistanceSlider(left, top + 60, panelWidth - 68,
            "deepprimal.distance.vertical", 0, VerticalRange.MAX_RADIUS, ClientPerformanceConfig.VERTICAL_RENDER_DISTANCE.get(), verticalInput));
        var verticalTip = Tooltip.create(Component.translatable("deepprimal.distance.vertical_help"));
        verticalSlider.setTooltip(verticalTip); verticalInput.setTooltip(verticalTip);
        renderInput.setResponder(text -> refresh()); simulationInput.setResponder(text -> refresh()); verticalInput.setResponder(text -> refresh());
        addRenderableWidget(Button.builder(relocationLabel(), button -> {
            fastRelocation = !fastRelocation; button.setMessage(relocationLabel());
        }).bounds(left, top + 138, panelWidth, 20).build());
        apply = addRenderableWidget(Button.builder(Component.translatable("deepprimal.distance.apply"), button -> applyChanges())
            .bounds(left, top + 182, panelWidth / 2 - 5, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> onClose())
            .bounds(left + panelWidth / 2 + 5, top + 182, panelWidth / 2 - 5, 20).build());
        refresh();
    }

    private Component relocationLabel() {
        return Component.translatable("deepprimal.distance.fast_relocation", fastRelocation ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
    }
    private OptionalInt renderValue() { return DistancePolicy.parse(renderInput.getValue(), DistancePolicy.MAX_RENDER); }
    private OptionalInt simulationValue() { return DistancePolicy.parse(simulationInput.getValue(), DistancePolicy.MAX_SIMULATION); }
    private OptionalInt verticalValue() {
        return DistancePolicy.parse(verticalInput.getValue(), 0, VerticalRange.MAX_RADIUS);
    }
    private void refresh() {
        var render = renderValue(); var simulation = simulationValue(); var vertical = verticalValue();
        apply.active = render.isPresent() && simulation.isPresent() && vertical.isPresent();
        renderInput.setTextColor(render.isPresent() ? 0xffeeeeee : 0xffff7777);
        simulationInput.setTextColor(simulation.isPresent() ? 0xffeeeeee : 0xffff7777);
        verticalInput.setTextColor(vertical.isPresent() ? 0xffeeeeee : 0xffff7777);
        render.ifPresent(renderSlider::setDistance); simulation.ifPresent(simulationSlider::setDistance); vertical.ifPresent(verticalSlider::setDistance);
    }
    private void applyChanges() {
        var render = renderValue(); var simulation = simulationValue(); var vertical = verticalValue();
        if (render.isEmpty() || simulation.isEmpty() || vertical.isEmpty()) return;
        boolean changedVertical = vertical.getAsInt() != ClientPerformanceConfig.VERTICAL_RENDER_DISTANCE.get();
        DistanceRuntime.clientRequest(render.getAsInt());
        minecraft.options.renderDistance().set(render.getAsInt());
        minecraft.options.simulationDistance().set(simulation.getAsInt());
        ClientPerformanceConfig.FAST_VIEW_RELOCATION.set(fastRelocation);
        ClientPerformanceConfig.VERTICAL_RENDER_DISTANCE.set(vertical.getAsInt());
        ClientPerformanceConfig.SPEC.save();
        minecraft.options.save();
        if (changedVertical && minecraft.level != null) minecraft.levelRenderer.allChanged();
        onClose();
    }
    @Override public void onClose() { minecraft.setScreen(parent); }

    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title, width / 2, top - 24, 0xffffffff);
        Component line;
        var render = renderValue();
        if (render.isEmpty() || simulationValue().isEmpty() || verticalValue().isEmpty()) {
            line = Component.translatable("deepprimal.distance.invalid", DistancePolicy.MAX_RENDER, DistancePolicy.MAX_SIMULATION);
        } else {
            int serverRange = ((OptionsAccessor)minecraft.options).deepprimal$serverRenderDistance();
            if (minecraft.getSingleplayerServer() != null && render.getAsInt() > DistanceRuntime.trackingCapacity())
                line = Component.translatable("deepprimal.distance.reopen", DistanceRuntime.trackingCapacity());
            else if (minecraft.level != null && serverRange > 0)
                line = Component.translatable("deepprimal.distance.server", serverRange);
            else line = Component.translatable("deepprimal.distance.ranges", DistancePolicy.MAX_RENDER, DistancePolicy.MAX_SIMULATION);
        }
        graphics.textWithWordWrap(font, line, left, top + 92, panelWidth, 0xffdddddd);
        graphics.text(font, Component.translatable("deepprimal.distance.backend", RenderSystem.getDevice().getBackendName()), left, top + 120, 0xffaaaaaa);
        if (minecraft.level != null && render.isPresent() && verticalValue().isPresent()) {
            int vertical = VerticalRange.count(minecraft.level.getSectionsCount(), verticalValue().getAsInt());
            long sections = DistancePolicy.sectionSlots(render.getAsInt(), vertical);
            graphics.text(font, Component.translatable("deepprimal.distance.sections", sections), left, top + 166, 0xffaaaaaa);
        }
    }

    private static final class DistanceSlider extends AbstractSliderButton {
        private final String label;
        private final int minimum;
        private final int maximum;
        private final EditBox input;
        DistanceSlider(int x, int y, int width, String label, int minimum, int maximum, int initial, EditBox input) {
            super(x, y, width, 20, Component.empty(), (initial - (double)minimum) / (maximum - (double)minimum));
            this.label = label; this.minimum = minimum; this.maximum = maximum; this.input = input; updateMessage();
        }
        int distance() { return (int)Math.round(minimum + value * (maximum - minimum)); }
        void setDistance(int distance) { value = (distance - (double)minimum) / (maximum - (double)minimum); updateMessage(); }
        @Override protected void updateMessage() {
            setMessage(minimum == 0 ? Component.translatable("deepprimal.distance.vertical_value", distance(), distance() * 16)
                : Component.translatable(label).append(": " + distance()));
            if (minimum == 0 && distance() == 0) setMessage(Component.translatable("deepprimal.distance.vertical_full"));
        }
        @Override protected void applyValue() { input.setValue(Integer.toString(distance())); }
    }
}
