package com.shnewbs.deepprimal.themes;

import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.resource.JarContentsPackResources;

final class ThemePacks {
    static void register(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) return;
        // Evaluate on every repository discovery so old required packs disappear on switching.
        event.addRepositorySource(consumer -> {
            Theme theme = ThemeMod.requestedTheme();
            if (!theme.custom()) return;
            var file = ModList.get().getModFileById(ThemeMod.ID).getFile();
            var location = new PackLocationInfo("deepprimal_themes/" + theme.id(),
                    Component.literal("Deep Primal: " + theme.displayName), PackSource.BUILT_IN, Optional.empty());
            var resources = new JarContentsPackResources.JarContentsResourcesSupplier(file.getContents(), "themes/" + theme.id());
            var pack = Pack.readMetaAndCreate(location, resources, PackType.CLIENT_RESOURCES,
                    new PackSelectionConfig(true, Pack.Position.TOP, true));
            if (pack == null) throw new IllegalStateException("Missing theme pack: " + theme.id());
            consumer.accept(pack);
        });
    }
}
