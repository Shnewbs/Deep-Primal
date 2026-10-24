package com.shnewbs.deepprimal.themes;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/** Atlas regions refer to the approved boards, retained unmodified as source artwork.
 * Only empty materials and well interiors are sampled; rims are drawn at runtime.
 * No mock inventory items, player models or survival values are rendered. */
public final class ThemeArt {
    public record Rect(int x, int y, int w, int h) {}
    public record Art(Rect material, Rect well, Rect button) {}
    private static Rect r(int x,int y,int w,int h) { return new Rect(x,y,w,h); }
    private static final java.util.EnumMap<Theme,Art> ART = new java.util.EnumMap<>(Theme.class);
    private static final java.util.EnumMap<Theme,Identifier> TEXTURES = new java.util.EnumMap<>(Theme.class);
    static {
        for (Theme t : Theme.values()) if (t.custom()) {
            ART.put(t, switch(t) {
                case BASALT_COPPER -> new Art(r(1310,156,100,29),r(970,354,56,45),r(74,350,211,32));
                case FIELD_JOURNAL -> new Art(r(1158,291,190,26),r(965,386,55,41),r(45,333,166,33));
                case RAWHIDE_FLINT -> new Art(r(1240,140,145,24),r(943,361,52,49),r(80,287,184,39));
                case RIVERSTONE_MOSS -> new Art(r(1295,215,103,37),r(953,314,51,46),r(79,270,183,31));
                case TIMBER_IRON -> new Art(r(1178,270,243,31),r(960,368,56,44),r(99,281,174,38));
                case CLAY_EMBER -> new Art(r(1150,248,235,24),r(955,332,53,45),r(113,258,147,39));
                case OBSIDIAN_ASH -> new Art(r(1300,252,135,25),r(950,343,57,47),r(309,255,153,34));
                case FROST_SLATE -> new Art(r(1164,284,225,29),r(951,373,49,46),r(293,265,182,35));
                default -> throw new IllegalArgumentException("Vanilla has no art atlas");
            });
            TEXTURES.put(t,Identifier.fromNamespaceAndPath(ThemeMod.ID,"textures/reference/"+t.id()+".png"));
        }
    }
    public static Art of(Theme t) {
        Art art=ART.get(t);
        if(art==null)throw new IllegalArgumentException("Vanilla has no art atlas");
        return art;
    }
    public static void region(GuiGraphicsExtractor g, Theme t, Rect s, int x,int y,int w,int h) {
        if(w<=0||h<=0) return;
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURES.get(t),
            x,y,s.x,s.y,w,h,s.w,s.h,1536,1024);
    }
    /** Continuous code-drawn rims: no screen-background pixels or repeated corner fragments. */
    public static void frame(GuiGraphicsExtractor g,Theme t,int x,int y,int w,int h,int rim) {
        if(w<2||h<2)return;
        int trim=mix(t.accent,t.surface,.3);
        g.outline(x,y,w,h,mix(t.well,0xff000000,.7));
        if(w<4||h<4)return;
        g.horizontalLine(x+1,x+w-2,y+1,mix(trim,0xffffffff,.25));
        g.verticalLine(x+1,y+1,y+h-2,trim);
        g.horizontalLine(x+1,x+w-2,y+h-2,mix(trim,0xff000000,.45));
        g.verticalLine(x+w-2,y+1,y+h-2,mix(trim,0xff000000,.35));
        if(rim>=3&&w>5&&h>5)g.outline(x+2,y+2,w-4,h-4,mix(t.well,0xff000000,.35));
    }
    public static void panel(GuiGraphicsExtractor g,Theme t,int x,int y,int w,int h) {
        if(w<=0||h<=0)return;
        region(g,t,of(t).material,x,y,w,h);
        frame(g,t,x,y,w,h,3);
    }
    public static void well(GuiGraphicsExtractor g,Theme t,int x,int y,int w,int h) {
        if(w<3||h<3)return;
        Rect source=of(t).well;
        region(g,t,r(source.x+3,source.y+3,source.w-6,source.h-6),x+1,y+1,w-2,h-2);
        g.fill(x+1,y+1,x+w-1,y+h-1,0x18000000);
        g.horizontalLine(x,x+w-1,y,mix(t.well,0xff000000,.6));
        g.verticalLine(x,y,y+h-1,mix(t.well,0xff000000,.6));
        g.horizontalLine(x,x+w-1,y+h-1,mix(t.surface,0xffffffff,.2));
        g.verticalLine(x+w-1,y,y+h-1,mix(t.surface,0xffffffff,.2));
    }
    public static void button(GuiGraphicsExtractor g,Theme t,int x,int y,int w,int h,boolean selected,boolean enabled) {
        if(w<=0||h<=0)return;
        Rect s=of(t).button;
        region(g,t,r(s.x+s.w-19,s.y+6,10,s.h-12),x,y,w,h);
        if(selected&&enabled)g.fill(x,y,x+w,y+h,selectedFill(t));
        if(!enabled)g.fill(x,y,x+w,y+h,0x44000000);
        frame(g,t,x,y,w,h,2);
    }
    private static int selectedFill(Theme t){return switch(t){
        case FIELD_JOURNAL->0xff626e43;case CLAY_EMBER->0xff854322;case RIVERSTONE_MOSS->0xff356b8c;
        case TIMBER_IRON->0xff555b32;case FROST_SLATE->0xff386a88;default->mix(t.surface,t.accent,.28);
    };}
    public static int textColor(Theme t,boolean selected,boolean enabled){
        return !enabled?mix(t.text,t.surface,.42):selected?0xfff7f0de:t.text;
    }
    public static int mix(int a,int b,double amount){
        int result=0xff000000;
        for(int shift=0;shift<=16;shift+=8)result|=(int)Math.round(((a>>>shift)&255)*(1-amount)+((b>>>shift)&255)*amount)<<shift;
        return result;
    }
    public static void arrow(GuiGraphicsExtractor g,Theme t,int x,int y) {
        g.fill(x,y+3,x+12,y+7,t.accent);
        for(int n=0;n<6;n++)g.fill(x+10+n,y+n-1,x+11+n,y+11-n,t.accent);
    }
    private ThemeArt() {}
}
