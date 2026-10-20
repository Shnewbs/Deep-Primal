package com.shnewbs.deepprimal.themes.assets;

import com.shnewbs.deepprimal.themes.Theme;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;

/** Builds native-resolution themed atlases, preserving every alpha mask and slot boundary. */
public final class GenerateThemes {
    private static final String GUI = "assets/minecraft/textures/gui/";
    private static final String INVENTORY = GUI + "container/inventory.png";
    public static void main(String[] args) throws Exception {
        Path output = Path.of(args[0]);
        Path input = null;
        for (int i = 1; i < args.length; i++) {
            Path candidate = Path.of(args[i]);
            if (!Files.isRegularFile(candidate) || !candidate.toString().endsWith(".jar")) continue;
            try (var zip = new ZipFile(candidate.toFile())) {
                if (zip.getEntry(INVENTORY) != null) { input = candidate; break; }
            }
        }
        if (input == null) throw new IOException("Minecraft GUI resources were not found in compileClasspath");
        List<String> report = new ArrayList<>();
        try (var zip = new ZipFile(input.toFile())) {
            var entries = zip.stream().filter(e -> !e.isDirectory() && included(e.getName())).sorted((a,b) -> a.getName().compareTo(b.getName())).toList();
            if (entries.size() < 100) throw new IOException("Unexpectedly few GUI resources: " + entries.size());
            for (Theme theme : Theme.values()) {
                if (!theme.custom()) continue;
                Path pack = output.resolve("themes/" + theme.id());
                Files.createDirectories(pack);
                Files.writeString(pack.resolve("pack.mcmeta"), "{\"pack\":{\"description\":\"Deep Primal: " + theme.displayName + "\",\"min_format\":[84,0],\"max_format\":[84,0]}}\n");
                int images = 0;
                for (var entry : entries) {
                    Path dest = pack.resolve(entry.getName());
                    Files.createDirectories(dest.getParent());
                    if (entry.getName().endsWith(".mcmeta")) {
                        try (var in = zip.getInputStream(entry)) { Files.copy(in, dest); }
                    } else {
                        BufferedImage src;
                        try (var in = zip.getInputStream(entry)) { src = ImageIO.read(in); }
                        if (src == null) throw new IOException("Cannot decode " + entry.getName());
                        BufferedImage image = style(src, theme, entry.getName());
                        verify(src, image, entry.getName());
                        if (entry.getName().equals(INVENTORY)) verifySlots(image, theme);
                        ImageIO.write(image, "PNG", dest.toFile()); images++;
                    }
                }
                Path panel = pack.resolve("assets/deepprimal_themes/textures/gui/sprites/panel.png");
                Files.createDirectories(panel.getParent());
                BufferedImage p = new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB);
                for(int y=0;y<32;y++)for(int x=0;x<32;x++)p.setRGB(x,y,panel(theme,x,y,32,32,false));
                ImageIO.write(p,"PNG",panel.toFile());
                Files.writeString(Path.of(panel+".mcmeta"), "{\"gui\":{\"scaling\":{\"type\":\"nine_slice\",\"width\":32,\"height\":32,\"border\":4}}}\n");
                report.add(theme.id() + ": " + images + " textures; dimensions and alpha masks preserved; 36 inventory slots verified");
            }
        }
        Files.write(output.resolve("theme-assets-report.txt"), report);
        report.forEach(System.out::println);
    }
    private static boolean included(String path) {
        if (!path.startsWith(GUI) || !(path.endsWith(".png") || path.endsWith(".png.mcmeta"))) return false;
        String name = path.substring(GUI.length());
        return name.startsWith("container/") || name.startsWith("sprites/widget/")
            || name.startsWith("sprites/container/") || name.startsWith("sprites/recipe_book/")
            || name.startsWith("sprites/tooltip/") || name.startsWith("sprites/advancements/")
            || name.startsWith("sprites/social_interactions/") || name.startsWith("sprites/toast/")
            || name.startsWith("sprites/hud/hotbar") || name.startsWith("sprites/hud/effect_background")
            || name.equals("menu_background.png") || name.equals("inworld_menu_background.png")
            || name.contains("header_separator") || name.contains("footer_separator");
    }
    private static BufferedImage style(BufferedImage src, Theme t, String path) {
        int w=src.getWidth(), h=src.getHeight();
        BufferedImage dst=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
        boolean button=path.contains("/widget/button"), highlight=path.contains("highlighted") || path.contains("hotbar_selection");
        for(int y=0;y<h;y++)for(int x=0;x<w;x++) {
            int pixel=src.getRGB(x,y), alpha=pixel>>>24;
            if(alpha==0) { dst.setRGB(x,y,pixel); continue; }
            int r=pixel>>16&255, g=pixel>>8&255, b=pixel&255;
            int max=Math.max(r,Math.max(g,b)), min=Math.min(r,Math.min(g,b));
            int c;
            if(button) c=panel(t,x,y,w,h,highlight);
            else if(max-min>30 && !path.contains("hotbar")) c=pixel;
            else if(r==139 && g==139 && b==139) c=t.well;
            else if(max<40) c=mix(t.well,0xff000000,0.55);
            else if(max>225) c=mix(t.accent,0xffffffff,0.32);
            else if(max>160) c=material(t,x,y);
            else c=mix(t.well,t.surface,(max-40)/185.0);
            if(highlight && !button) c=mix(c,t.accent,0.6);
            if(path.contains("disabled")) c=mix(c,t.surface,0.6);
            dst.setRGB(x,y,(alpha<<24)|(c&0xffffff));
        }
        return dst;
    }
    private static int panel(Theme t,int x,int y,int w,int h,boolean highlighted) {
        int edge=Math.min(Math.min(x,w-1-x),Math.min(y,h-1-y));
        if(edge==0)return mix(t.well,0xff000000,0.4);
        if(edge==1)return highlighted?mix(t.accent,0xffffffff,0.35):t.accent;
        if(edge==2)return t.well;
        return highlighted?mix(material(t,x,y),t.accent,0.12):material(t,x,y);
    }
    private static int material(Theme t,int x,int y) {
        int hash=(x*73428767)^(y*912931);double grain=((hash>>>5)&7)/7.0;
        int c=mix(t.surface,t.well,grain*0.055);
        return switch(t) {
            case TIMBER_IRON -> mix(c,t.well,(y%8==0?0.18:Math.floorMod(x/9+y*5,17)==0?0.1:0));
            case FIELD_JOURNAL -> (x%32==4 && y%7<3)?mix(c,t.accent,0.13):c;
            case RAWHIDE_FLINT -> ((y%32==3||x%32==3)&&(x+y)%6<2)?mix(c,t.accent,0.28):c;
            case RIVERSTONE_MOSS -> Math.floorMod(hash,47)<2?mix(c,t.accent,0.2):c;
            case CLAY_EMBER -> y%32==3?mix(c,t.accent,0.12):c;
            case BASALT_COPPER -> Math.floorMod(hash,137)==0?mix(c,t.accent,0.2):c;
            case FROST_SLATE -> (x+y)%41==0?mix(c,t.accent,0.12):c;
            default -> c;
        };
    }
    private static int mix(int a,int b,double f) {
        f=Math.clamp(f,0,1);int result=0xff000000;
        for(int shift:new int[]{0,8,16})result|=(int)Math.round(((a>>>shift)&255)*(1-f)+((b>>>shift)&255)*f)<<shift;
        return result;
    }
    private static void verify(BufferedImage src,BufferedImage dst,String name) {
        if(src.getWidth()!=dst.getWidth()||src.getHeight()!=dst.getHeight())throw new IllegalStateException("Dimensions changed: "+name);
        for(int y=0;y<src.getHeight();y++)for(int x=0;x<src.getWidth();x++)
            if((src.getRGB(x,y)>>>24)!=(dst.getRGB(x,y)>>>24))throw new IllegalStateException("Alpha changed: "+name+" at "+x+","+y);
    }
    private static void verifySlots(BufferedImage image,Theme t) {
        for(int row=0;row<4;row++)for(int col=0;col<9;col++) {
            int x=8+col*18,y=row==3?142:84+row*18;
            for(int dy=0;dy<16;dy++)for(int dx=0;dx<16;dx++)
                if(image.getRGB(x+dx,y+dy)!=t.well)throw new IllegalStateException("Inventory well mismatch: "+t+" slot "+row+","+col);
        }
    }
}
