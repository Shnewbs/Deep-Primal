package com.shnewbs.deepprimal.validation;

import java.io.File;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Opt-in development source set. Excluded from the distributed mod JAR. */
@Mod(value = "deepprimal", dist = Dist.CLIENT)
public final class ClientCapture {
    private long entered;
    private boolean taken;
    public ClientCapture() { NeoForge.EVENT_BUS.register(this); }
    @SubscribeEvent public void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || taken) return;
        mc.options.hideGui = true;
        if (entered == 0) entered = System.nanoTime();
        if (System.nanoTime() - entered < 45_000_000_000L) return;
        taken = true;
        File output = new File(System.getProperty("deepprimal.validationCapture"));
        output.mkdirs();
        Screenshot.grab(output, "terrain.png", mc.getMainRenderTarget(), 1, message -> {
            System.out.println("DEEPPRIMAL_CAPTURE_SAVED " + message.getString());
            mc.execute(mc::stop);
        });
    }
}
