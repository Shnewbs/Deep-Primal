package com.shnewbs.deepprimal.lod.client;
/** Stand-in for the production broadphase startup switch; no GPU semantics. */
final class LodPageFrustumBounds {
  static final boolean ENABLED = !"false".equalsIgnoreCase(
      System.getProperty("deepprimal.lod.pageFrustumBroadphase", "true"));
}
