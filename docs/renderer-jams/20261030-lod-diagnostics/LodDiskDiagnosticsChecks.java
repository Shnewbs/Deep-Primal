package com.shnewbs.deepprimal.lod.client;

final class LodDiskDiagnosticsChecks {
  public static void main(String[] args) {
    LodDiskIndexService service = new LodDiskIndexService();
    LodDiskIndexService.Diagnostics d = service.diagnostics(false, 0);
    require(!d.enabled(), "disabled snapshot");
    require(!d.started(), "fresh service should not be started");
    require(!d.retryScheduled(), "fresh service retry state");
    require(d.queueSize() == 0, "fresh queue size");
    require(d.renewalSize() == 0, "fresh renewal size");
    require(!d.cursorActive(), "fresh cursor state");
    require(!d.cursorCompleted(), "fresh completion state");
    require(!d.missingDirectory(), "fresh missing-directory state");
    require(!d.retiring(), "fresh retiring state");
    require(d.pendingApply() == 0, "fresh pending apply");
    System.out.println("lod diagnostics checks passed");
  }

  private static void require(boolean value, String message) {
    if (!value) throw new AssertionError(message);
  }
}
