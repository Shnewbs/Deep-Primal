package com.shnewbs.deepprimal.themes;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.toasts.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.resources.Identifier;

/** Draw actual vanilla toast implementations so Mixin injection and colors are visible. */
final class ThemeToastValidationScreen extends Screen {
    private final List<Toast> toasts;
    ThemeToastValidationScreen() throws ReflectiveOperationException {
        super(Component.literal("Notification contrast: "+ThemeMod.active().displayName));
        var ctor=RecipeToast.class.getDeclaredConstructor();ctor.setAccessible(true);var recipe=ctor.newInstance();
        var add=RecipeToast.class.getDeclaredMethod("addItem",ItemStack.class,ItemStack.class);add.setAccessible(true);
        add.invoke(recipe,new ItemStack(Items.CRAFTING_TABLE),new ItemStack(Items.STONE_AXE));
        var advancement=Advancement.Builder.advancement().display(Items.STONE_AXE,Component.literal("Getting an Upgrade"),Component.literal("Craft a stone tool"),null,AdvancementType.TASK,true,false,false).build(Identifier.fromNamespaceAndPath(ThemeMod.ID,"validation"));
        toasts=List.of(recipe,new TutorialToast(Minecraft.getInstance().font,TutorialToast.Icons.TREE,Component.literal("Gather wood"),Component.literal("Hold attack to break a log"),false),
            SystemToast.multiline(Minecraft.getInstance(),SystemToast.SystemToastId.PERIODIC_NOTIFICATION,Component.literal("System notification"),Component.literal("Settings saved. Your theme is ready.")),new AdvancementToast(advancement));
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
        g.fill(0,0,width,height,0xff30363c);
        g.text(font,title,(width-font.width(title))/2,12,0xffffffff,false);
        int y=34;
        for(var toast:toasts){g.pose().pushMatrix();g.pose().translate((width-toast.width())/2f,y);toast.extractRenderState(g,font,1000L);g.pose().popMatrix();y+=toast.height()+8;}
    }
}
