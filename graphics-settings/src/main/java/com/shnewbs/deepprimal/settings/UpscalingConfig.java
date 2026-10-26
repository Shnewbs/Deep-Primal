package com.shnewbs.deepprimal.settings;
import com.shnewbs.deepprimal.core.UpscalingPolicy;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Explicit capability-gated resolution scaling. Native resolution is the default. */
public final class UpscalingConfig {
  public static final ModConfigSpec SPEC;
  public static final ModConfigSpec.EnumValue<UpscalingPolicy.Mode> MODE;
  public static final ModConfigSpec.DoubleValue RENDER_SCALE;
  public static final ModConfigSpec.BooleanValue FRAME_GENERATION;
  static {var b=new ModConfigSpec.Builder();
    MODE=b.comment("NONE preserves native rendering. SPATIAL uses Vulkan linear presentation scaling of the entire framebuffer (including UI). FSR/DLSS currently fall back to native because no vendor provider is installed. Restart after changing.").defineEnum("mode",UpscalingPolicy.Mode.NONE);
    RENDER_SCALE=b.comment("Framebuffer scale for SPATIAL. 1 = native; .5 = half width and height. Restart after changing.").defineInRange("renderScale",1.0,.5,1.0);
    FRAME_GENERATION=b.comment("Requires an installed frame-generation provider; unavailable requests fall back to native rendering.").define("frameGeneration",false);SPEC=b.build();
  }
  public static UpscalingPolicy.Mode requestedMode(){return SPEC.isLoaded()?MODE.get():UpscalingPolicy.Mode.NONE;}
  public static UpscalingPolicy.Mode getActiveMode(){return UpscalingPolicy.effective(requestedMode(),RendererPreferences.requestedBackend()==RendererPreferences.Backend.Vulkan,SPEC.isLoaded()&&FRAME_GENERATION.get());}
  public static double activeScale(){return getActiveMode()==UpscalingPolicy.Mode.SPATIAL?RENDER_SCALE.get():1;}
  public static String unavailableReason(){return UpscalingPolicy.unavailableReason(requestedMode(),SPEC.isLoaded()&&FRAME_GENERATION.get());}
  private UpscalingConfig(){}
}
