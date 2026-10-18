package com.shnewbs.deepprimal.mixin.client;

import com.shnewbs.deepprimal.client.DistanceScreen;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VideoSettingsScreen.class)
public abstract class VideoSettingsMixin extends OptionsSubScreen {
    protected VideoSettingsMixin(Screen last, Options options, Component title) { super(last, options, title); }
    @Inject(method = "addOptions", at = @At("HEAD"))
    private void deepprimal$distanceButton(CallbackInfo callback) {
        this.list.addSmall(Button.builder(Component.translatable("deepprimal.distance.open"),
            button -> this.minecraft.setScreen(new DistanceScreen(this))).width(310).build(), null);
    }
}
