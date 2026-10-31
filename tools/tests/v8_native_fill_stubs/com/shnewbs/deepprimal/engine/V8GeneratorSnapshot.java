package com.shnewbs.deepprimal.engine;
public record V8GeneratorSnapshot(int version) {
  public static final int BLENDED_VERSION=2, VEGETATION_VERSION=6;
  public static final class Prepared {
    private final V8GeneratorSnapshot snapshot;
    private final long fingerprint;
    public int identityChecks,heightChecks;
    public Prepared(int revision,long fingerprint){snapshot=new V8GeneratorSnapshot(revision);this.fingerprint=fingerprint;}
    public V8GeneratorSnapshot snapshot(){return snapshot;}
    public void requireSavedProfileFingerprint(long expected){identityChecks++;if(expected!=fingerprint)throw new IllegalArgumentException("fingerprint");}
    public void requireHeight(int minY,int height){heightChecks++;if(minY!=-64||height!=384)throw new IllegalArgumentException("height");}
  }
}
