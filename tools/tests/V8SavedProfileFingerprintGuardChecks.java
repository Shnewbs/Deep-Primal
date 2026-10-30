package com.shnewbs.deepprimal.engine;

/** Focused source guard for the V8 saved-profile reopen identity gate. */
public final class V8SavedProfileFingerprintGuardChecks {
  private static void need(boolean ok, String message) {
    if (!ok) throw new AssertionError(message);
  }

  public static void main(String[] args) {
    String source = System.getProperty("v8.snapshot.source", "");
    need(source.contains("savedProfileFingerprint"), "fingerprint method missing");
    need(source.contains("requireSavedProfileFingerprint"), "prepared guard missing");
    need(source.contains("V8 saved profile fingerprint mismatch"), "fail-closed mismatch missing");
    need(source.contains("vegetationPolicy == null ? 0 : vegetationPolicy.hashCode()"),
        "latest saved policy not included in identity");
    System.out.println("V8_SAVED_PROFILE_FINGERPRINT_GUARD_PASS");
  }
}
