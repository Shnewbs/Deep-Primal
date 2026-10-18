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
    private boolean checked;
    private long checkedAt;
    public ClientCapture() { NeoForge.EVENT_BUS.register(this); }
    @SubscribeEvent public void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || taken) return;
        mc.options.hideGui = !Boolean.getBoolean("deepprimal.validationDistances");
        if (entered == 0) entered = System.nanoTime();
        if (System.nanoTime() - entered < 45_000_000_000L) return;
        if (Boolean.getBoolean("deepprimal.validationDistances") && !checked) {
            var center = mc.player.chunkPosition();
            if (!mc.level.getChunkSource().hasChunk(center.x() + 33, center.z())) {
                if (System.nanoTime() - entered > 150_000_000_000L) throw new AssertionError("Chunk at radius 33 was not delivered");
                return;
            }
            try { DistanceClientChecks.run(mc); }
            catch (Exception exception) { throw new RuntimeException(exception); }
            checked = true; checkedAt = System.nanoTime(); return;
        }
        if (Boolean.getBoolean("deepprimal.validationDistances") && System.nanoTime() - checkedAt < 5_000_000_000L) return;
        taken = true;
        File output = new File(System.getProperty("deepprimal.validationCapture"));
        output.mkdirs();
        Screenshot.grab(output, "terrain.png", mc.getMainRenderTarget(), 1, message -> {
            System.out.println("DEEPPRIMAL_CAPTURE_SAVED " + message.getString());
            mc.execute(mc::stop);
        });
    }
}
