package com.shnewbs.deepprimal.themes;

import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.lwjgl.glfw.GLFW;

/** Actual game captures and navigation checks, excluded from the release JAR. */
@EventBusSubscriber(modid=ThemeMod.ID,value=Dist.CLIENT)
public final class ThemeJournalValidation {
    private static final Theme[] THEMES={Theme.BASALT_COPPER,Theme.FIELD_JOURNAL,Theme.RAWHIDE_FLINT,
        Theme.RIVERSTONE_MOSS,Theme.TIMBER_IRON,Theme.CLAY_EMBER,Theme.OBSIDIAN_ASH,Theme.FROST_SLATE};
    private static int phase,ticks,total,index;
    private static boolean done;
    private static Screen inventory;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        if(System.getenv("DEEPPRIMAL_JOURNAL_VALIDATION")==null||done)return;
        var mc=Minecraft.getInstance();
        try{
            if(++total>6000)throw new IllegalStateException("Journal validation timeout phase "+phase);
            if(mc.getOverlay()!=null||++ticks<25)return;
            switch(phase){
                case 0 -> {
                    mc.options.onboardingAccessibilityFinished();
                    mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
                    mc.options.guiScale().set(2);mc.resizeGui();
                    ThemeConfig.THEME.set(THEMES[0]);ThemeConfig.SPEC.save();
                    mc.setScreen(new TitleScreen());next();
                }
                case 1 -> {
                    if(ThemeMod.reloading()||ThemeMod.active()!=THEMES[0])return;
                    mc.createWorldOpenFlows().createFreshLevel("journal-validation-"+System.currentTimeMillis(),
                        new LevelSettings("Journal validation",GameType.SURVIVAL,
                            new LevelSettings.DifficultySettings(Difficulty.PEACEFUL,false,false),true,WorldDataConfiguration.DEFAULT),
                        new WorldOptions(42L,false,false),WorldPresets::createFlatWorldDimensions,new TitleScreen());next();
                }
                case 2 -> {
                    if(mc.player==null||mc.level==null)return;
                    mc.setScreen(null);next();
                }
                case 3 -> {
                    if(ThemeMod.reloading()||ThemeMod.active()!=THEMES[index])return;
                    mc.setScreen(new InventoryScreen(mc.player));inventory=mc.screen;
                    require(inventory instanceof ThemeInventoryScreen,"Themed inventory missing");next();
                }
                case 4 -> {capture("inventory");press("Journal");require(mc.screen instanceof ThemeJournalScreen,"Journal tab did not open");next();}
                case 5 -> {require(mc.screen.isInGameUi(),"Journal must use the transparent in-game background");capture("journal-tab");press("Back");require(mc.screen==inventory,"Back did not restore inventory");next();}
                case 6 -> {
                    capture("back");press("Journal");mc.screen.keyPressed(new KeyEvent(GLFW.GLFW_KEY_ESCAPE,0,0));
                    require(mc.screen==inventory,"Escape did not restore inventory");
                    mc.setScreen(null);
                    KeyMapping.click(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_J));next();
                }
                case 7 -> {
                    require(mc.screen instanceof ThemeJournalScreen,"J shortcut did not open Journal");capture("journal-key");
                    mc.screen.keyPressed(new KeyEvent(GLFW.GLFW_KEY_ESCAPE,0,0));require(mc.screen==null,"Shortcut Escape did not return to world");
                    ThemeActions.journal(null);mc.options.guiScale().set(1);mc.resizeGui();next();
                }
                case 8 -> {
                    capture("journal-scale1");press("Back");require(mc.screen==null,"Shortcut Back did not return to world");
                    mc.options.guiScale().set(2);mc.resizeGui();
                    if(++index<THEMES.length){ThemeConfig.THEME.set(THEMES[index]);ThemeConfig.SPEC.save();phase=3;ticks=0;}
                    else next();
                }
                case 9 -> {
                    Files.writeString(Path.of(mc.gameDirectory.toString(),"alpha8-journal-validation-success.txt"),
                        "PASS: all eight themes; inventory Journal mouse click; Back and Escape to inventory; real J key mapping; Back and Escape to world; GUI scales 1 and 2. Actual render captures saved for world visibility review.\n");
                    done=true;mc.stop();
                }
            }
        }catch(Throwable error){done=true;error.printStackTrace();try{Files.writeString(Path.of(mc.gameDirectory.toString(),"alpha8-journal-validation-failure.txt"),error.toString());}catch(Exception ignored){}mc.stop();}
    }
    private static void next(){phase++;ticks=0;}
    private static void require(boolean ok,String message){if(!ok)throw new IllegalStateException(message);}
    private static void press(String label){
        var s=Minecraft.getInstance().screen;
        var b=s.children().stream().filter(c->c instanceof Button v&&v.getMessage().getString().equals(label)).map(c->(Button)c).findFirst().orElseThrow();
        var e=new MouseButtonEvent(b.getX()+b.getWidth()/2,b.getY()+b.getHeight()/2,new MouseButtonInfo(0,0));
        s.mouseClicked(e,false);s.mouseReleased(e);
    }
    private static void capture(String name){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,"alpha8-"+THEMES[index].name().toLowerCase()+"-"+name+".png",mc.getMainRenderTarget(),1,c->System.out.println("JOURNAL_CAPTURE: "+name));}
}
