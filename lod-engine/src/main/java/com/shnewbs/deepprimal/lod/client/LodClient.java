package com.shnewbs.deepprimal.lod.client;

import com.shnewbs.deepprimal.lod.network.LodWorld;
import com.shnewbs.deepprimal.settings.LodAccess;
import com.shnewbs.deepprimal.settings.LodConfig;
import com.shnewbs.deepprimal.settings.RendererOwnership;
import java.util.LinkedHashMap;
import java.util.Map;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = "deepprimal_lod", dist = Dist.CLIENT)
public final class LodClient {
  public LodClient(IEventBus modBus) {
    modBus.addListener(LodClient::payloads);
    LodAccess.install(
        new LodAccess.View() {
          public boolean active() {
            return VoxelLodRenderer.active();
          }

          public int distance() {
            return VoxelLodRenderer.distance();
          }

          public int visibleDistance() {
            return VoxelLodRenderer.visibleDistance();
          }

          public String status() {
            return "Visible LOD span: "
                + visibleDistance()
                + " chunks | requested: "
                + LodConfig.RADIUS.get();
          }

          public Map<String, Object> diagnostics() {
            var lod = VoxelLodRenderer.INSTANCE;
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("lodState", lod.status);
            data.put("lodCaptured", lod.capturedChunks);
            data.put("lodUploaded", lod.uploadedChunks);
            data.put("lodVisible", lod.visibleChunks);
            data.put("lodVisibleSpan", lod.visibleSpan);
            data.put("lodRequestedRadius", LodConfig.RADIUS.get());
            data.put("lodResidentBytes", lod.residentBytes);
            data.put("lodDiskReads", lod.diskReads);
            data.put("lodStaleResults", lod.staleResults);
            data.put("lodQueued", lod.queuedChunks);
            return data;
          }
        });
    if (RendererOwnership.owned()) NeoForge.EVENT_BUS.register(VoxelLodRenderer.INSTANCE);
  }

  private static void payloads(RegisterClientPayloadHandlersEvent event) {
    event.register(LodWorld.TYPE, (p, c) -> VoxelLodRenderer.INSTANCE.worldIdentity(p.identity()));
  }
}
