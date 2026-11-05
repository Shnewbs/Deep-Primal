package com.shnewbs.deepprimal.themes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public final class ThemeButton extends Button {
    public boolean selected;
    private float preferredTextScale=1f;
    public ThemeButton textScale(float scale){preferredTextScale=scale;return this;}
    public ThemeButton(int x,int y,int w,int h,String label,OnPress action) {
        super(x,y,w,h,Component.literal(label),action,DEFAULT_NARRATION);
    }
    @Override protected void extractContents(GuiGraphicsExtractor g,int mx,int my,float delta) {
        var t=ThemeMod.active();
        if(!t.custom()) {extractDefaultSprite(g);extractDefaultLabel(g.textRendererForWidget(this,GuiGraphicsExtractor.HoveredTextEffects.NONE));return;}
        ThemeArt.button(g,t,getX(),getY(),width,height,selected||isHoveredOrFocused(),active);
        var font=Minecraft.getInstance().font;
        int color=ThemeArt.textColor(t,selected||isHoveredOrFocused(),active);
        int textWidth=font.width(getMessage());
        float scale=Math.min(preferredTextScale,Math.min((width-4f)/Math.max(1,textWidth),(height-4f)/9f));
        double pixelScale=Minecraft.getInstance().getWindow().getGuiScale();
        // Keep glyph pixels and the label origin aligned with physical display pixels.
        float fit=Math.min((width-4f)/Math.max(1,textWidth),(height-4f)/9f);
        scale=(float)(Math.max(1,Math.min(Math.round(scale*pixelScale),Math.floor(fit*pixelScale)))/pixelScale);
        String label=getMessage().getString();
        int maxTextWidth=Math.max(1,(int)((width-4)/scale));
        if(textWidth>maxTextWidth){
            label=font.plainSubstrByWidth(label,Math.max(0,maxTextWidth-font.width("…")))+"…";
            textWidth=font.width(label);
        }
        float textX=(float)(Math.round((getX()+(width-textWidth*scale)/2f)*pixelScale)/pixelScale);
        float textY=(float)(Math.round((getY()+(height-8*scale)/2f)*pixelScale)/pixelScale);
        g.pose().pushMatrix();
        g.pose().translate(textX,textY);
        g.pose().scale(scale,scale);
        g.text(font,label,0,0,color,false);
        g.pose().popMatrix();
    }
}
