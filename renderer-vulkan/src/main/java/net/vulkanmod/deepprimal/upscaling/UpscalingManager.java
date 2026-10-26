package net.vulkanmod.deepprimal.upscaling;
import com.shnewbs.deepprimal.core.UpscalingPolicy;
import com.shnewbs.deepprimal.settings.UpscalingConfig;

/** Resolution policy only: the host owns attachment allocation and resource retirement. */
public final class UpscalingManager {
  private static final UpscalingManager INSTANCE=new UpscalingManager();
  public static UpscalingManager getInstance(){return INSTANCE;}
  public int renderDimension(int display){return UpscalingPolicy.dimension(display,UpscalingConfig.activeScale());}
  public UpscalingPolicy.Mode getCurrentMode(){return UpscalingConfig.getActiveMode();}
  public boolean isUpscalingActive(){return getCurrentMode()==UpscalingPolicy.Mode.SPATIAL&&UpscalingConfig.activeScale()<1;}
  public String unavailableReason(){return UpscalingConfig.unavailableReason();}
  private UpscalingManager(){}
}
