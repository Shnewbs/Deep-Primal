package com.shnewbs.deepprimal.themes;

import com.mojang.blaze3d.platform.NativeImage;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Opt-in menu regression checks; never packaged in the release JAR. */
@EventBusSubscriber(modid=ThemeMod.ID,value=Dist.CLIENT)
public final class ThemeMenuValidation {
    private static int index,phase,ticks,total;
    private static boolean done;
    private static final Theme[] THEMES={Theme.BASALT_COPPER,Theme.FIELD_JOURNAL,Theme.RAWHIDE_FLINT,
        Theme.RIVERSTONE_MOSS,Theme.TIMBER_IRON,Theme.CLAY_EMBER,Theme.OBSIDIAN_ASH,Theme.FROST_SLATE,Theme.VANILLA};
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if(System.getenv("DEEPPRIMAL_MENU_VALIDATION")==null||done)return;
        var mc=Minecraft.getInstance();
        try {
            if(++total>6000)throw new IllegalStateException("Menu validation timeout phase="+phase+" theme="+index);
            if(mc.getOverlay()!=null)return;
            if(++ticks<25)return;
            if(phase==0){
                mc.options.onboardingAccessibilityFinished();mc.options.guiScale().set(2);mc.resizeGui();
                ThemeConfig.THEME.set(THEMES[index]);ThemeConfig.SPEC.save();mc.setScreen(new TitleScreen());phase=1;ticks=0;
            }else if(phase==1&&!ThemeMod.reloading()&&ThemeMod.active()==THEMES[index]){
                if(THEMES[index].custom()){
                    if(!(mc.screen instanceof ThemeTitleScreen))throw new IllegalStateException("Missing themed title");
                    try(var stream=mc.getResourceManager().getResource(ThemeMenuArt.texture(THEMES[index])).orElseThrow().open();
                        var image=NativeImage.read(stream)){
                        if(image.getWidth()!=3840||image.getHeight()!=2160)throw new IllegalStateException("Wrong texture dimensions");
                    }
                    for(var child:mc.screen.children())if(child instanceof AbstractButton b){
                        if(b.getMessage().getString().equals("Continue"))throw new IllegalStateException("Continue still present");
                        if(b.getX()<0||b.getY()<0||b.getX()+b.getWidth()>mc.screen.width||b.getY()+b.getHeight()>mc.screen.height)
                            throw new IllegalStateException("Button out of bounds: "+b.getMessage().getString());
                    }
                    for(int[] size:new int[][]{{320,240},{427,240},{640,360},{960,540},{1280,540}}){
                        int bottom=-1;
                        for(int n=0;n<4;n++){
                            var b=ThemeMenuArt.button(THEMES[index],n,size[0],size[1]);
                            if(b.x()<0||b.y()<bottom||b.x()+b.w()>size[0]||b.y()+b.h()>ThemeMenuArt.footerButton(0,5,size[0],size[1]).y()-2)
                                throw new IllegalStateException("Invalid scaled geometry");
                            bottom=b.y()+b.h();
                        }
                    }
                }else if(mc.screen.getClass()!=TitleScreen.class)throw new IllegalStateException("Vanilla not restored");
                Screenshot.grab(mc.gameDirectory,"alpha7-"+THEMES[index].id()+"-menu.png",mc.getMainRenderTarget(),1,c->{});
                phase=5;ticks=0;
            }else if(phase==5){
                mc.options.guiScale().set(1);mc.resizeGui();phase=6;ticks=0;
            }else if(phase==6){
                Screenshot.grab(mc.gameDirectory,"alpha7-"+THEMES[index].id()+"-menu-scale1.png",mc.getMainRenderTarget(),1,c->{});
                mc.options.guiScale().set(2);mc.resizeGui();phase=2;ticks=0;
            }else if(phase==2){
                press("menu.singleplayer");
                if(!(mc.screen instanceof SelectWorldScreen))throw new IllegalStateException("Singleplayer did not open world list");
                mc.setScreen(new TitleScreen());phase=3;ticks=0;
            }else if(phase==3){
                press("menu.options");
                if(!(mc.screen instanceof OptionsScreen))throw new IllegalStateException("Options did not open");
                mc.setScreen(new TitleScreen());phase=4;ticks=0;
            }else if(phase==4){
                System.out.println("MENU_VALIDATION_PASS: "+THEMES[index]);
                if(++index==THEMES.length){
                    Files.writeString(Path.of(mc.gameDirectory.toString(),"alpha7-menu-validation-success.txt"),
                        "PASS: all eight 3840x2160 textures loaded; no Continue; menu bounds across five GUI dimensions; Singleplayer and Options navigation for all eight themes and Vanilla; Vanilla title restored.\n");
                    done=true;mc.stop();
                }else{phase=0;ticks=0;}
            }
        }catch(Throwable failure){
            done=true;failure.printStackTrace();
            try{Files.writeString(Path.of(mc.gameDirectory.toString(),"alpha7-menu-validation-failure.txt"),failure.toString());}catch(Exception ignored){}
            mc.stop();
        }
    }
    private static void press(String key){
        var mc=Minecraft.getInstance();String label=Component.translatable(key).getString();
        var button=mc.screen.children().stream().filter(c->c instanceof AbstractButton)
            .map(c->(AbstractButton)c).filter(b->b.getMessage().getString().equals(label)).findFirst().orElseThrow();
        if(!button.active)throw new IllegalStateException("Inactive menu action "+key);
        button.onPress(null);
    }
}
