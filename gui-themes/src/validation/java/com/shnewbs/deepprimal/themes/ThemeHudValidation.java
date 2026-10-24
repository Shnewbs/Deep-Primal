package com.shnewbs.deepprimal.themes;

import com.shnewbs.deepprimal.themes.api.ThemeIntegration;
import java.nio.file.*;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.effect.*;
import net.minecraft.network.chat.Component;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.lwjgl.glfw.GLFW;

/** Opt-in live input/layout regression checks; never included in the release JAR. */
@EventBusSubscriber(modid=ThemeMod.ID,value=Dist.CLIENT)
public final class ThemeHudValidation {
    private static final ThemeIntegration.Provider PROVIDER=()->Optional.of(new ThemeIntegration.SurvivalState(.7f,7,.55f));
    private static int phase,ticks,total;
    private static boolean done;
    private static double savedX,savedY;
    private static ThemeHudEditorScreen editor;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if(System.getenv("DEEPPRIMAL_HUD_VALIDATION")==null||done)return;
        var mc=Minecraft.getInstance();
        try {
            if(++total>3000)throw new IllegalStateException("HUD validation timeout phase "+phase);
            if(mc.getOverlay()!=null||++ticks<20)return;
            switch(phase) {
                case 0 -> {
                    Files.deleteIfExists(Path.of(mc.gameDirectory.toString(),"alpha7-hud-validation-failure.txt"));
                    Files.deleteIfExists(Path.of(mc.gameDirectory.toString(),"alpha7-hud-validation-success.txt"));
                    mc.options.onboardingAccessibilityFinished();mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);mc.options.guiScale().set(2);mc.resizeGui();
                    ThemeConfig.THEME.set(Theme.RIVERSTONE_MOSS);ThemeConfig.DAY_X.set(1.0);ThemeConfig.DAY_Y.set(0.0);ThemeConfig.SPEC.save();
                    mc.setScreen(new TitleScreen());next();
                }
                case 1 -> {
                    if(ThemeMod.reloading()||ThemeMod.active()!=Theme.RIVERSTONE_MOSS)return;
                    mc.createWorldOpenFlows().createFreshLevel("hud-validation-"+System.currentTimeMillis(),
                        new LevelSettings("HUD validation",GameType.SURVIVAL,new LevelSettings.DifficultySettings(Difficulty.PEACEFUL,false,false),true,WorldDataConfiguration.DEFAULT),
                        new WorldOptions(42L,false,false),WorldPresets::createFlatWorldDimensions,new TitleScreen());next();
                }
                case 2 -> {
                    if(mc.player==null||mc.level==null)return;
                    mc.setScreen(null);ThemeIntegration.register(PROVIDER);next();
                }
                case 3 -> {capture("riverstone-hud");mc.setScreen(new ThemeScreen(null));button(mc.screen,"Done").setFGColor(0xffff00ff);next();}
                case 4 -> {
                    capture("themes");
                    require(((com.shnewbs.deepprimal.themes.mixin.ThemeWidgetAccessor)button(mc.screen,"Apply theme")).deepprimalThemes$foreground()==Button.UNSET_FG_COLOR,"Theming mutated native button foreground");
                    require(button(mc.screen,"Done").getFGColor()==0xffff00ff,"Explicit button foreground overwritten");button(mc.screen,"Done").clearFGColor();
                    press(mc.screen,"HUD layout");
                    require(mc.screen instanceof ThemeHudEditorScreen,"HUD layout entry point");editor=(ThemeHudEditorScreen)mc.screen;
                    var badge=badge();drag(badge.getX()+4,badge.getY()+4,70,54);
                    editor.keyPressed(new KeyEvent(GLFW.GLFW_KEY_RIGHT,0,GLFW.GLFW_MOD_SHIFT));
                    require(badge.getX()==76&&badge.getY()==50,"Drag offset and keyboard movement");
                    require(ThemeConfig.DAY_X.get()==1.0,"Draft changed config before Save");next();
                }
                case 5 -> {
                    capture("editor-moved");press(editor,"Save");savedX=ThemeConfig.DAY_X.get();savedY=ThemeConfig.DAY_Y.get();
                    require(savedX>0&&savedX<1&&savedY>0&&savedY<1,"Save layout");
                    String config=Files.readString(Path.of(mc.gameDirectory.toString(),"config","deepprimal_themes-client.toml"));
                    require(config.contains("dayX = "+savedX)&&config.contains("dayY = "+savedY),"Layout written to disk");
                    mc.setScreen(null);next();
                }
                case 6 -> {
                    capture("saved-position");editor=new ThemeHudEditorScreen(null);mc.setScreen(editor);
                    require(badge().getX()==76&&badge().getY()==50,"Reopen uses saved layout");
                    press(editor,"Reset");require(badge().getY()==6,"Reset preview");
                    editor.keyPressed(new KeyEvent(GLFW.GLFW_KEY_ESCAPE,0,0));
                    require(ThemeConfig.DAY_X.get()==savedX&&ThemeConfig.DAY_Y.get()==savedY,"Escape cancels reset");
                    editor=new ThemeHudEditorScreen(null);mc.setScreen(editor);
                    drag(badge().getX()+2,badge().getY()+2,-200,-200);
                    require(badge().getX()==6&&badge().getY()==6,"Clamp upper-left");
                    press(editor,"Cancel");require(ThemeConfig.DAY_X.get()==savedX,"Cancel discards drag");
                    editor=new ThemeHudEditorScreen(null);mc.setScreen(editor);
                    mc.options.guiScale().set(1);mc.resizeGui();
                    var expected=ThemeDayBadge.bounds(editor.width,editor.height,savedX,savedY);
                    require(badge().getX()==expected.x()&&badge().getY()==expected.y(),"Relative layout on resize");
                    drag(badge().getX()+2,badge().getY()+2,5000,5000);
                    require(badge().getX()+badge().getWidth()==editor.width-6&&badge().getY()+badge().getHeight()==editor.height-6,"Clamp bottom-right");
                    next();
                }
                case 7 -> {
                    capture("editor-wide-edge");
                    var save=button(editor,"Save");
                    drag(badge().getX()+2,badge().getY()+2,save.getX()+save.getWidth()/2-20,save.getY()+2);
                    press(editor,"Save");require(mc.screen==null,"Badge overlapping Save intercepted toolbar input");
                    editor=new ThemeHudEditorScreen(null);mc.setScreen(editor);
                    press(editor,"Reset");press(editor,"Save");
                    require(ThemeConfig.DAY_X.get()==1.0&&ThemeConfig.DAY_Y.get()==0.0,"Save reset");
                    mc.options.guiScale().set(2);mc.resizeGui();ThemeIntegration.unregister(PROVIDER);next();
                }
                case 8 -> {
                    capture("companion-without-provider");ThemeIntegration.register(PROVIDER);
                    ThemeConfig.THEME.set(Theme.FIELD_JOURNAL);ThemeConfig.SPEC.save();next();
                }
                case 9 -> {
                    if(ThemeMod.reloading()||ThemeMod.active()!=Theme.FIELD_JOURNAL)return;
                    capture("field-journal-hud");ThemeConfig.THEME.set(Theme.VANILLA);ThemeConfig.SPEC.save();next();
                }
                case 10 -> {
                    if(ThemeMod.reloading()||ThemeMod.active()!=Theme.VANILLA)return;
                    capture("vanilla-hud");mc.setScreen(new ThemeScreen(null));
                    require(!button(mc.screen,"HUD layout").active,"Vanilla editor disabled");
                    mc.setScreen(new InventoryScreen(mc.player));ThemeConfig.THEME.set(Theme.BASALT_COPPER);ThemeConfig.SPEC.save();next();
                }
                case 11 -> {
                    if(ThemeMod.reloading()||ThemeMod.active()!=Theme.BASALT_COPPER)return;
                    require(mc.screen instanceof ThemeInventoryScreen,"Live switch failed to rebuild inventory");
                    button(mc.screen,"Crafting").onPress(null);next();
                }
                case 12 -> {
                    require(ThemeRecipes.narrowVisible(mc.screen),"Narrow book missing");
                    var back=button(mc.screen,"Back");require(back.active&&back.visible,"Recipe Back unavailable");
                    mc.screen.setFocused(back);mc.screen.keyPressed(new KeyEvent(GLFW.GLFW_KEY_ENTER,0,0));
                    require(!ThemeRecipes.narrowVisible(mc.screen),"Recipe Back keyboard action");
                    mc.setScreen(new FurnaceScreen(new FurnaceMenu(4,mc.player.getInventory()),mc.player.getInventory(),Component.literal("Furnace")));
                    require(mc.screen instanceof ThemeFurnaceScreen,"Furnace replacement");
                    ThemeConfig.THEME.set(Theme.VANILLA);ThemeConfig.SPEC.save();next();
                }
                case 13 -> {
                    if(ThemeMod.reloading()||ThemeMod.active()!=Theme.VANILLA)return;
                    require(mc.screen.getClass()==FurnaceScreen.class,"Live switch left a custom furnace open");
                    var slot=((FurnaceScreen)mc.screen).getMenu().getSlot(0);
                    require(slot.x==56&&slot.y==17,"Furnace slot restoration");
                    ThemeConfig.THEME.set(Theme.BASALT_COPPER);ThemeConfig.SPEC.save();next();
                }
                case 14 -> {
                    if(ThemeMod.reloading()||ThemeMod.active()!=Theme.BASALT_COPPER)return;
                    require(mc.screen instanceof ThemeFurnaceScreen,"Live switch to custom furnace");mc.setScreen(null);
                    mc.player.getInventory().setSelectedSlot(8);mc.player.setHealth(.5f);mc.player.setAbsorptionAmount(4);
                    mc.player.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.SHIELD));
                    mc.player.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.IRON_HELMET));
                    mc.player.getFoodData().setFoodLevel(1);mc.player.setAirSupply(100);
                    mc.player.addEffect(new MobEffectInstance(MobEffects.HUNGER,200));
                    var server=mc.getSingleplayerServer();var uuid=mc.player.getUUID();
                    server.execute(()->{var p=server.getPlayerList().getPlayer(uuid);p.setHealth(.5f);p.setAbsorptionAmount(4);p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.IRON_HELMET));p.addEffect(new MobEffectInstance(MobEffects.HUNGER,200));});
                    next();
                }
                case 15 -> {
                    capture("critical-health-offhand-last-slot");
                    require(ThemeDayBadge.bounds(mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight(),1,0).y()>=58,"Day badge covers effect icons");
                    ThemeHud.hint(true,mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight()).ifPresent(h->require(h.x()+h.width()<mc.getWindow().getGuiScaledWidth()/2-120,"Journal covers offhand"));
                    mc.player.setHealth(20);mc.player.setAbsorptionAmount(0);mc.player.setAirSupply(300);mc.player.getFoodData().setFoodLevel(20);
                    mc.player.removeAllEffects();mc.player.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);mc.player.setItemSlot(EquipmentSlot.HEAD,ItemStack.EMPTY);
                    ThemeIntegration.unregister(PROVIDER);next();
                }
                case 16 -> {
                    capture("last-slot-border");
                    mc.setScreen(new SmithingScreen(new SmithingMenu(5,mc.player.getInventory()),mc.player.getInventory(),Component.literal("Smithing")));next();
                }
                case 17 -> {
                    capture("smithing-guides");
                    mc.setScreen(new AnvilScreen(new AnvilMenu(6,mc.player.getInventory()),mc.player.getInventory(),Component.literal("Anvil")));next();
                }
                case 18 -> {
                    capture("anvil-guides");ThemeConfig.THEME.set(Theme.CLAY_EMBER);ThemeConfig.SPEC.save();mc.setScreen(new TitleScreen());next();
                }
                case 19 -> {
                    if(ThemeMod.reloading()||ThemeMod.active()!=Theme.CLAY_EMBER)return;
                    capture("clay-menu-text");mc.options.guiScale().set(1);mc.resizeGui();next();
                }
                case 20 -> {
                    capture("clay-menu-text-scale1");
                    Files.writeString(Path.of(mc.gameDirectory.toString(),"alpha7-hud-validation-success.txt"),
                        "PASS: real mouse drag, keyboard nudging, Save to disk, reopen, Reset, Cancel, Escape, edge bounds, GUI scale resize, toolbar overlap, native foreground state, recipe Back keyboard activation, live inventory/furnace switching, slot restoration, critical health/offhand/armor captures, and specialized guides. Companion presence uses a dev stub.\n");
                    ThemeIntegration.unregister(PROVIDER);done=true;mc.stop();
                }
            }
        }catch(Throwable error){
            done=true;error.printStackTrace();
            try{Files.writeString(Path.of(mc.gameDirectory.toString(),"alpha7-hud-validation-failure.txt"),error.toString());}catch(Exception ignored){}
            mc.stop();
        }
    }
    private static void next(){phase++;ticks=0;}
    private static void require(boolean condition,String message){if(!condition)throw new IllegalStateException(message);}
    private static Button button(Screen screen,String name){return screen.children().stream().filter(c->c instanceof Button b&&b.getMessage().getString().equals(name)).map(c->(Button)c).findFirst().orElseThrow();}
    private static Button badge(){return (Button)editor.children().getFirst();}
    private static MouseButtonEvent mouse(double x,double y){return new MouseButtonEvent(x,y,new MouseButtonInfo(0,0));}
    private static void press(Screen screen,String name){var b=button(screen,name);screen.mouseClicked(mouse(b.getX()+b.getWidth()/2,b.getY()+b.getHeight()/2),false);screen.mouseReleased(mouse(b.getX()+b.getWidth()/2,b.getY()+b.getHeight()/2));}
    private static void drag(double x,double y,double toX,double toY){editor.mouseClicked(mouse(x,y),false);editor.mouseDragged(mouse(toX,toY),toX-x,toY-y);editor.mouseReleased(mouse(toX,toY));}
    private static void capture(String name){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,"alpha7-"+name+".png",mc.getMainRenderTarget(),1,c->System.out.println("HUD_CAPTURE: "+name));}
}
