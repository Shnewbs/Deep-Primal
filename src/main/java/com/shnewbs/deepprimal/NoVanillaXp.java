package com.shnewbs.deepprimal;

import net.minecraft.world.entity.ExperienceOrb;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

/** Prototype enforces zero vanilla XP. The future character progression system is not implemented. */
public final class NoVanillaXp {
    @SubscribeEvent
    public void livingDrops(LivingExperienceDropEvent event) { event.setDroppedExperience(0); }

    @SubscribeEvent
    public void blockDrops(BlockDropsEvent event) { event.setDroppedExperience(0); }

    @SubscribeEvent
    public void orb(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ExperienceOrb) event.setCanceled(true);
    }

    @SubscribeEvent
    public void points(PlayerXpEvent.XpChange event) { event.setCanceled(true); }

    @SubscribeEvent
    public void levels(PlayerXpEvent.LevelChange event) { event.setCanceled(true); }

    @SubscribeEvent
    public void tick(ServerTickEvent.Post event) {
        for (var player : event.getServer().getPlayerList().getPlayers()) {
            // Covers loaded old data and direct setters such as /experience set.
            if (player.experienceLevel != 0 || player.totalExperience != 0 || player.experienceProgress != 0) {
                player.experienceLevel = 0;
                player.totalExperience = 0;
                player.experienceProgress = 0;
                player.setExperienceLevels(0);
                player.setExperiencePoints(0);
            }
        }
    }
}
