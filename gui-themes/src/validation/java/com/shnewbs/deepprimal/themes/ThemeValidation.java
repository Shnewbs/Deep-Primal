package com.shnewbs.deepprimal.themes;

import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import com.shnewbs.deepprimal.themes.api.ThemeIntegration;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Development-only, creates a disposable flat world and captures each actual inventory. */
@EventBusSubscriber(modid = ThemeMod.ID, value = Dist.CLIENT)
public final class ThemeValidation {
    private static final Theme[] THEMES = {Theme.BASALT_COPPER, Theme.FIELD_JOURNAL, Theme.RAWHIDE_FLINT,
        Theme.RIVERSTONE_MOSS, Theme.TIMBER_IRON, Theme.CLAY_EMBER, Theme.OBSIDIAN_ASH, Theme.FROST_SLATE, Theme.VANILLA};
    private static int phase, ticks, themeIndex, total;
    private static boolean failed;
    private static int extraIndex;
    private static final String[] EXTRA={"anvil","smithing","enchanting","brewing","hopper","stonecutter","cartography","grindstone","smoker","blast-furnace"};
    private static int[][] nativePositions;
    private static final ThemeIntegration.Provider TEST_PROVIDER=()->Optional.of(new ThemeIntegration.SurvivalState(0.7f,7,0.55f));
    private static void verifySlots() {
        var mc=Minecraft.getInstance();var menu=mc.player.inventoryMenu;
        if(THEMES[themeIndex].custom()) {
            if(!(mc.screen instanceof ThemeInventoryScreen))throw new IllegalStateException("Custom inventory not installed");
            var seen=new java.util.HashSet<String>();
            for(var s:menu.slots){if(s.x<0||s.y<0||s.x+16>260||s.y+16>210||!seen.add(s.x+","+s.y))throw new IllegalStateException("Invalid slot "+s.index);}
            if(menu.slots.size()!=46)throw new IllegalStateException("Changed inventory slot count");
        }else for(int i=0;i<46;i++)if(menu.getSlot(i).x!=nativePositions[i][0]||menu.getSlot(i).y!=nativePositions[i][1])throw new IllegalStateException("Vanilla slot not restored "+i);
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post e) {
        if(System.getenv("DEEPPRIMAL_MENU_VALIDATION")!=null||System.getenv("DEEPPRIMAL_HUD_VALIDATION")!=null||System.getenv("DEEPPRIMAL_JOURNAL_VALIDATION")!=null)return;
        if (failed) return;
        var mc = Minecraft.getInstance();
        try {
            if (++total > 12000) throw new IllegalStateException("Client harness timed out at phase " + phase);
            ticks++;
            if (mc.getOverlay() != null) return;
            if (phase == 0 && ticks > 80) {
                mc.options.onboardingAccessibilityFinished();
                ThemeConfig.THEME.set(Theme.BASALT_COPPER); ThemeConfig.SPEC.save();
                mc.setScreen(new TitleScreen()); phase=1; ticks=0;
            } else if (phase == 1 && ticks > 80 && !ThemeMod.reloading() && ThemeMod.active() == Theme.BASALT_COPPER) {
                capture("00-title.png");
                mc.createWorldOpenFlows().createFreshLevel("theme-validation-" + System.currentTimeMillis(),
                    new LevelSettings("Theme validation", GameType.SURVIVAL,
                        new LevelSettings.DifficultySettings(Difficulty.PEACEFUL, false, false), true, WorldDataConfiguration.DEFAULT),
                    new WorldOptions(42L, false, false), WorldPresets::createFlatWorldDimensions, new TitleScreen());
                phase=2; ticks=0;
            } else if (phase == 2 && mc.player != null && mc.level != null && ticks > 120) {
                mc.setScreen(null);
                nativePositions=new int[46][2];for(int i=0;i<46;i++){nativePositions[i][0]=mc.player.inventoryMenu.getSlot(i).x;nativePositions[i][1]=mc.player.inventoryMenu.getSlot(i).y;}
                var stacks=new ItemStack[]{new ItemStack(Items.COBBLESTONE,32),new ItemStack(Items.OAK_LOG,16),new ItemStack(Items.STICK,8),new ItemStack(Items.FLINT,6),new ItemStack(Items.BONE,4),new ItemStack(Items.SWEET_BERRIES,12),new ItemStack(Items.COD,3),new ItemStack(Items.RAW_COPPER,5),new ItemStack(Items.COAL,8)};
                for(int i=0;i<9;i++)mc.player.getInventory().setItem(i+9,stacks[i]);
                mc.player.getInventory().setItem(0,new ItemStack(Items.STONE_AXE));mc.player.getInventory().setItem(1,new ItemStack(Items.STONE_PICKAXE));mc.player.getInventory().setItem(4,new ItemStack(Items.TORCH,16));
                ThemeIntegration.register(TEST_PROVIDER);
                phase=3; ticks=0;
            } else if (phase == 3) {
                if (themeIndex == THEMES.length) {
                    Files.writeString(Path.of(mc.gameDirectory.toString(), "alpha7-theme-validation-success.txt"),
                        "PASS: eight themes and Vanilla; resource origins, 46 slot IDs, unique coordinates, bounds and Vanilla coordinate restoration verified; title/inventory/workstation/crafting/HUD captures.\n");
                    mc.stop(); failed=true; return;
                }
                ThemeConfig.THEME.set(THEMES[themeIndex]); ThemeConfig.SPEC.save(); phase=4; ticks=0;
            } else if (phase == 4 && ticks > 30 && !ThemeMod.reloading() && ThemeMod.active() == THEMES[themeIndex]) {
                String actual = mc.getResourceManager().getResource(Identifier.withDefaultNamespace("textures/gui/container/inventory.png")).orElseThrow().sourcePackId();
                String expected = THEMES[themeIndex].custom() ? "deepprimal_themes/" + THEMES[themeIndex].id() : "vanilla";
                if (!actual.equals(expected)) throw new IllegalStateException("Expected pack " + expected + ", got " + actual);
                mc.setScreen(new TitleScreen()); phase=7; ticks=0;
            } else if (phase == 5 && ticks > 12) {
                verifySlots();
                capture(String.format("alpha7-%02d-%s-inventory.png", themeIndex+1, THEMES[themeIndex].id()));
                mc.setScreen(new ThemeToastValidationScreen()); phase=17; ticks=0;
            } else if (phase == 17 && ticks > 12) {
                capture(String.format("alpha7-%02d-%s-notifications.png", themeIndex+1, THEMES[themeIndex].id()));
                mc.setScreen(new ThemeScreen(null)); phase=6; ticks=0;
            } else if (phase == 6 && ticks > 12) {
                capture(String.format("alpha7-%02d-%s-selector.png", themeIndex+1, THEMES[themeIndex].id()));
                mc.setScreen(new FurnaceScreen(new FurnaceMenu(1,mc.player.getInventory()),mc.player.getInventory(),Component.literal("Furnace")));phase=8;ticks=0;
            } else if(phase==7&&ticks>15){
                capture(String.format("alpha7-%02d-%s-title.png",themeIndex+1,THEMES[themeIndex].id()));
                mc.setScreen(new InventoryScreen(mc.player));phase=5;ticks=0;
            } else if(phase==8&&ticks>12){
                capture(String.format("alpha7-%02d-%s-furnace.png",themeIndex+1,THEMES[themeIndex].id()));
                mc.setScreen(new CraftingScreen(new CraftingMenu(2,mc.player.getInventory()),mc.player.getInventory(),Component.literal("Crafting")));phase=9;ticks=0;
            } else if(phase==9&&ticks>12){
                capture(String.format("alpha7-%02d-%s-crafting.png",themeIndex+1,THEMES[themeIndex].id()));
                mc.setScreen(null);phase=10;ticks=0;
            } else if(phase==10&&ticks>12){
                capture(String.format("alpha7-%02d-%s-hud.png",themeIndex+1,THEMES[themeIndex].id()));
                if(themeIndex==0){ThemeIntegration.unregister(TEST_PROVIDER);phase=15;ticks=0;}
                else{mc.setScreen(new ContainerScreen(ChestMenu.sixRows(3,mc.player.getInventory()),mc.player.getInventory(),Component.literal("Large Chest")));phase=11;ticks=0;}
            } else if(phase==15&&ticks>12){
                capture("alpha7-standalone-survival-hud.png");mc.gameMode.setLocalMode(GameType.CREATIVE);phase=16;ticks=0;
            } else if(phase==16&&ticks>12){
                capture("alpha7-standalone-creative-hud.png");mc.gameMode.setLocalMode(GameType.SURVIVAL);ThemeIntegration.register(TEST_PROVIDER);
                mc.setScreen(new ContainerScreen(ChestMenu.sixRows(3,mc.player.getInventory()),mc.player.getInventory(),Component.literal("Large Chest")));phase=11;ticks=0;
            } else if(phase==11&&ticks>12){
                capture(String.format("alpha7-%02d-%s-chest.png",themeIndex+1,THEMES[themeIndex].id()));
                mc.gameMode.setLocalMode(GameType.CREATIVE);
                mc.setScreen(new CreativeModeInventoryScreen(mc.player,mc.player.connection.enabledFeatures(),false));phase=12;ticks=0;
            } else if(phase==12&&ticks>12){
                capture(String.format("alpha7-%02d-%s-creative.png",themeIndex+1,THEMES[themeIndex].id()));
                mc.setScreen(null);mc.gameMode.setLocalMode(GameType.SURVIVAL);
                if(themeIndex==0){
                    mc.setScreen(new InventoryScreen(mc.player));
                    var buttons=mc.screen.children().stream().filter(c->c instanceof ThemeButton).map(c->(ThemeButton)c).toList();
                    buttons.get(1).onPress(null);phase=13;ticks=0;
                }else{themeIndex++;phase=3;ticks=0;}
            } else if(phase==13&&ticks>12){
                capture("alpha7-recipe-book.png");
                if(!ThemeRecipes.narrowVisible(mc.screen))throw new IllegalStateException("Narrow recipe layout not active");
                ThemeRecipes.close(mc.screen);
                if(ThemeRecipes.narrowVisible(mc.screen))throw new IllegalStateException("Recipe return did not close book");
                mc.options.guiScale().set(1);mc.resizeGui();
                ((com.shnewbs.deepprimal.themes.mixin.ThemeRecipeAccessor)mc.screen).deepprimalThemes$recipeBook().toggleVisibility();
                mc.screen.resize(mc.screen.width,mc.screen.height);phase=14;ticks=0;
            } else if(phase==14&&ticks>12){
                if(ThemeRecipes.narrowVisible(mc.screen))throw new IllegalStateException("Wide recipe layout still narrow");
                capture("alpha7-recipe-book-wide.png");
                mc.setScreen(null);mc.player.getRecipeBook().setOpen(net.minecraft.world.inventory.RecipeBookType.CRAFTING,false);
                mc.options.guiScale().set(2);mc.resizeGui();
                phase=18;ticks=0;
            } else if(phase==18&&ticks>12){
                if(extraIndex>0)capture("alpha7-audit-"+EXTRA[extraIndex-1]+".png");
                if(extraIndex==EXTRA.length){mc.setScreen(null);themeIndex++;phase=3;ticks=0;}
                else {mc.setScreen(extra(EXTRA[extraIndex++]));ticks=0;}
            }
        } catch (Throwable error) {
            failed=true; error.printStackTrace();
            try { Files.writeString(Path.of(mc.gameDirectory.toString(), "alpha7-theme-validation-failure.txt"), error.toString()); } catch (Exception ignored) {}
            mc.stop();
        }
    }
    private static net.minecraft.client.gui.screens.Screen extra(String name){
        var inv=Minecraft.getInstance().player.getInventory();var title=Component.literal(name);
        return switch(name){
            case "anvil" -> new AnvilScreen(new AnvilMenu(4,inv),inv,title);
            case "smithing" -> new SmithingScreen(new SmithingMenu(5,inv),inv,title);
            case "enchanting" -> new EnchantmentScreen(new EnchantmentMenu(6,inv),inv,title);
            case "brewing" -> new BrewingStandScreen(new BrewingStandMenu(7,inv),inv,title);
            case "hopper" -> new HopperScreen(new HopperMenu(8,inv),inv,title);
            case "stonecutter" -> new StonecutterScreen(new StonecutterMenu(9,inv),inv,title);
            case "cartography" -> new CartographyTableScreen(new CartographyTableMenu(10,inv),inv,title);
            case "grindstone" -> new GrindstoneScreen(new GrindstoneMenu(11,inv),inv,title);
            case "smoker" -> new SmokerScreen(new SmokerMenu(12,inv),inv,title);
            case "blast-furnace" -> new BlastFurnaceScreen(new BlastFurnaceMenu(13,inv),inv,title);
            default -> throw new IllegalArgumentException(name);
        };
    }
    private static void capture(String filename) {
        var mc=Minecraft.getInstance();
        Screenshot.grab(mc.gameDirectory, filename, mc.getMainRenderTarget(), 1, c -> System.out.println("THEME_CAPTURE: " + filename));
    }
}
