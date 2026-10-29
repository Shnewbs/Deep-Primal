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
            data.put("lodErrorViewportHeightPixels", lod.lodErrorViewportHeight);
            data.put("lodErrorWorldTargetPresent", lod.lodErrorWorldTargetPresent);
            data.put("lodErrorViewportScope",
                "Opt-in last successful LOD extract: world-target pixel height during spatial/FSR 1 world rendering, otherwise native window height; not GPU time or FPS");
            data.put("lodResidentBytes", lod.residentBytes);
            data.put("lodDiskReads", lod.diskReads);
            data.put("lodStaleResults", lod.staleResults);
            data.put("lodQueued", lod.queuedChunks);
            data.put("lodDiscoveryVisited", lod.discoveryVisited);
            data.put("lodDiscoveryTotal", lod.discoveryTotal);
            data.put("lodDiscoveryComplete", lod.discoveryComplete);
            data.put("lodTelemetryEnabled", VoxelLodRenderer.TELEMETRY);
            data.put("lodTimingDomain", "CPU component elapsed nanoseconds; not GPU or frame time");
            data.put("lodCullTiming", timing(lod.cullTiming.snapshot()));
            data.put("lodUploadTiming", timing(lod.uploadTiming.snapshot()));
            data.put("lodSubmitTiming", timing(lod.submitTiming.snapshot()));
            var submission = lod.submissionStageStats();
            data.put("lodTransformPrepCpuTiming", timing(submission.transformCpuNanos()));
            data.put("lodDrawApiCpuTiming", timing(submission.drawApiCpuNanos()));
            data.put("lodSubmissionTotalCpuTiming", timing(submission.totalCpuNanos()));
            data.put("lodSubmissionCompletePasses", submission.completedPasses());
            data.put("lodSubmissionFailedPasses", submission.failedPasses());
            data.put("lodSubmissionPreDrawFailures", submission.failedBeforeDrawPasses());
            data.put("lodSubmissionReturnedDrawRecords", submission.returnedRecords());
            data.put("lodSubmissionReturnedApiCalls", submission.returnedApiCalls());
            data.put("lodSubmissionStageScope",
                "Opt-in render-thread CPU time including failed transform preparation (zero draw-stage time); draws count API returns, including partial failed passes; not GPU fence, hardware calls or FPS");
            var partUploads = lod.uploadPartStats();
            data.put("lodGpuPartCpuTiming", timing(partUploads.cpuNanos()));
            data.put("lodGpuPartSizeTiming", byteTiming(partUploads.acceptedBytes()));
            data.put("lodGpuPartAccepted", partUploads.acceptedParts());
            data.put("lodGpuPartFailed", partUploads.failedParts());
            data.put("lodGpuPartOverCpuBudget", partUploads.overTimeBudgetParts());
            data.put("lodGpuPartPeakBytes", partUploads.peakPartBytes());
            data.put("lodGpuPartAcceptedTotalBytes", partUploads.acceptedByteTotal());
            data.put("lodGpuPartScope", "Render-thread CPU packing and synchronous GPU-buffer submission for accepted parts; excludes device completion/FPS");
            var pages = lod.uploadPageStats();
            data.put("lodPageAdmissionToCommitWall", timing(pages.wallNanos()));
            data.put("lodPageUploadFrames", frameTiming(pages.uploadFrames()));
            data.put("lodPageCommittedBytes", byteTiming(pages.committedBytes()));
            data.put("lodPageCommitted", pages.committedPages());
            data.put("lodPageStaleDrops", pages.staleDrops());
            data.put("lodPageMemoryDrops", pages.memoryDrops());
            data.put("lodPagePeakUploadFrames", pages.peakUploadFrames());
            data.put("lodPageTimingScope", "Time from LOD render-thread admission to fully committed GPU page; includes frame pacing/pauses, excludes GPU fence completion and total capture/worker time");
            var publication = lod.publicationStats();
            data.put("lodPublicationFreezeCpuTiming", timing(publication.freezeCpuNanos()));
            data.put("lodPublicationMetadataCpuTiming", timing(publication.metadataCpuNanos()));
            data.put("lodPublicationTotalCpuTiming", timing(publication.totalCpuNanos()));
            data.put("lodPublicationPreparedPages", publication.preparedPages());
            data.put("lodPublicationFrozenParts", publication.frozenParts());
            data.put("lodPublicationAdjacentAliasBits", publication.adjacentAliasBits());
            data.put("lodPublicationTwoBackAliasBits", publication.twoBackAliasBits());
            data.put("lodPublicationScope", "Opt-in CPU p50/p95/p99 per completed bounds/mask preparation before resident cache installation; includes all frozen levels and array allocations, excludes upload/GPU execution, cache retirement and full-frame/FPS. Alias counts cover prepared candidates, not necessarily later-visible or committed pages. Compare startup twoBackFrustumReuse=true/false with identical scenes.");
            data.put("lodTestedParts", lod.testedParts);
            data.put("lodRejectedParts", lod.rejectedParts);
            data.put("lodPageBroadphaseEnabled", LodPageFrustumBounds.ENABLED);
            data.put("lodTwoBackFrustumReuseEnabled",
                LodPageFrustumBounds.ENABLED && LodPublishedBounds.TWO_BACK_ENABLED);
            data.put("lodPageAdaptiveAutoSkipEnabled", LodPageFrustumAdaptiveGate.ENABLED);
            data.put("lodPageCoarseSampledLastExtract", lod.coarseAdaptiveSampledLastFrame);
            data.put("lodPageAdaptiveBypassedPages", lod.coarseAdaptiveBypassedPages);
            data.put("lodCoarseTestedPages", lod.coarseTestedPages);
            data.put("lodCoarseVisiblePages", lod.coarseVisiblePages);
            data.put("lodCoarseRejectedPages", lod.coarseRejectedPages);
            data.put("lodCoarseSkippedPartTests", lod.coarseSkippedPartTests);
            data.put("lodCoarseUniformVisiblePages", lod.coarseUniformVisiblePages);
            data.put("lodCoarseUniformVisiblePartTests", lod.coarseUniformVisiblePartTests);
            data.put("lodAdjacentBoundsReusedVisiblePartTests", lod.coarseRunReusedVisibleParts);
            data.put("lodAdjacentBoundsReusedRejectedPartTests", lod.coarseRunReusedRejectedParts);
            data.put("lodTwoBackBoundsReusedVisiblePartTests", lod.coarseTwoBackReusedVisibleParts);
            data.put("lodTwoBackBoundsReusedRejectedPartTests", lod.coarseTwoBackReusedRejectedParts);
            data.put("lodPageCullingScope",
                "Opt-in CPU counters for completed extract: coarse tested = visible + rejected. Avoided fine-part tests include whole-page rejection and positive uniform-bound reuse, separately reported; mixed-bound pages retain ordered fine culling and can reuse adjacent and two-back identical finite AABB verdicts (separate visible/rejected counters, not used to train the adaptive heuristic). Broadphase=false disables page tests and adjacent-run reuse. The independent JVM startup -Ddeepprimal.lod.twoBackFrustumReuse=false disables only two-back aliases and their publication comparisons; default true. AutoSkip=false keeps full coarse testing every extract; opt-in adaptive policy samples whole extracts when coarse checks save at most one fine-part check each (at least 32 coarse pages), bypassing up to 3 extracts. Client restart required; fine visibility decisions and material ordering are preserved. Not GPU occlusion/FPS.");
            data.put("lodSubmittedParts", lod.submittedParts);
            data.put("lodIndexedApiBatches", lod.submittedApiBatches);
            data.put("lodIndexedApiBatchScope",
                "Synchronous drawMultipleIndexed API calls, not GPU multi-draw indirect or hardware draw-call count");
            data.put("lodLastUploadBytes", lod.lastUploadBytes);
            data.put("lodOversizedFirstUploads", lod.oversizedFirstUploads);
            data.put("lodOversizedFirstUploadBytes", lod.oversizedFirstUploadBytes);
            data.put("lodUploadBudgetSemantics",
                "Byte/time limits are soft per frame: one indivisible part may exceed them; retained GPU-memory ceiling remains hard");
            data.put("lodPendingMeshGpuBudgetBytes", lod.pendingMeshBudgetBytes);
            var transform = lod.transformStats();
            data.put("lodTransformScope",
                "Successful Java transform-array requests since world reset; not heap bytes, GPU timing or FPS");
            data.put("lodTransformRequests", transform.requests());
            data.put("lodTransformCacheHits", transform.hits());
            data.put("lodTransformFactoryArrays", transform.allocations());
            data.put("lodTransformOversizedBypasses", transform.uncachedOversized());
            data.put("lodTransformEvictions", transform.evictions());
            data.put("lodTransformRetainedElements", transform.retainedElements());
            data.put("lodTransformRetainedArrays", transform.retainedArrays());
            data.put("lodTransformPeakRetainedElements", transform.peakRetainedElements());
            data.put("lodTransformLastDrawCount", transform.lastDrawCount());
            data.put("lodTransformPeakDrawCount", transform.peakDrawCount());
            data.put("lodTransformCount0to512", transform.count0to512());
            data.put("lodTransformCount513to2048", transform.count513to2048());
            data.put("lodTransformCount2049to8192", transform.count2049to8192());
            data.put("lodTransformCount8193to32768", transform.count8193to32768());
            data.put("lodTransformCountOver32768", transform.countOver32768());
            return data;
          }
        });
    if (RendererOwnership.owned()) NeoForge.EVENT_BUS.register(VoxelLodRenderer.INSTANCE);
  }

  private static Map<String, Object> timing(com.shnewbs.deepprimal.core.RollingTiming.Snapshot s) {
    return Map.of("samples", s.samples(), "p50Nanos", s.p50Nanos(),
        "p95Nanos", s.p95Nanos(), "p99Nanos", s.p99Nanos(), "maxNanos", s.maxNanos());
  }

  private static Map<String, Object> frameTiming(com.shnewbs.deepprimal.core.RollingTiming.Snapshot s) {
    return Map.of("samples", s.samples(), "p50Frames", s.p50Nanos(),
        "p95Frames", s.p95Nanos(), "p99Frames", s.p99Nanos(), "maxFrames", s.maxNanos());
  }

  private static Map<String, Object> byteTiming(com.shnewbs.deepprimal.core.RollingTiming.Snapshot s) {
    return Map.of("samples", s.samples(), "p50Bytes", s.p50Nanos(),
        "p95Bytes", s.p95Nanos(), "p99Bytes", s.p99Nanos(), "maxBytes", s.maxNanos());
  }

  private static void payloads(RegisterClientPayloadHandlersEvent event) {
    event.register(LodWorld.TYPE, (p, c) -> VoxelLodRenderer.INSTANCE.worldIdentity(p.identity()));
  }
}
