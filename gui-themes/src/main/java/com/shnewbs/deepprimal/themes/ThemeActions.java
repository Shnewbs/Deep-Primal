package com.shnewbs.deepprimal.themes;

import com.shnewbs.deepprimal.themes.api.ThemeIntegration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

final class ThemeActions {
    static final KeyMapping.Category CATEGORY=new KeyMapping.Category(Identifier.fromNamespaceAndPath(ThemeMod.ID,"themes"));
    static final KeyMapping JOURNAL=new KeyMapping("key.deepprimal_themes.journal",74,CATEGORY);
    static final KeyMapping HEARTH=new KeyMapping("key.deepprimal_themes.hearth",72,CATEGORY);
    static void register(RegisterKeyMappingsEvent e){e.registerCategory(CATEGORY);e.register(JOURNAL);e.register(HEARTH);}
    static void tick(){
        var mc=Minecraft.getInstance();
        while(JOURNAL.consumeClick())if(ThemeMod.active().custom()&&mc.player!=null&&mc.screen==null)journal(null);
        while(HEARTH.consumeClick())if(ThemeMod.active().custom()&&mc.player!=null&&mc.screen==null)hearth();
    }
    static void journal(Screen parent){
        var action=ThemeIntegration.provider().journal();
        if(action.isPresent()){if(action.get().enabled())action.get().invoke().run();}
        else Minecraft.getInstance().setScreen(new ThemeJournalScreen(parent));
    }
    static void hearth(){ThemeIntegration.provider().hearth().filter(ThemeIntegration.Action::enabled).ifPresent(a->a.invoke().run());}
}
