package com.shnewbs.deepprimal.themes;

import com.shnewbs.deepprimal.themes.api.ThemeIntegration;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

final class ThemeScreens {
    @SubscribeEvent public void init(ScreenEvent.Init.Post event) {
        var screen = event.getScreen();
        if (screen instanceof TitleScreen || screen instanceof OptionsScreen) {
            event.addListener(Button.builder(Component.literal("Themes"), b -> Minecraft.getInstance().setScreen(new ThemeScreen(screen)))
                    .bounds(4, 4, 58, 20).build());
        }
        if (screen instanceof InventoryScreen inventory && ThemeMod.active().custom() && ThemeConfig.INTEGRATION_HUD.get()) {
            int x = inventory.getLeftPos(), y = inventory.getTopPos() - 22;
            if (y < 2) return;
            addAction(event, x, y, () -> ThemeIntegration.provider().journal());
            addAction(event, x + 90, y, () -> ThemeIntegration.provider().hearth());
        }
    }
    private static void addAction(ScreenEvent.Init.Post event, int x, int y, Supplier<Optional<ThemeIntegration.Action>> source) {
        source.get().ifPresent(action -> {
            var button = Button.builder(Component.literal(action.label()), b -> source.get().filter(ThemeIntegration.Action::enabled).ifPresent(a -> a.invoke().run()))
                    .bounds(x, y, 86, 20).build();
            button.active = action.enabled();
            event.addListener(button);
        });
    }
}
