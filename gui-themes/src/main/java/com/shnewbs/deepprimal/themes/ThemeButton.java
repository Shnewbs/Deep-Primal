package com.shnewbs.deepprimal.themes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public final class ThemeButton extends Button {
    public boolean selected;
    public ThemeButton(int x,int y,int w,int h,String label,OnPress action) {
        super(x,y,w,h,Component.literal(label),action,DEFAULT_NARRATION);
    }
    @Override protected void extractContents(GuiGraphicsExtractor g,int mx,int my,float delta) {
        var t=ThemeMod.active();
        if(!t.custom()) {extractDefaultSprite(g);extractDefaultLabel(g.textRendererForWidget(this,GuiGraphicsExtractor.HoveredTextEffects.NONE));return;}
        ThemeArt.button(g,t,getX(),getY(),width,height,selected||isHoveredOrFocused(),active);
        var font=Minecraft.getInstance().font;
        int color=!active?0xff999999:(selected||isHoveredOrFocused())?0xfff7f0de:t.text;
        g.text(font,getMessage(),getX()+(width-font.width(getMessage()))/2,getY()+(height-8)/2,color,false);
    }
}
