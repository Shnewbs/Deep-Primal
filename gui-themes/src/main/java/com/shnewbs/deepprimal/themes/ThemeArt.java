package com.shnewbs.deepprimal.themes;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/** Atlas regions refer to the approved boards, retained unmodified as source artwork.
 * Only empty material, borders and empty wells are sampled for live container UI.
 * No mock inventory items, player models or survival values are rendered. */
public final class ThemeArt {
    public record Rect(int x, int y, int w, int h) {}
    public record Art(Rect scene, Rect frame, Rect material, Rect well, Rect button, Rect[] menuButtons) {}
    private static Rect r(int x,int y,int w,int h) { return new Rect(x,y,w,h); }
    public static Art of(Theme t) {
        return switch (t) {
            case BASALT_COPPER -> new Art(r(20,99,743,431),r(156,575,498,388),r(1310,156,100,29),r(970,354,56,45),r(74,350,211,32),new Rect[]{r(72,309,215,36),r(74,350,211,32),r(74,386,211,32),r(74,424,211,32),r(74,464,211,32)});
            case FIELD_JOURNAL -> new Art(r(22,94,739,455),r(22,589,739,411),r(1158,291,190,26),r(965,386,55,41),r(45,333,166,33),new Rect[]{r(45,286,166,39),r(45,333,166,33),r(45,374,166,34),r(45,415,166,34),r(45,455,166,35)});
            case RAWHIDE_FLINT -> new Art(r(11,82,749,452),r(162,576,487,406),r(1240,140,145,24),r(943,361,52,49),r(80,287,184,39),new Rect[]{r(80,244,184,39),r(80,287,184,39),r(80,332,184,39),r(80,376,184,39),r(80,421,184,39)});
            case RIVERSTONE_MOSS -> new Art(r(0,42,764,448),r(149,552,474,432),r(1295,215,103,37),r(953,314,51,46),r(79,270,183,31),new Rect[]{r(79,231,183,33),r(79,270,183,31),r(79,307,183,31),r(79,343,183,31),r(79,379,183,31)});
            case TIMBER_IRON -> new Art(r(0,70,767,439),r(196,575,531,394),r(1178,270,243,31),r(960,368,56,44),r(99,281,174,38),new Rect[]{r(99,235,174,41),r(99,281,174,38),r(99,325,174,38),r(99,369,174,36),r(99,411,174,38)});
            case CLAY_EMBER -> new Art(r(8,53,754,440),r(163,552,515,430),r(1150,248,235,24),r(955,332,53,45),r(113,258,147,39),new Rect[]{r(113,214,147,40),r(113,258,147,39),r(113,301,147,39),r(113,345,147,39),r(113,388,147,40)});
            case OBSIDIAN_ASH -> new Art(r(15,60,745,454),r(176,565,449,400),r(1300,252,135,25),r(950,343,57,47),r(309,255,153,34),new Rect[]{r(303,213,165,38),r(309,255,153,34),r(309,295,153,34),r(309,336,153,35),r(309,377,153,35)});
            case FROST_SLATE -> new Art(r(2,100,763,438),r(179,560,481,351),r(1164,284,225,29),r(951,373,49,46),r(293,265,182,35),new Rect[]{r(292,221,184,35),r(293,265,182,35),r(293,310,182,35),r(293,354,182,35),r(293,398,182,35)});
            default -> throw new IllegalArgumentException("Vanilla has no art atlas");
        };
    }
    public static void region(GuiGraphicsExtractor g, Theme t, Rect s, int x,int y,int w,int h) {
        if(w<=0||h<=0) return;
        g.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(ThemeMod.ID,"textures/reference/"+t.id()+".png"),
            x,y,s.x,s.y,w,h,s.w,s.h,1536,1024);
    }
    private static void edges(GuiGraphicsExtractor g,Theme t,Rect s,int x,int y,int w,int h,int src,int dst) {
        int[] xx={x,x+dst,x+w-dst}, yy={y,y+dst,y+h-dst};
        int[] ww={dst,w-2*dst,dst}, hh={dst,h-2*dst,dst};
        int[] sx={s.x,s.x+src,s.x+s.w-src}, sy={s.y,s.y+src,s.y+s.h-src};
        int[] sw={src,s.w-2*src,src}, sh={src,s.h-2*src,src};
        for(int row=0;row<3;row++) for(int col=0;col<3;col++) if(row!=1||col!=1)
            region(g,t,r(sx[col],sy[row],sw[col],sh[row]),xx[col],yy[row],ww[col],hh[row]);
    }
    public static void panel(GuiGraphicsExtractor g,Theme t,int x,int y,int w,int h) {
        Art a=of(t);
        tile(g,t,a.material,x,y,w,h,Math.max(8,a.material.w/2),Math.max(8,a.material.h/2));
        Rect f=a.frame;
        // Short edge samples avoid headings, item counts and controls elsewhere on the board.
        tile(g,t,r(f.x+24,f.y+f.h-7,28,7),x+5,y,w-10,5,14,5);
        tile(g,t,r(f.x+24,f.y+f.h-7,28,7),x+5,y+h-5,w-10,5,14,5);
        tile(g,t,r(f.x,f.y+45,14,30),x,y+5,7,h-10,7,15);
        tile(g,t,r(f.x+f.w-14,f.y+45,14,30),x+w-7,y+5,7,h-10,7,15);
        region(g,t,r(f.x,f.y,14,14),x,y,7,7);
        region(g,t,r(f.x+f.w-14,f.y,14,14),x+w-7,y,7,7);
        region(g,t,r(f.x,f.y+f.h-14,14,14),x,y+h-7,7,7);
        region(g,t,r(f.x+f.w-14,f.y+f.h-14,14,14),x+w-7,y+h-7,7,7);
        if(t==Theme.FIELD_JOURNAL && w>210 && h>120) {
            region(g,t,r(837,181,64,330),x+4,y+23,18,h-35);
            region(g,t,r(1431,178,67,332),x+w-22,y+23,18,h-35);
        }
    }
    private static void tile(GuiGraphicsExtractor g,Theme t,Rect s,int x,int y,int w,int h,int tw,int th){
        for(int yy=0;yy<h;yy+=th)for(int xx=0;xx<w;xx+=tw){
            int dw=Math.min(tw,w-xx),dh=Math.min(th,h-yy);
            region(g,t,r(s.x,s.y,Math.max(1,s.w*dw/tw),Math.max(1,s.h*dh/th)),x+xx,y+yy,dw,dh);
        }
    }
    public static void well(GuiGraphicsExtractor g,Theme t,int x,int y,int w,int h) {
        region(g,t,of(t).well,x,y,w,h);
        g.outline(x,y,w,h,0x99000000|(t.text&0xffffff));
    }
    public static void button(GuiGraphicsExtractor g,Theme t,int x,int y,int w,int h,boolean selected,boolean enabled) {
        Rect s=of(t).button;
        region(g,t,r(s.x+s.w-19,s.y+6,10,s.h-12),x,y,w,h);
        edges(g,t,s,x,y,w,h,4,2);
        if(selected) {
            int fill=switch(t){case FIELD_JOURNAL->0xcc626e43;case CLAY_EMBER->0xddb65325;case RIVERSTONE_MOSS->0xdd356b8c;case TIMBER_IRON->0xdd555b32;case FROST_SLATE->0xbb386a88;default->(t.accent&0xffffff)|0x35000000;};
            g.fill(x+2,y+2,x+w-2,y+h-2,fill);g.outline(x,y,w,h,t.accent);
        }
        if(!enabled) g.fill(x+1,y+1,x+w-1,y+h-1,0x66000000);
    }
    public static void arrow(GuiGraphicsExtractor g,Theme t,int x,int y) {
        g.fill(x,y+3,x+12,y+7,t.accent);
        for(int n=0;n<6;n++)g.fill(x+10+n,y+n-1,x+11+n,y+11-n,t.accent);
    }
    private ThemeArt() {}
}
