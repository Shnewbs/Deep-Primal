package net.vulkanmod.deepprimal.upscaling;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.vulkanmod.render.engine.VkTextureView;
import net.vulkanmod.vulkan.Renderer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import static org.lwjgl.vulkan.VK10.*;

/** Real Vulkan spatial presentation. This is linear filtering, not FSR, DLSS or frame generation. */
public final class UpscalingPass {
  public static void present(GpuTextureView texture){
    RenderSystem.assertOnRenderThread();
    if(!(texture instanceof VkTextureView source))throw new IllegalArgumentException("Vulkan texture required");
    Renderer renderer=Renderer.getInstance();renderer.endRenderPass();
    var src=source.texture().getVulkanImage();var dst=renderer.getSwapChain().getColorAttachment();
    try(var stack=MemoryStack.stackPush()){
      var command=Renderer.getCommandBuffer();src.transitionImageLayout(stack,command,VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL);dst.transitionImageLayout(stack,command,VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
      var region=VkImageBlit.calloc(1,stack);
      region.srcSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).mipLevel(source.baseMipLevel()).baseArrayLayer(0).layerCount(1);
      region.dstSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).mipLevel(0).baseArrayLayer(0).layerCount(1);
      region.srcOffsets(0).set(0,0,0);region.srcOffsets(1).set(source.getWidth(0),source.getHeight(0),1);
      region.dstOffsets(0).set(0,0,0);region.dstOffsets(1).set(renderer.getSwapChain().getWidth(),renderer.getSwapChain().getHeight(),1);
      vkCmdBlitImage(command,src.getId(),VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,dst.getId(),VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,region,VK_FILTER_LINEAR);
      dst.transitionImageLayout(stack,command,KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);
    }
  }
  private UpscalingPass(){}
}
