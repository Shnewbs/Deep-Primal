package com.shnewbs.deepprimal.themes;
import com.shnewbs.deepprimal.themes.mixin.ThemeSlotAccessor;
import java.util.IdentityHashMap;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
final class ThemeSlotLayout {
    private final AbstractContainerMenu menu;
    private final IdentityHashMap<Slot,int[]> original=new IdentityHashMap<>();
    ThemeSlotLayout(AbstractContainerMenu menu){this.menu=menu;}
    void move(int i,int x,int y){var s=menu.getSlot(i);original.putIfAbsent(s,new int[]{s.x,s.y});var a=(ThemeSlotAccessor)(Object)s;a.deepprimalThemes$x(x);a.deepprimalThemes$y(y);}
    void playerGrid(int first){for(int i=0;i<27;i++)move(first+i,23+(i%9)*24,107+(i/9)*24);for(int i=0;i<9;i++)move(first+27+i,23+i*24,183);}
    void restore(){original.forEach((s,p)->{var a=(ThemeSlotAccessor)(Object)s;a.deepprimalThemes$x(p[0]);a.deepprimalThemes$y(p[1]);});original.clear();}
}
