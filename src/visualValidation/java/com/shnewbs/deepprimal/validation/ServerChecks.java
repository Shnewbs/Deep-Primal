package com.shnewbs.deepprimal.validation;

import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Development-only timer inspection; never included in a distributed JAR. */
@Mod(value = "deepprimal", dist = Dist.DEDICATED_SERVER)
public final class ServerChecks {
    public ServerChecks() { NeoForge.EVENT_BUS.register(this); }
    @SubscribeEvent public void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("deepprimaltest")
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(Commands.literal("idle").then(Commands.argument("entity", EntityArgument.entity()).executes(context -> {
                var entity = EntityArgument.getEntity(context, "entity");
                if (!(entity instanceof Mob mob)) return 0;
                context.getSource().sendSuccess(() -> Component.literal("idleTicks=" + mob.getNoActionTime()
                    + " persistent=" + mob.isPersistenceRequired()), false);
                return 1;
            }))));
    }
}
