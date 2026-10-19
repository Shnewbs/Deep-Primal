// VulkanMod 0.6.8+26.1.2, Collateral and contributors. LGPL-3.0-only.
// Reconstructed from the matching published artifact; modified for Deep Primal on 2026-10-19.
package net.vulkanmod.render.engine;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.GpuFence;
import com.mojang.blaze3d.buffers.GpuBuffer.MappedView;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlProgram;
import net.vulkanmod.gl.VkState;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.opengl.Uniform;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.UniformDescription;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.NativeImage.Format;
import com.mojang.blaze3d.systems.CommandEncoderBackend;
import com.mojang.blaze3d.systems.GpuQuery;
import com.mojang.blaze3d.systems.RenderPassBackend;
import com.mojang.blaze3d.systems.RenderPass.Draw;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat.IndexType;
import com.mojang.logging.LogUtils;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Supplier;
import net.minecraft.util.ARGB;
import net.vulkanmod.gl.VkGlFramebuffer;
import net.vulkanmod.gl.VkGlTexture;
import net.vulkanmod.interfaces.shader.ExtendedRenderPipeline;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.memory.MemoryManager;
import net.vulkanmod.vulkan.Synchronization;
import net.vulkanmod.vulkan.VRenderSystem;
import net.vulkanmod.vulkan.Vulkan;
import net.vulkanmod.vulkan.device.DeviceManager;
import net.vulkanmod.vulkan.framebuffer.Framebuffer;
import net.vulkanmod.vulkan.framebuffer.RenderPass;
import net.vulkanmod.vulkan.memory.buffer.StagingBuffer;
import net.vulkanmod.vulkan.memory.buffer.index.AutoIndexBuffer;
import net.vulkanmod.vulkan.queue.CommandPool;
import net.vulkanmod.vulkan.queue.GraphicsQueue;
import net.vulkanmod.vulkan.shader.GraphicsPipeline;
import net.vulkanmod.vulkan.shader.Pipeline;
import net.vulkanmod.vulkan.shader.descriptor.ImageDescriptor;
import net.vulkanmod.vulkan.shader.descriptor.UBO;
import net.vulkanmod.vulkan.texture.ImageUtil;
import net.vulkanmod.vulkan.texture.VTextureSelector;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferMemoryBarrier;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkMemoryBarrier;
import org.lwjgl.vulkan.VkMemoryBarrier.Buffer;
import org.slf4j.Logger;

public class VkCommandEncoder implements CommandEncoderBackend {
   private static void transfer(java.util.function.Consumer<VkCommandBuffer> operation) {
      var renderer = Renderer.getInstance(); renderer.endRenderPass(); renderer.submitUploads();
      if (Renderer.isRecording()) operation.accept(Renderer.getCommandBuffer());
      else {
         var commands = net.vulkanmod.vulkan.device.DeviceManager.getGraphicsQueue().beginCommands();
         operation.accept(commands.getHandle());
         long fence = net.vulkanmod.vulkan.device.DeviceManager.getGraphicsQueue().submitCommands(commands);
         int result = VK10.vkWaitForFences(net.vulkanmod.vulkan.device.DeviceManager.vkDevice, fence, true, -1L);
         if (result != VK10.VK_SUCCESS) throw new IllegalStateException("Transfer failed: " + result);
      }
   }

   @Override public void clearStencilTexture(GpuTexture texture, int value) {
      if (!texture.getFormat().hasStencilAspect()) throw new IllegalArgumentException("Texture has no stencil aspect");
      var image = ((VkGpuTexture)texture).getVulkanImage();
      transfer(command -> {
         try (MemoryStack stack = MemoryStack.stackPush()) {
            image.transitionImageLayout(stack, command, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
            var clear = org.lwjgl.vulkan.VkClearDepthStencilValue.calloc(stack).depth(1).stencil(value);
            var range = org.lwjgl.vulkan.VkImageSubresourceRange.calloc(1, stack).aspectMask(VK10.VK_IMAGE_ASPECT_STENCIL_BIT)
               .baseMipLevel(0).levelCount(texture.getMipLevels()).baseArrayLayer(0).layerCount(texture.getDepthOrLayers());
            VK10.vkCmdClearDepthStencilImage(command, image.getId(), VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, clear, range);
         }
      });
   }

   private static final Logger LOGGER = LogUtils.getLogger();
   private final VkGpuDevice device;
   @Nullable
   private RenderPipeline lastPipeline;
   private boolean inRenderPass;
   @Nullable
   private int framebufferId = VkGlFramebuffer.genFramebufferId();

   protected VkCommandEncoder(VkGpuDevice glDevice) {
      this.device = glDevice;
   }

   @Override
   public RenderPassBackend createRenderPass(Supplier<String> supplier, GpuTextureView colorAttachmentView, OptionalInt optionalInt) {
      return this.createRenderPass(supplier, colorAttachmentView, optionalInt, null, OptionalDouble.empty());
   }

   @Override
   public RenderPassBackend createRenderPass(
      Supplier<String> supplier,
      GpuTextureView colorAttachmentView,
      OptionalInt optionalInt,
      @Nullable GpuTextureView depthTexture,
      OptionalDouble optionalDouble
   ) {
      if (this.inRenderPass) {
         throw new IllegalStateException("Close the existing render pass before creating a new one!");
      } else {
         if (optionalDouble.isPresent() && depthTexture == null) {
            LOGGER.warn("Depth clear value was provided but no depth texture is being used");
         }

          if (colorAttachmentView.isClosed()) {
            throw new IllegalStateException("Color texture is closed");
         } else if (depthTexture != null && depthTexture.isClosed()) {
            throw new IllegalStateException("Depth texture is closed");
         } else {
            this.inRenderPass = true;
            GpuTexture depthTexture1 = depthTexture != null ? depthTexture.texture() : null;
            VkFbo fbo = ((VkTextureView)colorAttachmentView).getFbo(depthTexture1);
            fbo.bind();
            int jx = 0;
            if (optionalInt.isPresent()) {
               int k = optionalInt.getAsInt();
               VRenderSystem.setClearColor(ARGB.redFloat(k), ARGB.greenFloat(k), ARGB.blueFloat(k), ARGB.alphaFloat(k));
               jx |= 16384;
            }

            if (depthTexture != null && optionalDouble.isPresent()) {
               VRenderSystem.clearDepth(optionalDouble.getAsDouble());
               jx |= 256;
            }

            if (jx != 0) {
               VkState._disableScissorTest();
               VkState._depthMask(true);
               VkState._colorMask(15);
               VkState._clear(jx);
            }

            VkState._viewport(0, 0, colorAttachmentView.getWidth(0), colorAttachmentView.getHeight(0));
            this.lastPipeline = null;
            return new VkRenderPass(this, depthTexture != null, true);
         }
      }
   }

   @Override
   public boolean isInRenderPass() {
      return this.inRenderPass;
   }

   @Override
   public void clearColorTexture(GpuTexture colorAttachment, int clearColor) {
      if (this.inRenderPass) {
         throw new IllegalStateException("Close the existing render pass before creating a new one!");
      } else {
         if (Renderer.isRecording()) {
             {
               VkGpuTexture vkGpuTexture = (VkGpuTexture)colorAttachment;
               VkGlFramebuffer.bindFramebuffer(36160, this.framebufferId);
               VkGlFramebuffer.framebufferTexture2D(36160, 36064, 3553, vkGpuTexture.glId(), 0);
               VkGlFramebuffer.beginRendering(VkGlFramebuffer.getFramebuffer(this.framebufferId));
               VRenderSystem.setClearColor(ARGB.redFloat(clearColor), ARGB.greenFloat(clearColor), ARGB.blueFloat(clearColor), ARGB.alphaFloat(clearColor));
               Renderer.clearAttachments(16384);
               Renderer.getInstance().endRenderPass();
               VkFbo fbo = ((VkGpuTexture)colorAttachment).getFbo(null);
               ((VkGpuTexture)colorAttachment).setClearColor(clearColor);
               Framebuffer boundFramebuffer = Renderer.getInstance().getBoundFramebuffer();
               if (boundFramebuffer != null && boundFramebuffer.getColorAttachment() == ((VkGpuTexture)colorAttachment).getVulkanImage()) {
                  fbo.clearAttachments();
               }
            }
         } else {
            GraphicsQueue graphicsQueue = DeviceManager.getGraphicsQueue();
            CommandPool.CommandBuffer commandBuffer = graphicsQueue.getCommandBuffer();
            VkGpuTexture vkGpuTexture = (VkGpuTexture)colorAttachment;
            VkGlFramebuffer glFramebuffer = VkGlFramebuffer.getFramebuffer(this.framebufferId);
            glFramebuffer.setAttachmentTexture(36064, vkGpuTexture.glId());
            glFramebuffer.create();
            Framebuffer framebuffer = glFramebuffer.getFramebuffer();
            RenderPass renderPass = glFramebuffer.getRenderPass();

            try (MemoryStack stack = MemoryStack.stackPush()) {
               framebuffer.beginRenderPass(commandBuffer.handle, renderPass, stack);
            }

            VRenderSystem.setClearColor(ARGB.redFloat(clearColor), ARGB.greenFloat(clearColor), ARGB.blueFloat(clearColor), ARGB.alphaFloat(clearColor));
            Renderer.clearAttachments(commandBuffer.handle, 16384, 0, 0, framebuffer.getWidth(), framebuffer.getHeight());
            renderPass.endRenderPass(commandBuffer.handle);
            long fence = graphicsQueue.submitCommands(commandBuffer);
            Synchronization.waitFence(fence);
         }
      }
   }

   @Override
   public void clearColorAndDepthTextures(GpuTexture colorAttachment, int clearColor, GpuTexture depthAttachment, double clearDepth) {
      if (this.inRenderPass) {
         throw new IllegalStateException("Close the existing render pass before creating a new one!");
      } else {
          {
            VkFbo fbo = ((VkGpuTexture)colorAttachment).getFbo(depthAttachment);
            ((VkGpuTexture)colorAttachment).setClearColor(clearColor);
            ((VkGpuTexture)depthAttachment).setDepthClearValue((float)clearDepth);
            Framebuffer boundFramebuffer = Renderer.getInstance().getBoundFramebuffer();
            if (boundFramebuffer != null
               && boundFramebuffer.getColorAttachment() == ((VkGpuTexture)colorAttachment).getVulkanImage()
               && boundFramebuffer.getDepthAttachment() == ((VkGpuTexture)depthAttachment).getVulkanImage()) {
               fbo.clearAttachments();
            }
         }
      }
   }

   @Override
   public void clearColorAndDepthTextures(
      GpuTexture colorAttachment, int clearColor, GpuTexture depthAttachment, double clearDepth, int x0, int y0, int width, int height
   ) {
      if (this.inRenderPass) {
         throw new IllegalStateException("Close the existing render pass before creating a new one!");
      } else {
         VRenderSystem.clearDepth(clearDepth);
         VRenderSystem.setClearColor(ARGB.redFloat(clearColor), ARGB.greenFloat(clearColor), ARGB.blueFloat(clearColor), ARGB.alphaFloat(clearColor));
         int framebufferHeight = colorAttachment.getHeight(0);
         y0 = framebufferHeight - height - y0;
         Framebuffer boundFramebuffer = Renderer.getInstance().getBoundFramebuffer();
         if (boundFramebuffer != null
            && boundFramebuffer.getColorAttachment() == ((VkGpuTexture)colorAttachment).getVulkanImage()
            && boundFramebuffer.getDepthAttachment() == ((VkGpuTexture)depthAttachment).getVulkanImage()) {
            Renderer.clearAttachments(16640, x0, y0, width, height);
         } else {
            VkGpuTexture gpuTexture = (VkGpuTexture)colorAttachment;
            gpuTexture.getFbo(depthAttachment).bind();
            Renderer.clearAttachments(16640, x0, y0, width, height);
         }
      }
   }

   @Override
   public void clearDepthTexture(GpuTexture depthAttachment, double clearDepth) {
      if (this.inRenderPass) {
         throw new IllegalStateException("Close the existing render pass before creating a new one!");
      } else {
         Framebuffer boundFramebuffer = Renderer.getInstance().getBoundFramebuffer();
         if (boundFramebuffer != null && boundFramebuffer.getDepthAttachment() == ((VkGpuTexture)depthAttachment).getVulkanImage()) {
            VRenderSystem.clearDepth(clearDepth);
            Renderer.clearAttachments(256);
         } else {
            ((VkGpuTexture)depthAttachment).setDepthClearValue((float)clearDepth);
         }
      }
   }

   @Override
   public void writeToBuffer(GpuBufferSlice gpuBufferSlice, ByteBuffer byteBuffer) {
      if (this.inRenderPass) {
         throw new IllegalStateException("Close the existing render pass before performing additional commands");
      } else {
         VkGpuBuffer vkGpuBuffer = (VkGpuBuffer)gpuBufferSlice.buffer();
         if (vkGpuBuffer.closed) {
            throw new IllegalStateException("Buffer already closed");
         } else {
            int size = byteBuffer.remaining();
            if (size + gpuBufferSlice.offset() > vkGpuBuffer.size()) {
               throw new IllegalArgumentException(
                  "Cannot write more data than this buffer can hold (attempting to write "
                     + size
                     + " bytes at offset "
                     + gpuBufferSlice.offset()
                     + " to "
                     + gpuBufferSlice.length()
                     + " slice size)"
               );
            } else {
               long dstOffset = gpuBufferSlice.offset();
               CommandPool.CommandBuffer commandBuffer = Renderer.getInstance().getTransferCb();
               StagingBuffer stagingBuffer = Vulkan.getStagingBuffer();
               stagingBuffer.copyBuffer(size, byteBuffer);
               long srcOffset = stagingBuffer.getOffset();

               try (MemoryStack stack = MemoryStack.stackPush()) {
                  if (!commandBuffer.isRecording()) {
                     commandBuffer.begin(stack);
                  }

                  Buffer barrier = VkMemoryBarrier.calloc(1, stack);
                  barrier.sType$Default();
                  org.lwjgl.vulkan.VkBufferMemoryBarrier.Buffer bufferMemoryBarriers = VkBufferMemoryBarrier.calloc(1, stack);
                  VkBufferMemoryBarrier bufferMemoryBarrier = bufferMemoryBarriers.get(0);
                  bufferMemoryBarrier.sType$Default();
                  bufferMemoryBarrier.buffer(vkGpuBuffer.buffer.getId());
                  bufferMemoryBarrier.srcAccessMask(4096);
                  bufferMemoryBarrier.dstAccessMask(4096);
                  bufferMemoryBarrier.size(-1L);
                  VK10.vkCmdPipelineBarrier(commandBuffer.handle, 4096, 4096, 0, barrier, bufferMemoryBarriers, null);
                  org.lwjgl.vulkan.VkBufferCopy.Buffer copyRegion = VkBufferCopy.calloc(1, stack);
                  copyRegion.size(size);
                  copyRegion.srcOffset(srcOffset);
                  copyRegion.dstOffset(dstOffset);
                  VK10.vkCmdCopyBuffer(commandBuffer.handle, stagingBuffer.getId(), vkGpuBuffer.buffer.getId(), copyRegion);
               }
            }
         }
      }
   }

   @Override
   public MappedView mapBuffer(GpuBufferSlice gpuBufferSlice, boolean readable, boolean writable) {
      if (this.inRenderPass) {
         throw new IllegalStateException("Close the existing render pass before performing additional commands");
      } else {
         VkGpuBuffer gpuBuffer = (VkGpuBuffer)gpuBufferSlice.buffer();
         if (gpuBuffer.closed) {
            throw new IllegalStateException("Buffer already closed");
         } else if (!readable && !writable) {
            throw new IllegalArgumentException("At least read or write must be true");
         } else if (readable && (gpuBuffer.usage() & 1) == 0) {
            throw new IllegalStateException("Buffer is not readable");
         } else if (writable && (gpuBuffer.usage() & 2) == 0) {
            throw new IllegalStateException("Buffer is not writable");
         } else if (gpuBufferSlice.offset() + gpuBufferSlice.length() > gpuBuffer.size()) {
            throw new IllegalArgumentException(
               "Cannot map more data than this buffer can hold (attempting to map "
                  + gpuBufferSlice.length()
                  + " bytes at offset "
                  + gpuBufferSlice.offset()
                  + " from "
                  + gpuBuffer.size()
                  + " size buffer)"
            );
         } else {
            int i = 0;
            if (readable) {
               i |= 1;
            }

            if (writable) {
               i |= 34;
            }

            ByteBuffer byteBuffer = MemoryUtil.memByteBuffer(gpuBuffer.getBuffer().getDataPtr() + gpuBufferSlice.offset(), (int)gpuBufferSlice.length());
            return new VkGpuBuffer.MappedView(0, byteBuffer);
         }
      }
   }

   @Override
   public void copyToBuffer(GpuBufferSlice gpuBufferSlice, GpuBufferSlice gpuBufferSlice2) {
      if (this.inRenderPass) {
         throw new IllegalStateException("Close the existing render pass before performing additional commands");
      } else {
         VkGpuBuffer vkGpuBuffer = (VkGpuBuffer)gpuBufferSlice.buffer();
         if (vkGpuBuffer.closed) {
            throw new IllegalStateException("Source buffer already closed");
         } else if ((vkGpuBuffer.usage() & 16) == 0) {
            throw new IllegalStateException("Source buffer needs USAGE_COPY_SRC");
         } else {
            VkGpuBuffer vkGpuBuffer2 = (VkGpuBuffer)gpuBufferSlice2.buffer();
            if (vkGpuBuffer2.closed) {
               throw new IllegalStateException("Target buffer already closed");
            } else if ((vkGpuBuffer2.usage() & 8) == 0) {
               throw new IllegalStateException("Target buffer needs USAGE_COPY_DST to be a destination for a copy");
            } else if (gpuBufferSlice.length() != gpuBufferSlice2.length()) {
               long var6 = gpuBufferSlice.length();
               throw new IllegalArgumentException(
                  "Cannot copy from slice of size " + var6 + " to slice of size " + gpuBufferSlice2.length() + ", they must be equal"
               );
            } else if (gpuBufferSlice.offset() + gpuBufferSlice.length() > vkGpuBuffer.size()) {
               long var5 = gpuBufferSlice.length();
               throw new IllegalArgumentException(
                  "Cannot copy more data than the source buffer holds (attempting to copy "
                     + var5
                     + " bytes at offset "
                     + gpuBufferSlice.offset()
                     + " from "
                     + vkGpuBuffer.size()
                     + " size buffer)"
               );
            } else if (gpuBufferSlice2.offset() + gpuBufferSlice2.length() > vkGpuBuffer2.size()) {
               long var10002 = gpuBufferSlice2.length();
               throw new IllegalArgumentException(
                  "Cannot copy more data than the target buffer can hold (attempting to copy "
                     + var10002
                     + " bytes at offset "
                     + gpuBufferSlice2.offset()
                     + " to "
                     + vkGpuBuffer2.size()
                     + " size buffer)"
               );
            } else {
               transfer(command -> {
                  try (var stack = MemoryStack.stackPush()) {
                     var copies = VkBufferCopy.calloc(1, stack).srcOffset(gpuBufferSlice.offset())
                        .dstOffset(gpuBufferSlice2.offset()).size(gpuBufferSlice.length());
                     VK10.vkCmdCopyBuffer(command, vkGpuBuffer.getBuffer().getId(), vkGpuBuffer2.getBuffer().getId(), copies);
                     var barriers = VkBufferMemoryBarrier.calloc(1, stack).sType$Default()
                        .srcAccessMask(VK10.VK_ACCESS_TRANSFER_WRITE_BIT).dstAccessMask(VK10.VK_ACCESS_MEMORY_READ_BIT)
                        .srcQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED).dstQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED)
                        .buffer(vkGpuBuffer2.getBuffer().getId()).offset(gpuBufferSlice2.offset()).size(gpuBufferSlice2.length());
                     VK10.vkCmdPipelineBarrier(command, VK10.VK_PIPELINE_STAGE_TRANSFER_BIT, VK10.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT, 0, null, barriers, null);
                  }
               });
            }
         }
      }
   }

   @Override
   public void writeToTexture(
      GpuTexture gpuTexture,
      NativeImage nativeImage,
      int level,
      int arrayLayer,
      int xOffset,
      int yOffset,
      int width,
      int height,
      int unpackSkipPixels,
      int unpackSkipRows
   ) {
      if (this.inRenderPass) {
         throw new IllegalStateException("Close the existing render pass before performing additional commands");
      } else if (level >= 0 && level < gpuTexture.getMipLevels()) {
         if (unpackSkipPixels + width > nativeImage.getWidth() || unpackSkipRows + height > nativeImage.getHeight()) {
            throw new IllegalArgumentException(
               "Copy source ("
                  + nativeImage.getWidth()
                  + "x"
                  + nativeImage.getHeight()
                  + ") is not large enough to read a rectangle of "
                  + width
                  + "x"
                  + height
                  + " from "
                  + unpackSkipPixels
                  + "x"
                  + unpackSkipRows
            );
         } else if (xOffset + width > gpuTexture.getWidth(level) || yOffset + height > gpuTexture.getHeight(level)) {
            throw new IllegalArgumentException(
               "Dest texture ("
                  + width
                  + "x"
                  + height
                  + ") is not large enough to write a rectangle of "
                  + width
                  + "x"
                  + height
                  + " at "
                  + xOffset
                  + "x"
                  + yOffset
                  + " (at mip level "
                  + level
                  + ")"
            );
         } else if (gpuTexture.isClosed()) {
            throw new IllegalStateException("Destination texture is closed");
         } else {
            VTextureSelector.setActiveTexture(0);
            VkGlTexture glTexture = VkGlTexture.getTexture(((GlTexture)gpuTexture).glId());
            VTextureSelector.bindTexture(glTexture.getVulkanImage());
            VTextureSelector.uploadSubTexture(
               level, arrayLayer, width, height, xOffset, yOffset, unpackSkipRows, unpackSkipPixels, nativeImage.getWidth(), nativeImage.getPointer()
            );
         }
      } else {
         throw new IllegalArgumentException("Invalid mipLevel " + level + ", must be >= 0 and < " + gpuTexture.getMipLevels());
      }
   }

   @Override
   public void writeToTexture(GpuTexture gpuTexture, ByteBuffer byteBuffer, Format format, int level, int j, int xOffset, int yOffset, int width, int height) {
      if (this.inRenderPass) {
         throw new IllegalStateException("Close the existing render pass before performing additional commands");
      } else if (level >= 0 && level < gpuTexture.getMipLevels()) {
         if (width * height * format.components() > byteBuffer.remaining()) {
            throw new IllegalArgumentException(
               "Copy would overrun the source buffer (remaining length of "
                  + byteBuffer.remaining()
                  + ", but copy is "
                  + width
                  + "x"
                  + height
                  + " of format "
                  + format
                  + ")"
            );
         } else if (xOffset + width > gpuTexture.getWidth(level) || yOffset + height > gpuTexture.getHeight(level)) {
            throw new IllegalArgumentException(
               "Dest texture ("
                  + gpuTexture.getWidth(level)
                  + "x"
                  + gpuTexture.getHeight(level)
                  + ") is not large enough to write a rectangle of "
                  + width
                  + "x"
                  + height
                  + " at "
                  + xOffset
                  + "x"
                  + yOffset
            );
         } else if (gpuTexture.isClosed()) {
            throw new IllegalStateException("Destination texture is closed");
         } else if ((gpuTexture.usage() & 1) == 0) {
            throw new IllegalStateException("Color texture must have USAGE_COPY_DST to be a destination for a write");
         } else if (j >= gpuTexture.getDepthOrLayers()) {
            throw new UnsupportedOperationException("Depth or layer is out of range, must be >= 0 and < " + gpuTexture.getDepthOrLayers());
         } else {
            VkState._bindTexture(((VkGpuTexture)gpuTexture).id);
            VkState._pixelStore(3314, width);
            VkState._pixelStore(3316, 0);
            VkState._pixelStore(3315, 0);
            VkState._pixelStore(3317, format.components());
            VkState._texSubImage2D(3553, level, xOffset, yOffset, width, height, GlConst.toGl(format), 5121, byteBuffer);
         }
      } else {
         throw new IllegalArgumentException("Invalid mipLevel, must be >= 0 and < " + gpuTexture.getMipLevels());
      }
   }

   @Override
   public void copyTextureToBuffer(GpuTexture gpuTexture, GpuBuffer gpuBuffer, long i, Runnable runnable, int j) {
      if (this.inRenderPass) {
         throw new IllegalStateException("Close the existing render pass before performing additional commands");
      } else {
         this.copyTextureToBuffer(gpuTexture, gpuBuffer, i, runnable, j, 0, 0, gpuTexture.getWidth(j), gpuTexture.getHeight(j));
      }
   }

   @Override
   public void copyTextureToBuffer(
      GpuTexture gpuTexture, GpuBuffer gpuBuffer, long dstOffset, Runnable runnable, int mipLevel, int xOffset, int yOffset, int width, int height
   ) {
      VkGpuBuffer vkGpuBuffer = (VkGpuBuffer)gpuBuffer;
      VkGpuTexture vkGpuTexture = (VkGpuTexture)gpuTexture;
      if (inRenderPass) throw new IllegalStateException("Close the render pass before copying");
      boolean recorded = Renderer.isRecording();
      transfer(command -> {
         try (var stack = MemoryStack.stackPush()) {
            var image = vkGpuTexture.getVulkanImage();
            image.transitionImageLayout(stack, command, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL);
            ImageUtil.copyImageToBufferCmd(stack, command, vkGpuBuffer.getBuffer().getId(), image.getId(), mipLevel,
               width, height, xOffset, yOffset, dstOffset, width, height);
            var barrier = VkBufferMemoryBarrier.calloc(1, stack).sType$Default()
               .srcAccessMask(VK10.VK_ACCESS_TRANSFER_WRITE_BIT).dstAccessMask(VK10.VK_ACCESS_HOST_READ_BIT)
               .srcQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED).dstQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED)
               .buffer(vkGpuBuffer.getBuffer().getId()).offset(dstOffset).size((long)width*height*gpuTexture.getFormat().pixelSize());
            VK10.vkCmdPipelineBarrier(command, VK10.VK_PIPELINE_STAGE_TRANSFER_BIT, VK10.VK_PIPELINE_STAGE_HOST_BIT, 0, null, barrier, null);
         }
      });
      if (recorded) MemoryManager.getInstance().addFrameOp(runnable); else runnable.run();
   }

   @Override
   public void copyTextureToTexture(GpuTexture gpuTexture, GpuTexture gpuTexture2, int mipLevel, int j, int k, int l, int m, int n, int o) {
      if (this.inRenderPass) {
         throw new IllegalStateException("Close the existing render pass before performing additional commands");
      } else if (mipLevel >= 0 && mipLevel < gpuTexture.getMipLevels() && mipLevel < gpuTexture2.getMipLevels()) {
         if (j + n > gpuTexture2.getWidth(mipLevel) || k + o > gpuTexture2.getHeight(mipLevel)) {
            throw new IllegalArgumentException(
               "Dest texture ("
                  + gpuTexture2.getWidth(mipLevel)
                  + "x"
                  + gpuTexture2.getHeight(mipLevel)
                  + ") is not large enough to write a rectangle of "
                  + n
                  + "x"
                  + o
                  + " at "
                  + j
                  + "x"
                  + k
            );
         } else if (l + n > gpuTexture.getWidth(mipLevel) || m + o > gpuTexture.getHeight(mipLevel)) {
            throw new IllegalArgumentException(
               "Source texture ("
                  + gpuTexture.getWidth(mipLevel)
                  + "x"
                  + gpuTexture.getHeight(mipLevel)
                  + ") is not large enough to read a rectangle of "
                  + n
                  + "x"
                  + o
                  + " at "
                  + l
                  + "x"
                  + m
            );
         } else if (gpuTexture.isClosed()) {
            throw new IllegalStateException("Source texture is closed");
         } else if (gpuTexture2.isClosed()) {
            throw new IllegalStateException("Destination texture is closed");
         }
      } else {
         throw new IllegalArgumentException(
            "Invalid mipLevel " + mipLevel + ", must be >= 0 and < " + gpuTexture.getMipLevels() + " and < " + gpuTexture2.getMipLevels()
         );
      }
      if (gpuTexture.getFormat()!=gpuTexture2.getFormat()) throw new IllegalArgumentException("Texture formats differ");
      if (gpuTexture == gpuTexture2) throw new IllegalArgumentException("Same-image copy is not supported");
      transfer(command -> {
         try (var stack = MemoryStack.stackPush()) {
            var source = ((VkGpuTexture)gpuTexture).getVulkanImage();
            var destination = ((VkGpuTexture)gpuTexture2).getVulkanImage();
            source.transitionImageLayout(stack, command, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL);
            destination.transitionImageLayout(stack, command, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
            // Vulkan requires separate regions for the depth and stencil aspects.
            var copy = org.lwjgl.vulkan.VkImageCopy.calloc(Integer.bitCount(source.aspect), stack);
            int regionIndex = 0;
            for (int aspect = 1; aspect <= VK10.VK_IMAGE_ASPECT_STENCIL_BIT; aspect <<= 1) {
               if ((source.aspect & aspect) == 0) continue;
               var region = copy.get(regionIndex++);
               region.srcSubresource().aspectMask(aspect).mipLevel(mipLevel).baseArrayLayer(0).layerCount(1);
               region.dstSubresource().aspectMask(aspect).mipLevel(mipLevel).baseArrayLayer(0).layerCount(1);
               region.srcOffset().set(l, m, 0); region.dstOffset().set(j, k, 0); region.extent().set(n, o, 1);
            }
            VK10.vkCmdCopyImage(command, source.getId(), VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
               destination.getId(), VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, copy);
         }
      });
   }

   @Override
   public GpuFence createFence() {
      if (this.inRenderPass) {
         throw new IllegalStateException("Close the existing render pass before performing additional commands");
      } else {
         return Renderer.getInstance().createFenceTicket();
      }
   }

   @Override
   public GpuQuery timerQueryBegin() {
      return new VkTimerQuery();
   }

   @Override
   public void timerQueryEnd(GpuQuery gpuQuery) {
      ((VkTimerQuery)gpuQuery).end();
   }

   @Override
   public void presentTexture(GpuTextureView gpuTexture) {
      VkTextureView source = (VkTextureView)gpuTexture;
      Renderer renderer = Renderer.getInstance();
      renderer.endRenderPass();
      var src = source.texture().getVulkanImage();
      var dst = renderer.getSwapChain().getColorAttachment();
      try (MemoryStack stack = MemoryStack.stackPush()) {
         var command = Renderer.getCommandBuffer();
         src.transitionImageLayout(stack, command, 6);
         dst.transitionImageLayout(stack, command, 7);
         var region = org.lwjgl.vulkan.VkImageBlit.calloc(1, stack);
         region.srcSubresource().aspectMask(1).mipLevel(source.baseMipLevel()).baseArrayLayer(0).layerCount(1);
         region.dstSubresource().aspectMask(1).mipLevel(0).baseArrayLayer(0).layerCount(1);
         region.srcOffsets(0).set(0, 0, 0); region.srcOffsets(1).set(source.getWidth(0), source.getHeight(0), 1);
         region.dstOffsets(0).set(0, 0, 0); region.dstOffsets(1).set(renderer.getSwapChain().getWidth(), renderer.getSwapChain().getHeight(), 1);
         VK10.vkCmdBlitImage(command, src.getId(), 6, dst.getId(), 7, region, 1);
         dst.transitionImageLayout(stack, command, 1000001002);
      }
   }

   protected <T> void executeDrawMultiple(
      VkRenderPass renderPass,
      Collection<Draw<T>> collection,
      @Nullable GpuBuffer indexBuffer,
      @Nullable IndexType indexType,
      Collection<String> collection2,
      T object
   ) {
      IndexType fallbackType = indexType == null ? IndexType.SHORT : indexType;
      for (Draw<T> draw : collection) {
         IndexType selectedType = draw.indexType() == null ? fallbackType : draw.indexType();
         renderPass.setIndexBuffer(draw.indexBuffer() == null ? indexBuffer : draw.indexBuffer(), selectedType);
         renderPass.setVertexBuffer(draw.slot(), draw.vertexBuffer());
         for (String name : collection2) renderPass.uniforms.remove(name);
         var upload = draw.uniformUploaderConsumer();
         if (upload != null) upload.accept(object, (name, slice) -> renderPass.uniforms.put(name, slice));
         this.executeDraw(renderPass, draw.baseVertex(), draw.firstIndex(), draw.indexCount(), selectedType, 1);
      }
   }

   protected void executeDraw(VkRenderPass renderPass, int vertexOffset, int firstIndex, int vertexCount, @Nullable IndexType indexType, int instanceCount) {
      if (this.trySetup(renderPass)) {
         if (VkRenderPass.VALIDATION) {
            if (indexType != null) {
               if (renderPass.indexBuffer == null) {
                  throw new IllegalStateException("Missing index buffer");
               }

               if (renderPass.indexBuffer.isClosed()) {
                  throw new IllegalStateException("Index buffer has been closed!");
               }
            }

            if (renderPass.pipeline.getVertexFormat().getVertexSize() != 0 && renderPass.vertexBuffers[0] == null) {
               throw new IllegalStateException("Missing vertex buffer at slot 0");
            }

            if (renderPass.vertexBuffers[0] != null && renderPass.vertexBuffers[0].isClosed()) {
               throw new IllegalStateException("Vertex buffer at slot 0 has been closed!");
            }
         }

         this.drawFromBuffers(renderPass, vertexOffset, firstIndex, vertexCount, indexType, renderPass.pipeline, instanceCount);
      }
   }

   public void drawFromBuffers(
      VkRenderPass renderPass,
      int vertexOffset,
      int firstIndex,
      int vertexCount,
      @Nullable IndexType indexType,
      RenderPipeline renderPipeline,
      int instanceCount
   ) {
      if (instanceCount < 1 || vertexCount <= 0) return;

      VkCommandBuffer vkCommandBuffer = Renderer.getCommandBuffer();
      VkGpuBuffer vertexBuffer = (VkGpuBuffer)renderPass.vertexBuffers[0];

      try (MemoryStack stack = MemoryStack.stackPush()) {
         if (vertexBuffer != null) {
            VK11.vkCmdBindVertexBuffers(vkCommandBuffer, 0, stack.longs(vertexBuffer.buffer.getId()), stack.longs(vertexBuffer.offset));
         }

         if (indexType != null) {
            VkGpuBuffer indexBuffer = (VkGpuBuffer)renderPass.indexBuffer;

            int vkIndexType = switch (indexType) {
               case SHORT -> 0;
               case INT -> 1;
            };
            VK11.vkCmdBindIndexBuffer(vkCommandBuffer, indexBuffer.buffer.getId(), indexBuffer.offset, vkIndexType);
            VK11.vkCmdDrawIndexed(vkCommandBuffer, vertexCount, instanceCount, firstIndex, vertexOffset, 0);
         } else {
            AutoIndexBuffer autoIndexBuffer = Renderer.getDrawer().getAutoIndexBuffer(renderPipeline.getVertexFormatMode(), vertexCount);
            if (autoIndexBuffer != null) {
               int indexCount = autoIndexBuffer.getIndexCount(vertexCount);
               VK11.vkCmdBindIndexBuffer(vkCommandBuffer, autoIndexBuffer.getIndexBuffer().getId(), 0L, autoIndexBuffer.getIndexBuffer().indexType.value);
               VK11.vkCmdDrawIndexed(vkCommandBuffer, indexCount, instanceCount, firstIndex, vertexOffset, 0);
            } else {
               VK11.vkCmdDraw(vkCommandBuffer, vertexCount, instanceCount, vertexOffset, 0);
            }
         }
      }
   }

   public boolean trySetup(VkRenderPass renderPass) {
      if (VkRenderPass.VALIDATION) {
         if (renderPass.pipeline == null) {
            throw new IllegalStateException("Can't draw without a render pipeline");
         }

         for (UniformDescription uniformDescription : renderPass.pipeline.getUniforms()) {
            Object object = renderPass.uniforms.get(uniformDescription.name());
            if (object == null && !GlProgram.BUILT_IN_UNIFORMS.contains(uniformDescription.name())) {
               throw new IllegalStateException("Missing uniform " + uniformDescription.name() + " (should be " + uniformDescription.type() + ")");
            }
         }
      }

      var renderer = Renderer.getInstance();
      var previousPass = renderer.getBoundRenderPass();
      var previousTarget = renderer.getBoundFramebuffer();
      boolean interrupted = false;
      for (var binding : renderPass.samplers.values()) {
         var image = binding.view().texture().getVulkanImage();
         if (image.getCurrentLayout() != VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL) {
            if (!interrupted) { renderer.endRenderPass(); interrupted = true; }
            try (var stack = MemoryStack.stackPush()) {
               image.transitionImageLayout(stack, Renderer.getCommandBuffer(), VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
            }
         }
      }
      if (interrupted && previousPass != null) renderer.beginRenderPass(previousPass, previousTarget);
      this.applyPipelineState(renderPass.pipeline);
      this.setupUniforms(renderPass);
      if (renderPass.isScissorEnabled()) {
         VkState._enableScissorTest();
         VkState._scissorBox(renderPass.getScissorX(), renderPass.getScissorY(), renderPass.getScissorWidth(), renderPass.getScissorHeight());
      } else {
         VkState._disableScissorTest();
      }

      return this.bindPipeline(renderPass.pipeline);
   }

   public void setupUniforms(VkRenderPass renderPass) {
      RenderPipeline renderPipeline = renderPass.pipeline;
      Pipeline pipeline = ExtendedRenderPipeline.of(renderPass.pipeline).getPipeline();

      for (UBO ubo : pipeline.getBuffers()) {
         String uniformName = ubo.name;
         GpuBufferSlice gpuBufferSlice = renderPass.uniforms.get(uniformName);
         if (gpuBufferSlice == null) {
            ubo.setUseGlobalBuffer(true);
            ubo.setUpdate(true);
         } else {
            VkGpuBuffer gpuBuffer = (VkGpuBuffer)gpuBufferSlice.buffer();

            assert ubo != null;

            ubo.setUseGlobalBuffer(false);
            ubo.getBufferSlice().set(gpuBuffer.buffer, gpuBufferSlice.offset(), (int)gpuBufferSlice.length());
         }
      }

      for (ImageDescriptor imageDescriptor : pipeline.getImageDescriptors()) {
         String uniformName = imageDescriptor.name;
         if (imageDescriptor.isTexelBuffer()) {
            var slice = renderPass.uniforms.get(uniformName);
            if (slice == null) throw new IllegalStateException("Missing texel buffer: " + uniformName);
            var description = renderPipeline.getUniforms().stream().filter(u -> u.name().equals(uniformName)).findFirst().orElseThrow();
            var buffer = (VkGpuBuffer)slice.buffer();
            imageDescriptor.setTexelView(buffer.texelView(VkGpuTexture.vkFormat(description.textureFormat()), slice.offset(), slice.length()));
            continue;
         }
         int samplerIndex = imageDescriptor.imageIdx;
         VkRenderPass.TextureViewAndSampler textureSampler = renderPass.samplers.get(uniformName);
         if (textureSampler != null) {
            VkTextureView textureView = textureSampler.view();
            VkGpuTexture gpuTexture = textureView.texture();
            if (!gpuTexture.isClosed()) {
               VkState._activeTexture(33984 + samplerIndex);
               VkState._bindTexture(gpuTexture.id);
               gpuTexture.getVulkanImage().setSampler(textureSampler.sampler().getId());
            }
         }
      }
   }

   public boolean bindPipeline(RenderPipeline renderPipeline) {
      Pipeline pipeline = ExtendedRenderPipeline.of(renderPipeline).getPipeline();
      if (pipeline == null) {
         return false;
      } else {
         Renderer renderer = Renderer.getInstance();
         renderer.bindGraphicsPipeline((GraphicsPipeline)pipeline);
         renderer.uploadAndBindUBOs(pipeline);
         return true;
      }
   }

   public void applyPipelineState(RenderPipeline renderPipeline) {
      if (this.lastPipeline != renderPipeline) {
         this.lastPipeline = renderPipeline;
         DepthStencilState depthStencilState = renderPipeline.getDepthStencilState();
         if (depthStencilState != null) {
            VkState._enableDepthTest();
            VkState._depthFunc(GlConst.toGl(depthStencilState.depthTest()));
            VkState._depthMask(depthStencilState.writeDepth());
            if (depthStencilState.depthBiasConstant() == 0.0F && depthStencilState.depthBiasScaleFactor() == 0.0F) {
               VkState._disablePolygonOffset();
            } else {
               VkState._polygonOffset(depthStencilState.depthBiasScaleFactor(), depthStencilState.depthBiasConstant());
               VkState._enablePolygonOffset();
            }
         } else {
            VkState._disableDepthTest();
            VkState._depthMask(false);
            VkState._disablePolygonOffset();
         }

         if (renderPipeline.isCull()) {
            VkState._enableCull();
         } else {
            VkState._disableCull();
         }

         if (renderPipeline.getColorTargetState().blendFunction().isPresent()) {
            VkState._enableBlend();
            BlendFunction blendFunction = renderPipeline.getColorTargetState().blendFunction().get();
            VkState._blendFuncSeparate(
               GlConst.toGl(blendFunction.sourceColor()),
               GlConst.toGl(blendFunction.destColor()),
               GlConst.toGl(blendFunction.sourceAlpha()),
               GlConst.toGl(blendFunction.destAlpha())
            );
         } else {
            VkState._disableBlend();
         }

         VkState._polygonMode(1032, GlConst.toGl(renderPipeline.getPolygonMode()));
         VkState._colorMask(renderPipeline.getColorTargetState().writeMask());
         VRenderSystem.setPrimitiveTopologyGL(GlConst.toGl(renderPipeline.getVertexFormatMode()));
      }
   }

   public void finishRenderPass(boolean forceEnd) {
      if (forceEnd) {
         Renderer.getInstance().endRenderPass();
      }

      this.inRenderPass = false;
   }

   protected VkGpuDevice getDevice() {
      return this.device;
   }
}
