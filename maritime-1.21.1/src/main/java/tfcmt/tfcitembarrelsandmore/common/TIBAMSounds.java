package tfcmt.tfcitembarrelsandmore.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class TIBAMSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, (String)"tfc_item_barrels_and_more");
    public static final DeferredHolder<SoundEvent, SoundEvent> ENGINE_STEAM_HISS = SOUNDS.register("block.engine.steam_hiss", () -> SoundEvent.createVariableRangeEvent((ResourceLocation)ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block.engine.steam_hiss")));
    public static final DeferredHolder<SoundEvent, SoundEvent> TELEGRAPH_BELL = SOUNDS.register("block.telegraph.bell", () -> SoundEvent.createVariableRangeEvent((ResourceLocation)ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block.telegraph.bell")));
}

