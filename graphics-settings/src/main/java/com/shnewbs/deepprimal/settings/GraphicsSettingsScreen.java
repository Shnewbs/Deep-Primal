package com.shnewbs.deepprimal.settings;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModList;

/** Backend-neutral API selection. Native initialization belongs to the selected module. */
public final class GraphicsSettingsScreen extends Screen {
  private final Screen parent;
  private RendererPreferences.Backend backend = RendererPreferences.savedBackend();
  private boolean backendSelected;
  private String error;
  private int left, panelWidth, top;

  public GraphicsSettingsScreen(Screen parent) {
    super(Component.literal("Deep Primal graphics"));
    this.parent = parent;
  }

  private Component selection() {
    return Component.literal(
        "After restart: "
            + backend
            + (backend == RendererPreferences.Backend.Vulkan ? " (experimental)" : ""));
  }

  @Override
  protected void init() {
    panelWidth = Math.min(440, width - 40);
    left = (width - panelWidth) / 2;
    top = Math.max(40, height / 2 - 80);
    var choice =
        addRenderableWidget(
            Button.builder(
                    selection(),
                    button -> {
                      backend =
                          backend == RendererPreferences.Backend.Vulkan
                              ? RendererPreferences.Backend.OpenGL
                              : RendererPreferences.Backend.Vulkan;
                      button.setMessage(selection());
                      backendSelected = true;
                      button.active = ModList.get().isLoaded("deepprimal_vulkan")
                          || backend == RendererPreferences.Backend.Vulkan;
                    })
                .bounds(left, top, panelWidth, 20)
                .build());
    choice.active = ModList.get().isLoaded("deepprimal_vulkan")
        || backend == RendererPreferences.Backend.Vulkan;
    addRenderableWidget(
        Button.builder(
                Component.literal("Apply"),
                button -> {
                  try {
                    if (backendSelected) RendererPreferences.save(backend);
                    onClose();
                  } catch (java.io.IOException failure) {
                    error = "Could not save the graphics preference.";
                  }
                })
            .bounds(left, top + 122, panelWidth / 2 - 5, 20)
            .build());
    addRenderableWidget(
        Button.builder(Component.literal("Cancel"), button -> onClose())
            .bounds(left + panelWidth / 2 + 5, top + 122, panelWidth / 2 - 5, 20)
            .build());
  }

  @Override
  public void onClose() {
    minecraft.setScreen(parent);
  }

  @Override
  public void extractRenderState(
      GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    graphics.centeredText(font, title, width / 2, top - 24, 0xffffffff);
    graphics.textWithWordWrap(
        font,
        Component.literal(
            "Active: "
                + RenderSystem.getDevice().getBackendName()
                + " — "
                + RenderSystem.getDevice().getRenderer()),
        left,
        top + 32,
        panelWidth,
        0xffdddddd);
    graphics.textWithWordWrap(
        font,
        Component.literal(
            error != null
                ? error
                : RendererStatus.explanation(RenderSystem.getDevice().getBackendName(), backend)),
        left,
        top + 72,
        panelWidth,
        0xffaaaaaa);
  }
}
