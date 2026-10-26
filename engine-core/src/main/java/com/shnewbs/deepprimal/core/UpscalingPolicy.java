package com.shnewbs.deepprimal.core;
/** Backend-independent capability policy. Vendor algorithms require a real provider. */
public final class UpscalingPolicy {
  public enum Mode { NONE, SPATIAL, FSR, DLSS }
  public static Mode effective(Mode requested,boolean vulkan,boolean frameGeneration){return vulkan&&!frameGeneration&&requested==Mode.SPATIAL?Mode.SPATIAL:Mode.NONE;}
  public static int dimension(int display,double scale){if(display<1||!Double.isFinite(scale)||scale<.5||scale>1)throw new IllegalArgumentException("render scale");return Math.max(1,(int)Math.ceil(display*scale));}
  public static String unavailableReason(Mode requested,boolean frameGeneration){if(frameGeneration)return "Frame generation has no installed provider";return switch(requested){case FSR->"FSR requires a validated FidelityFX provider";case DLSS->"DLSS requires a validated NVIDIA NGX provider";default->"";};}
  private UpscalingPolicy(){}
}
