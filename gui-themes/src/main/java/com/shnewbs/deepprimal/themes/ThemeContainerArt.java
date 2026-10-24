package com.shnewbs.deepprimal.themes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.resources.Identifier;

/** Replace vanilla container backplates while retaining actual menu geometry and progress overlays. */
public final class ThemeContainerArt {
    public static boolean background(GuiGraphicsExtractor g,Identifier id,int x,int y,int w,int h,float u,float v){
        if(ThemeMod.active().custom()&&id.equals(Identifier.withDefaultNamespace("textures/gui/recipe_book.png"))&&w==147&&h==166){ThemeArt.panel(g,ThemeMod.active(),x,y,w,h);return true;}
        // Chests draw two texture slices. Replace them with one continuous frame.
        if(ThemeMod.active().custom()&&id.equals(Identifier.withDefaultNamespace("textures/gui/container/generic_54.png"))
                &&Minecraft.getInstance().screen instanceof ContainerScreen chest&&chest.getClass()==ContainerScreen.class
                &&x==chest.getLeftPos()&&w==chest.getImageWidth()&&u==0){
            if(v==0&&y==chest.getTopPos()){
                ThemeArt.panel(g,ThemeMod.active(),x,y,w,114+chest.getMenu().getRowCount()*18);
                for(var slot:chest.getMenu().slots)if(slot.isActive())ThemeArt.well(g,ThemeMod.active(),x+slot.x-1,y+slot.y-1,18,18);
                return true;
            }
            if(v==126&&y==chest.getTopPos()+chest.getMenu().getRowCount()*18+17&&h==96)return true;
        }
        if(!ThemeMod.active().custom()||!id.getNamespace().equals("minecraft")||!id.getPath().startsWith("textures/gui/container/")||w<150||h<100||u!=0||v!=0)return false;
        if(!(Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> s)||s instanceof ThemeInventoryScreen)return false;
        // Keep functional guides and indicators baked into specialized workstation textures.
        if(s.getClass()!=CreativeModeInventoryScreen.class)return false;
        // Do not replace tab/icon fragments or animated overlays.
        if(x!=s.getLeftPos()||y!=s.getTopPos()||w!=s.getImageWidth())return false;
        var t=ThemeMod.active();ThemeArt.panel(g,t,x,y,w,h);
        for(var slot:s.getMenu().slots)if(slot.isActive())ThemeArt.well(g,t,x+slot.x-1,y+slot.y-1,18,18);
        return true;
    }
    public static boolean sprite(GuiGraphicsExtractor g,Identifier id,int x,int y,int w,int h){
        if(!ThemeMod.active().custom()||!id.getNamespace().equals("minecraft"))return false;
        var t=ThemeMod.active();
        String path=id.getPath();
        if(path.equals("toast/recipe")||path.equals("toast/system")||path.equals("toast/tutorial")||path.equals("toast/advancement")){
            ThemeArt.button(g,t,x,y,w,h,false,true);return true;
        }
        if(id.getPath().startsWith("widget/button")){
            ThemeArt.button(g,t,x,y,w,h,id.getPath().contains("highlighted"),!id.getPath().contains("disabled"));return true;
        }
        if(id.getPath().startsWith("container/creative_inventory/tab_")){
            ThemeArt.button(g,t,x,y,w,h,!id.getPath().contains("unselected"),true);return true;
        }
        if(id.getPath().equals("hud/hotbar")){
            ThemeArt.region(g,t,ThemeArt.of(t).material(),x,y,w,h);
            ThemeArt.frame(g,t,x,y,w,h,2);
            for(int i=0;i<9;i++)ThemeArt.well(g,t,x+2+20*i,y+2,18,18);
            return true;
        }
        if(id.getPath().equals("hud/hotbar_selection")){
            g.outline(x+2,y+2,w-4,h-3,0xfff4e5ac);g.outline(x+3,y+3,w-6,h-5,t.accent);return true;
        }
        if(path.equals("hud/hotbar_offhand_left")||path.equals("hud/hotbar_offhand_right")){
            int slotX=x+(path.endsWith("left")?2:9);
            ThemeArt.panel(g,t,slotX-2,y+1,22,22);
            ThemeArt.well(g,t,slotX,y+3,18,18);return true;
        }
        return false;
    }
    private ThemeContainerArt(){}
}
