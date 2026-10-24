package com.shnewbs.deepprimal.themes;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/** Responsive 4K artwork and controls share a transform; protect the logo and menu when cropping. */
public final class ThemeMenuArt {
    public static final int TEXTURE_WIDTH=3840, TEXTURE_HEIGHT=2160;
    private record Layout(int x,int y,int width,int height,int step) {}
    private static Layout layout(Theme theme) {
        return switch(theme) {
            case BASALT_COPPER -> new Layout(90,510,480,60,78);
            case FIELD_JOURNAL -> new Layout(45,480,360,65,81);
            case RAWHIDE_FLINT -> new Layout(100,370,365,75,95);
            case RIVERSTONE_MOSS -> new Layout(150,420,350,62,77);
            case TIMBER_IRON -> new Layout(125,328,370,67,94);
            case CLAY_EMBER -> new Layout(115,330,270,78,98);
            case OBSIDIAN_ASH -> new Layout(650,365,372,72,92);
            case FROST_SLATE -> new Layout(650,340,370,70,92);
            default -> throw new IllegalArgumentException("Vanilla has no custom menu art");
        };
    }
    public static Identifier texture(Theme theme) {
        return Identifier.fromNamespaceAndPath(ThemeMod.ID,"textures/menu/"+theme.id()+".png");
    }
    public static ThemeArt.Rect viewport(Theme theme,int width,int height) {
        var p=layout(theme);
        // Prefer filling the window. Limit zoom on extreme aspect ratios to keep the
        // logo and four actions visible; use a matching dark backdrop in those cases.
        double safeTop=switch(theme){case BASALT_COPPER->180;case FIELD_JOURNAL->35;case RAWHIDE_FLINT->100;
            case RIVERSTONE_MOSS->125;case TIMBER_IRON->160;case CLAY_EMBER->165;
            case OBSIDIAN_ASH->150;case FROST_SLATE->130;default->0;};
        double safeLeft=p.x-24, safeRight=p.x+p.width+24;
        double safeBottom=p.y+3*p.step+p.height+30;
        double scale=Math.max(width/1672.0,height/941.0);
        scale=Math.min(scale,Math.min((width-12)/(safeRight-safeLeft),(height-footerHeight(width,height)-12)/(safeBottom-safeTop)));
        int w=(int)Math.ceil(1672*scale),h=(int)Math.ceil(941*scale);
        double x=(width-w)/2.0,y=(height-h)/2.0;
        x=Math.max(6-safeLeft*scale,Math.min(x,width-6-safeRight*scale));
        y=Math.max(6-safeTop*scale,Math.min(y,height-footerHeight(width,height)-6-safeBottom*scale));
        // Avoid unnecessary gutters when the protected region already fits.
        if(w>=width)x=Math.max(width-w,Math.min(0,x));
        if(h>=height)y=Math.max(height-h,Math.min(0,y));
        return new ThemeArt.Rect((int)Math.round(x),(int)Math.round(y),w,h);
    }
    public static int footerHeight(int width,int height){
        return (int)Math.round(28*Math.max(1,Math.min(width/640.0,height/360.0)));
    }
    public static ThemeArt.Rect footerButton(int index,int count,int width,int height){
        double scale=Math.max(1,Math.min(width/640.0,height/360.0));
        int gap=Math.max(4,(int)Math.round(4*scale)),h=(int)Math.round(20*scale);
        int w=Math.min((int)Math.round(72*scale),(width-gap*(count+1))/count);
        int total=count*w+(count-1)*gap;
        return new ThemeArt.Rect((width-total)/2+index*(w+gap),height-h-gap,w,h);
    }
    public static ThemeArt.Rect button(Theme theme,int index,int width,int height) {
        if(index<0||index>=4)throw new IllegalArgumentException("Menu button index");
        var v=viewport(theme,width,height);var p=layout(theme);
        return new ThemeArt.Rect(v.x()+(int)Math.round(p.x*v.w()/1672.0),
            v.y()+(int)Math.round((p.y+index*p.step)*v.h()/941.0),
            (int)Math.round(p.width*v.w()/1672.0),(int)Math.round(p.height*v.h()/941.0));
    }
    public static void background(GuiGraphicsExtractor g,Theme theme,int width,int height) {
        var v=viewport(theme,width,height);
        g.fill(0,0,width,height,0xff101214);
        g.blit(RenderPipelines.GUI_TEXTURED,texture(theme),v.x(),v.y(),0,0,v.w(),v.h(),
            TEXTURE_WIDTH,TEXTURE_HEIGHT,TEXTURE_WIDTH,TEXTURE_HEIGHT);
    }
    private ThemeMenuArt() {}
}
