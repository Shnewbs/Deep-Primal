package com.shnewbs.deepprimal.client;

import com.shnewbs.deepprimal.DistanceRuntime;
import com.mojang.blaze3d.systems.RenderSystem;
import com.shnewbs.deepprimal.core.DistancePolicy;
import com.shnewbs.deepprimal.mixin.client.OptionsAccessor;
import java.util.OptionalInt;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/** Edits are staged until Apply; keyboard entry and sliders share the same validation. */
public final class DistanceScreen extends Screen {
    private final Screen parent;
    private EditBox renderInput, simulationInput;
    private DistanceSlider renderSlider, simulationSlider;
    private Button apply;
    private boolean fastRelocation;
    private int left, top, panelWidth;

    public DistanceScreen(Screen parent) {
        super(Component.translatable("deepprimal.distance.title"));
        this.parent = parent;
    }

    @Override protected void init() {
        panelWidth = Math.min(310, width - 32);
        left = (width - panelWidth) / 2; top = Math.max(32, (height - 164) / 2);
        fastRelocation = ClientPerformanceConfig.FAST_VIEW_RELOCATION.get();
        renderInput = addRenderableWidget(new EditBox(font, left + panelWidth - 58, top, 58, 20,
            Component.translatable("options.renderDistance")));
        simulationInput = addRenderableWidget(new EditBox(font, left + panelWidth - 58, top + 30, 58, 20,
            Component.translatable("options.simulationDistance")));
        for (EditBox input : new EditBox[]{renderInput, simulationInput}) {
            input.setMaxLength(3);
            input.setFilter(text -> text.chars().allMatch(c -> c >= '0' && c <= '9'));
        }
        renderInput.setValue(Integer.toString(minecraft.options.renderDistance().get()));
        simulationInput.setValue(Integer.toString(minecraft.options.simulationDistance().get()));
        renderSlider = addRenderableWidget(new DistanceSlider(left, top, panelWidth - 68,
            "options.renderDistance", DistancePolicy.MAX_RENDER, minecraft.options.renderDistance().get(), renderInput));
        simulationSlider = addRenderableWidget(new DistanceSlider(left, top + 30, panelWidth - 68,
            "options.simulationDistance", DistancePolicy.MAX_SIMULATION, minecraft.options.simulationDistance().get(), simulationInput));
        renderInput.setResponder(text -> refresh()); simulationInput.setResponder(text -> refresh());
        addRenderableWidget(Button.builder(relocationLabel(), button -> {
            fastRelocation = !fastRelocation; button.setMessage(relocationLabel());
        }).bounds(left, top + 96, panelWidth, 20).build());
        apply = addRenderableWidget(Button.builder(Component.translatable("deepprimal.distance.apply"), button -> applyChanges())
            .bounds(left, top + 134, panelWidth / 2 - 5, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> onClose())
            .bounds(left + panelWidth / 2 + 5, top + 134, panelWidth / 2 - 5, 20).build());
        refresh();
    }

    private Component relocationLabel() {
        return Component.translatable("deepprimal.distance.fast_relocation", fastRelocation ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
    }
    private OptionalInt renderValue() { return DistancePolicy.parse(renderInput.getValue(), DistancePolicy.MAX_RENDER); }
    private OptionalInt simulationValue() { return DistancePolicy.parse(simulationInput.getValue(), DistancePolicy.MAX_SIMULATION); }
    private void refresh() {
        var render = renderValue(); var simulation = simulationValue();
        apply.active = render.isPresent() && simulation.isPresent();
        renderInput.setTextColor(render.isPresent() ? 0xffeeeeee : 0xffff7777);
        simulationInput.setTextColor(simulation.isPresent() ? 0xffeeeeee : 0xffff7777);
        render.ifPresent(renderSlider::setDistance); simulation.ifPresent(simulationSlider::setDistance);
    }
    private void applyChanges() {
        var render = renderValue(); var simulation = simulationValue();
        if (render.isEmpty() || simulation.isEmpty()) return;
        DistanceRuntime.clientRequest(render.getAsInt());
        minecraft.options.renderDistance().set(render.getAsInt());
        minecraft.options.simulationDistance().set(simulation.getAsInt());
        ClientPerformanceConfig.FAST_VIEW_RELOCATION.set(fastRelocation);
        ClientPerformanceConfig.SPEC.save();
        minecraft.options.save();
        onClose();
    }
    @Override public void onClose() { minecraft.setScreen(parent); }

    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title, width / 2, top - 24, 0xffffffff);
        Component line;
        var render = renderValue();
        if (render.isEmpty() || simulationValue().isEmpty()) {
            line = Component.translatable("deepprimal.distance.invalid", DistancePolicy.MAX_RENDER, DistancePolicy.MAX_SIMULATION);
        } else {
            int serverRange = ((OptionsAccessor)minecraft.options).deepprimal$serverRenderDistance();
            if (minecraft.getSingleplayerServer() != null && render.getAsInt() > DistanceRuntime.trackingCapacity())
                line = Component.translatable("deepprimal.distance.reopen", DistanceRuntime.trackingCapacity());
            else if (minecraft.level != null && serverRange > 0)
                line = Component.translatable("deepprimal.distance.server", serverRange);
            else line = Component.translatable("deepprimal.distance.ranges", DistancePolicy.MAX_RENDER, DistancePolicy.MAX_SIMULATION);
        }
        graphics.textWithWordWrap(font, line, left, top + 60, panelWidth, 0xffdddddd);
        graphics.text(font, Component.translatable("deepprimal.distance.backend", RenderSystem.getDevice().getBackendName()), left, top + 83, 0xffaaaaaa);
        if (minecraft.level != null && render.isPresent()) {
            long sections = DistancePolicy.sectionSlots(render.getAsInt(), minecraft.level.getSectionsCount());
            graphics.text(font, Component.translatable("deepprimal.distance.sections", sections), left, top + 120, 0xffaaaaaa);
        }
    }

    private static final class DistanceSlider extends AbstractSliderButton {
        private final String label;
        private final int maximum;
        private final EditBox input;
        DistanceSlider(int x, int y, int width, String label, int maximum, int initial, EditBox input) {
            super(x, y, width, 20, Component.empty(), (initial - 2.0) / (maximum - 2.0));
            this.label = label; this.maximum = maximum; this.input = input; updateMessage();
        }
        int distance() { return (int)Math.round(2 + value * (maximum - 2)); }
        void setDistance(int distance) { value = (distance - 2.0) / (maximum - 2.0); updateMessage(); }
        @Override protected void updateMessage() {
            setMessage(Component.translatable(label).append(": " + distance()));
        }
        @Override protected void applyValue() { input.setValue(Integer.toString(distance())); }
    }
}
