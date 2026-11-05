package com.shnewbs.deepprimal.themes.mixin;

import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Slot.class)
public interface ThemeSlotAccessor {
    @Mutable @Accessor("x") void deepprimalThemes$x(int x);
    @Mutable @Accessor("y") void deepprimalThemes$y(int y);
}
