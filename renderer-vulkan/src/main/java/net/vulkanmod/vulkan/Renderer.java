// VulkanMod 0.6.8+26.1.2, Collateral and contributors. LGPL-3.0-only.
// Reconstructed from the matching published artifact; modified for Deep Primal on 2026-10-19.
package net.vulkanmod.vulkan;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.vulkanmod.Initializer;
import net.vulkanmod.gl.VkGlFramebuffer;
import net.vulkanmod.mixin.window.WindowAccessor;
import net.vulkanmod.render.profiling.Profiler;
import net.vulkanmod.render.texture.ImageUploadHelper;
import net.vulkanmod.vulkan.device.DeviceManager;
import net.vulkanmod.vulkan.framebuffer.Framebuffer;
import net.vulkanmod.vulkan.framebuffer.RenderPass;
import net.vulkanmod.vulkan.framebuffer.SwapChain;
import net.vulkanmod.vulkan.memory.MemoryManager;
import net.vulkanmod.vulkan.pass.DefaultMainPass;
import net.vulkanmod.vulkan.pass.MainPass;
import net.vulkanmod.vulkan.shader.GraphicsPipeline;
import net.vulkanmod.vulkan.shader.Pipeline;
import net.vulkanmod.vulkan.shader.PipelineState;
import net.vulkanmod.vulkan.shader.Uniforms;
import net.vulkanmod.vulkan.shader.layout.PushConstants;
import net.vulkanmod.vulkan.texture.VTextureSelector;
import net.vulkanmod.vulkan.util.VkResult;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkClearAttachment;
import org.lwjgl.vulkan.VkClearAttachment.Buffer;
import org.lwjgl.vulkan.VkClearRect;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkViewport;

public class Renderer {
  private static Renderer INSTANCE;
  private static VkDevice device;
  private static boolean swapChainUpdate = false;
  private final Set<Pipeline> usedPipelines = new ObjectOpenHashSet<>();
  private Pipeline boundPipeline;
  private long boundPipelineHandle;
  private Drawer drawer;
  private SwapChain swapChain;
  private int framesNum;
  private List<VkCommandBuffer> mainCommandBuffers;
  private ArrayList<Long> imageAvailableSemaphores;
  private ArrayList<Long> renderFinishedSemaphores;
  private ArrayList<Long> inFlightFences;
  private long[] submittedGenerations;
  private int fenceEpoch;
  private boolean imageAcquireWaitPending;
  private final net.vulkanmod.vulkan.shader.DescriptorBindingState descriptorBindings =
      new net.vulkanmod.vulkan.shader.DescriptorBindingState();

  public boolean descriptorBindingChanged(
      long layout, long set, int bindPoint, java.nio.IntBuffer offsets) {
    boolean changed = descriptorBindings.changed(layout, set, bindPoint, offsets);
    if (changed) VulkanStatistics.descriptorBinds++;
    else VulkanStatistics.descriptorBindSkips++;
    return changed;
  }

  private Framebuffer boundFramebuffer;
  private RenderPass boundRenderPass;
  private static int currentFrame = 0;
  private static int imageIndex = 0;
  private static int lastReset = -1;
  private VkCommandBuffer currentCmdBuffer;
  private boolean recordingCmds = false;
  int recursion = 0;
  MainPass mainPass;
  private final List<Runnable> onResizeCallbacks = new ObjectArrayList<>();

  public static void initRenderer() {
    INSTANCE = new Renderer();
    INSTANCE.init();
  }

  public static Renderer getInstance() {
    return INSTANCE;
  }

  public static Drawer getDrawer() {
    return INSTANCE.drawer;
  }

  public static int getCurrentFrame() {
    return currentFrame;
  }

  public static int getCurrentImage() {
    return imageIndex;
  }

  public Renderer() {
    device = Vulkan.getVkDevice();
    this.framesNum = Initializer.CONFIG.frameQueueSize;
  }

  public static void setLineWidth(float width) {
    if (INSTANCE.boundFramebuffer != null) {
      VK10.vkCmdSetLineWidth(INSTANCE.currentCmdBuffer, width);
    }
  }

  private void init() {
    MemoryManager.createInstance(getFramesNum());
    Vulkan.createStagingBuffers();
    this.swapChain = new SwapChain();
    this.mainPass = DefaultMainPass.create();
    this.drawer = new Drawer();
    this.drawer.createResources(this.framesNum);
    Uniforms.setupDefaultUniforms();

    this.allocateCommandBuffers();
    this.createSyncObjects();
  }

  private void allocateCommandBuffers() {
    if (this.mainCommandBuffers != null) {
      this.mainCommandBuffers.forEach(
          commandBuffer ->
              VK10.vkFreeCommandBuffers(device, Vulkan.getCommandPool(), commandBuffer));
    }

    this.mainCommandBuffers = new ArrayList<>(this.framesNum);

    try (MemoryStack stack = MemoryStack.stackPush()) {
      VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.calloc(stack);
      allocInfo.sType(40);
      allocInfo.commandPool(Vulkan.getCommandPool());
      allocInfo.level(0);
      allocInfo.commandBufferCount(this.framesNum);
      PointerBuffer pCommandBuffers = stack.mallocPointer(this.framesNum);
      int vkResult = VK10.vkAllocateCommandBuffers(device, allocInfo, pCommandBuffers);
      if (vkResult != 0) {
        throw new RuntimeException(
            "Failed to allocate command buffers: %s".formatted(VkResult.decode(vkResult)));
      }

      for (int i = 0; i < this.framesNum; i++) {
        this.mainCommandBuffers.add(new VkCommandBuffer(pCommandBuffers.get(i), device));
      }
    }
  }

  private void createSyncObjects() {
    int swapChainImages = this.swapChain.getImagesNum();
    this.submittedGenerations = new long[this.framesNum];
    this.fenceEpoch++;
    this.renderFinishedSemaphores = new ArrayList<>(swapChainImages);
    this.imageAvailableSemaphores = new ArrayList<>(this.framesNum);
    this.inFlightFences = new ArrayList<>(this.framesNum);

    try (MemoryStack stack = MemoryStack.stackPush()) {
      VkSemaphoreCreateInfo semaphoreInfo = VkSemaphoreCreateInfo.calloc(stack);
      semaphoreInfo.sType(9);
      VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.calloc(stack);
      fenceInfo.sType(8);
      fenceInfo.flags(1);
      LongBuffer pImageAvailableSemaphore = stack.mallocLong(1);
      LongBuffer pRenderFinishedSemaphore = stack.mallocLong(1);
      LongBuffer pFence = stack.mallocLong(1);

      for (int i = 0; i < this.framesNum; i++) {
        if (VK10.vkCreateSemaphore(device, semaphoreInfo, null, pImageAvailableSemaphore) != 0
            || VK10.vkCreateFence(device, fenceInfo, null, pFence) != 0) {
          throw new RuntimeException(
              "Failed to create synchronization objects for the frame: " + i);
        }

        this.imageAvailableSemaphores.add(pImageAvailableSemaphore.get(0));
        this.inFlightFences.add(pFence.get(0));
      }

      for (int i = 0; i < this.swapChain.getImagesNum(); i++) {
        if (VK10.vkCreateSemaphore(device, semaphoreInfo, null, pRenderFinishedSemaphore) != 0) {
          throw new RuntimeException(
              "Failed to create synchronization objects for the image: " + i);
        }

        this.renderFinishedSemaphores.add(pRenderFinishedSemaphore.get(0));
      }
    }
  }

  public void preInitFrame() {
    Profiler p = Profiler.getMainProfiler();
    p.pop();
    p.round();
    p.push("Frame_ops");
    this.drawer.resetBuffers(currentFrame);
  }

  public void beginFrame() {
    this.recursion++;
    if (swapChainUpdate && this.recursion <= 1) {
      this.recreateSwapChain();
      swapChainUpdate = false;
    }

    if (this.recursion > 1) {
      this.endFrame();
    }

    Profiler p = Profiler.getMainProfiler();
    p.pop();
    p.push("Frame_fence");
    VK10.vkWaitForFences(device, this.inFlightFences.get(currentFrame), true, -1L);
    p.pop();
    p.push("Begin_rendering");
    this.submitUploads();
    MemoryManager.getInstance().initFrame(currentFrame);
    this.drawer.setCurrentFrame(currentFrame);
    Vulkan.getStagingBuffers().beginFrame(currentFrame);
    this.preInitFrame();
    this.resetDescriptors();
    this.currentCmdBuffer = this.mainCommandBuffers.get(currentFrame);
    VK10.vkResetCommandBuffer(this.currentCmdBuffer, 0);

    try (MemoryStack stack = MemoryStack.stackPush()) {
      if (this.swapChain.hasImages()) {
        IntBuffer pImageIndex = stack.mallocInt(1);
        long semaphore = this.imageAvailableSemaphores.get(currentFrame);
        int vkResult =
            KHRSwapchain.vkAcquireNextImageKHR(
                device, this.swapChain.getId(), -1L, semaphore, 0L, pImageIndex);
        if (vkResult != 1000001003 && vkResult != -1000001004 && !swapChainUpdate) {
          if (vkResult != 0) {
            throw new RuntimeException(
                "Cannot acquire next swap chain image: %s".formatted(VkResult.decode(vkResult)));
          }
        } else {
          swapChainUpdate = true;
        }

        imageIndex = pImageIndex.get(0);
        this.swapChain.setAcquired(true);
        imageAcquireWaitPending = true;
      }

      this.beginMainRenderPass(stack);
    }

    p.pop();
  }

  private void beginMainRenderPass(MemoryStack stack) {
    descriptorBindings.reset();
    this.boundPipeline = null;
    this.boundPipelineHandle = 0L;
    VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack);
    beginInfo.sType(42);
    beginInfo.flags(1);
    VkCommandBuffer commandBuffer = this.currentCmdBuffer;
    int vkResult = VK10.vkBeginCommandBuffer(commandBuffer, beginInfo);
    if (vkResult != 0) {
      throw new RuntimeException(
          "Failed to begin recording command buffer: %s".formatted(VkResult.decode(vkResult)));
    } else {
      this.recordingCmds = true;
      this.mainPass.begin(commandBuffer, stack);
      resetDynamicState(commandBuffer);
    }
  }

  public void endFrame() {
    if (this.recordingCmds) {
      if (this.recursion != 0) {
        this.recursion--;
        Profiler p = Profiler.getMainProfiler();
        p.push("End_rendering");
        this.mainPass.end(this.currentCmdBuffer);
        this.submitUploads();
        this.waitFences();
        this.submitFrame();
        this.recordingCmds = false;
        this.boundRenderPass = null;
        this.boundFramebuffer = null;
        p.pop();
        p.push("Post_rendering");
      }
    }
  }

  private void submitFrame() {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack);
      submitInfo.sType(4);
      int waitSemaphoreCount = Synchronization.INSTANCE.getWaitSemaphoreCount();
      int totalWaitSemaphores = waitSemaphoreCount;
      if (imageAcquireWaitPending) {
        totalWaitSemaphores = waitSemaphoreCount + 1;
      }

      LongBuffer waitSemaphores = stack.mallocLong(totalWaitSemaphores);
      IntBuffer waitDstStageMask = stack.mallocInt(totalWaitSemaphores);
      Synchronization.INSTANCE.getWaitSemaphores(waitSemaphores);

      for (int i = 0; i < waitSemaphoreCount; i++) {
        waitDstStageMask.put(i, 1);
      }

      if (imageAcquireWaitPending) {
        waitSemaphores.put(
            totalWaitSemaphores - 1, this.imageAvailableSemaphores.get(currentFrame));
        waitDstStageMask.put(totalWaitSemaphores - 1, VK10.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT);
      }

      waitSemaphores.position(0);
      waitSemaphores.limit(totalWaitSemaphores);
      submitInfo.pWaitSemaphores(waitSemaphores);
      submitInfo.waitSemaphoreCount(waitSemaphores.limit());
      submitInfo.pWaitDstStageMask(waitDstStageMask);
      submitInfo.pCommandBuffers(stack.pointers(this.currentCmdBuffer));
      if (this.swapChain.isAcquired()) {
        submitInfo.pSignalSemaphores(stack.longs(this.renderFinishedSemaphores.get(imageIndex)));
      }

      VK10.vkResetFences(device, this.inFlightFences.get(currentFrame));
      int vkResult;
      if ((vkResult =
              VK10.vkQueueSubmit(
                  DeviceManager.getGraphicsQueue().vkQueue(),
                  submitInfo,
                  this.inFlightFences.get(currentFrame)))
          != 0) {
        VK10.vkResetFences(device, this.inFlightFences.get(currentFrame));
        throw new RuntimeException(
            "Failed to submit draw command buffer: %s".formatted(VkResult.decode(vkResult)));
      }

      imageAcquireWaitPending = false;
      submittedGenerations[currentFrame]++;
      Synchronization.INSTANCE.scheduleCbReset();
      if (this.swapChain.isAcquired()) {
        VkPresentInfoKHR presentInfo = VkPresentInfoKHR.calloc(stack);
        presentInfo.sType(1000001001);
        presentInfo.pWaitSemaphores(stack.longs(this.renderFinishedSemaphores.get(imageIndex)));
        presentInfo.swapchainCount(1);
        presentInfo.pSwapchains(stack.longs(this.swapChain.getId()));
        presentInfo.pImageIndices(stack.ints(imageIndex));
        vkResult =
            KHRSwapchain.vkQueuePresentKHR(DeviceManager.getPresentQueue().vkQueue(), presentInfo);
        if (vkResult == -1000001004 || vkResult == 1000001003 || swapChainUpdate) {
          swapChainUpdate = true;
          return;
        }

        if (vkResult != 0) {
          throw new RuntimeException(
              "Failed to present rendered frame: %s".formatted(VkResult.decode(vkResult)));
        }
      }

      Vulkan.getStagingBuffers().endFrame(currentFrame);
      currentFrame = (currentFrame + 1) % this.framesNum;
      this.swapChain.setAcquired(false);
    }
  }

  /** Tickets track frame generations, so an old signaled fence cannot complete unsubmitted work. */
  public com.mojang.blaze3d.buffers.GpuFence createFenceTicket() {
    final int slot = currentFrame, epoch = fenceEpoch;
    final long wanted = submittedGenerations[slot] + (recordingCmds ? 1 : 0);
    return new com.mojang.blaze3d.buffers.GpuFence() {
      private boolean closed;

      public void close() {
        closed = true;
      }

      public boolean awaitCompletion(long timeout) {
        if (closed) return true;
        if (epoch != fenceEpoch || submittedGenerations[slot] > wanted) return true;
        if (submittedGenerations[slot] < wanted) {
          if (timeout == Long.MAX_VALUE && currentFrame == slot && recordingCmds) flushCmds();
          else return false;
        }
        int status =
            timeout == 0
                ? VK10.vkGetFenceStatus(device, inFlightFences.get(slot))
                : VK10.vkWaitForFences(device, inFlightFences.get(slot), true, timeout);
        if (status == VK10.VK_SUCCESS) return true;
        if (status == VK10.VK_NOT_READY || status == VK10.VK_TIMEOUT) return false;
        throw new IllegalStateException("Vulkan fence failed: " + VkResult.decode(status));
      }
    };
  }

  public void flushCmds() {
    if (this.recordingCmds) {
      try (MemoryStack stack = MemoryStack.stackPush()) {
        this.endRenderPass(this.currentCmdBuffer);
        VK10.vkEndCommandBuffer(this.currentCmdBuffer);
        VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack);
        submitInfo.sType(4);
        submitInfo.pCommandBuffers(stack.pointers(this.currentCmdBuffer));
        this.submitUploads();
        int waitSemaphoreCount = Synchronization.INSTANCE.getWaitSemaphoreCount();
        int totalWait = waitSemaphoreCount + (imageAcquireWaitPending ? 1 : 0);
        LongBuffer waitSemaphores = stack.mallocLong(totalWait);
        IntBuffer waitDstStageMask = stack.mallocInt(totalWait);
        Synchronization.INSTANCE.getWaitSemaphores(waitSemaphores);

        for (int i = 0; i < waitSemaphoreCount; i++) {
          waitDstStageMask.put(i, 1);
        }

        if (imageAcquireWaitPending) {
          waitSemaphores.put(waitSemaphoreCount, imageAvailableSemaphores.get(currentFrame));
          waitDstStageMask.put(waitSemaphoreCount, VK10.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT);
        }
        waitSemaphores.position(0);
        waitSemaphores.limit(totalWait);
        submitInfo.pWaitSemaphores(waitSemaphores);
        submitInfo.waitSemaphoreCount(waitSemaphores.limit());
        submitInfo.pWaitDstStageMask(waitDstStageMask);
        this.waitFences();
        VK10.vkResetFences(device, this.inFlightFences.get(currentFrame));
        int vkResult;
        if ((vkResult =
                VK10.vkQueueSubmit(
                    DeviceManager.getGraphicsQueue().vkQueue(),
                    submitInfo,
                    this.inFlightFences.get(currentFrame)))
            != 0) {
          VK10.vkResetFences(device, this.inFlightFences.get(currentFrame));
          throw new RuntimeException(
              "Failed to submit draw command buffer: %s".formatted(VkResult.decode(vkResult)));
        }

        VK10.vkWaitForFences(device, this.inFlightFences.get(currentFrame), true, -1L);
        imageAcquireWaitPending = false;
        submittedGenerations[currentFrame]++;
        this.beginMainRenderPass(stack);
      }
    }
  }

  public void submitUploads() {
    ImageUploadHelper.INSTANCE.submitCommands();
  }

  public void endRenderPass() {
    this.endRenderPass(this.currentCmdBuffer);
  }

  public void endRenderPass(VkCommandBuffer commandBuffer) {
    if (this.recordingCmds && this.boundFramebuffer != null) {
      this.boundRenderPass.endRenderPass(commandBuffer);
      this.boundRenderPass = null;
      this.boundFramebuffer = null;
      VkGlFramebuffer.resetBoundFramebuffer();
    }
  }

  public boolean beginRenderPass(RenderPass renderPass, Framebuffer framebuffer) {
    if (!this.recordingCmds) {
      this.beginFrame();
      this.recordingCmds = true;
    }

    if (this.boundFramebuffer != framebuffer) {
      this.endRenderPass(this.currentCmdBuffer);

      try (MemoryStack stack = MemoryStack.stackPush()) {
        framebuffer.beginRenderPass(this.currentCmdBuffer, renderPass, stack);
      }

      this.boundFramebuffer = framebuffer;
      this.boundRenderPass = renderPass;
      setViewportState(0, 0, framebuffer.getWidth(), framebuffer.getHeight());
      setScissor(0, 0, framebuffer.getWidth(), framebuffer.getHeight());
    }

    return true;
  }

  public void addUsedPipeline(Pipeline pipeline) {
    this.usedPipelines.add(pipeline);
  }

  public void removeUsedPipeline(Pipeline pipeline) {
    this.usedPipelines.remove(pipeline);
  }

  private void waitFences() {
    Synchronization.INSTANCE.waitFences();
  }

  private void resetDescriptors() {
    for (Pipeline pipeline : this.usedPipelines) {
      pipeline.resetDescriptorPool(currentFrame);
    }

    this.usedPipelines.clear();
    this.boundPipeline = null;
    this.boundPipelineHandle = 0L;
  }

  void waitForSwapChain() {
    VK10.vkResetFences(device, this.inFlightFences.get(currentFrame));

    try (MemoryStack stack = MemoryStack.stackPush()) {
      VkSubmitInfo info =
          VkSubmitInfo.calloc(stack)
              .sType$Default()
              .pWaitSemaphores(stack.longs(this.imageAvailableSemaphores.get(currentFrame)))
              .pWaitDstStageMask(stack.ints(65536));
      VK10.vkQueueSubmit(
          DeviceManager.getGraphicsQueue().vkQueue(), info, this.inFlightFences.get(currentFrame));
      VK10.vkWaitForFences(device, this.inFlightFences.get(currentFrame), true, -1L);
    }
  }

  private void recreateSwapChain() {
    this.submitUploads();
    this.waitFences();
    Vulkan.waitIdle();
    this.mainCommandBuffers.forEach(commandBuffer -> VK10.vkResetCommandBuffer(commandBuffer, 0));
    this.recordingCmds = false;
    this.swapChain.recreate();
    this.destroySyncObjects();
    int newFramesNum = Initializer.CONFIG.frameQueueSize;
    if (this.framesNum != newFramesNum) {

      this.framesNum = newFramesNum;
      MemoryManager.getInstance().freeAllBuffers();
      MemoryManager.createInstance(newFramesNum);
      Vulkan.createStagingBuffers();
      this.allocateCommandBuffers();
      Pipeline.recreateDescriptorSets(this.framesNum);
      this.drawer.createResources(this.framesNum);
    }

    this.createSyncObjects();
    this.mainPass.onResize();
    this.onResizeCallbacks.forEach(Runnable::run);
    ((WindowAccessor) (Object) Minecraft.getInstance().getWindow()).getEventHandler().resizeGui();
    currentFrame = 0;
  }

  public void cleanUpResources() {
    this.destroySyncObjects();
    this.drawer.cleanUpResources();
    this.mainPass.cleanUp();
    this.swapChain.cleanUp();
    VTextureSelector.getWhiteTexture().free();
  }

  private void destroySyncObjects() {
    for (int i = 0; i < this.framesNum; i++) {
      VK10.vkDestroyFence(device, this.inFlightFences.get(i), null);
      VK10.vkDestroySemaphore(device, this.imageAvailableSemaphores.get(i), null);
    }

    for (int i = 0; i < this.swapChain.getImagesNum(); i++) {
      VK10.vkDestroySemaphore(device, this.renderFinishedSemaphores.get(i), null);
    }
  }

  public void addOnResizeCallback(Runnable runnable) {
    this.onResizeCallbacks.add(runnable);
  }

  public void bindGraphicsPipeline(GraphicsPipeline pipeline) {
    VkCommandBuffer commandBuffer = this.currentCmdBuffer;
    PipelineState currentState = PipelineState.getCurrentPipelineState(this.boundRenderPass);
    long handle = pipeline.getHandle(currentState);
    if (this.boundPipelineHandle != handle) {
      VK10.vkCmdBindPipeline(commandBuffer, 0, handle);
      this.boundPipelineHandle = handle;
      this.boundPipeline = pipeline;
      this.addUsedPipeline(pipeline);
    }
  }

  public void uploadAndBindUBOs(Pipeline pipeline) {
    VkCommandBuffer commandBuffer = this.currentCmdBuffer;
    pipeline.bindDescriptorSets(commandBuffer, currentFrame);
  }

  public void pushConstants(Pipeline pipeline) {
    VkCommandBuffer commandBuffer = this.currentCmdBuffer;
    PushConstants pushConstants = pipeline.getPushConstants();

    try (MemoryStack stack = MemoryStack.stackPush()) {
      ByteBuffer buffer = stack.malloc(pushConstants.getSize());
      long ptr = MemoryUtil.memAddress0(buffer);
      pushConstants.update(ptr);
      VK10.nvkCmdPushConstants(
          commandBuffer, pipeline.getLayout(), 1, 0, pushConstants.getSize(), ptr);
    }
  }

  public Pipeline getBoundPipeline() {
    return this.boundPipeline;
  }

  public void setBoundFramebuffer(Framebuffer framebuffer) {
    this.boundFramebuffer = framebuffer;
  }

  public Framebuffer getBoundFramebuffer() {
    return this.boundFramebuffer;
  }

  public void setBoundRenderPass(RenderPass boundRenderPass) {
    this.boundRenderPass = boundRenderPass;
  }

  public RenderPass getBoundRenderPass() {
    return this.boundRenderPass;
  }

  public void setMainPass(MainPass mainPass) {
    this.mainPass = mainPass;
  }

  public MainPass getMainPass() {
    return this.mainPass;
  }

  public SwapChain getSwapChain() {
    return this.swapChain;
  }

  private static void resetDynamicState(VkCommandBuffer commandBuffer) {
    VK10.vkCmdSetDepthBias(commandBuffer, 0.0F, 0.0F, 0.0F);
    VK10.vkCmdSetLineWidth(commandBuffer, 1.0F);
  }

  public static void setDepthBias(float constant, float slope) {
    VkCommandBuffer commandBuffer = INSTANCE.currentCmdBuffer;
    VK10.vkCmdSetDepthBias(commandBuffer, constant, 0.0F, slope);
  }

  public static void clearAttachments(int attachments) {
    clearAttachments(INSTANCE.currentCmdBuffer, attachments);
  }

  public static void clearAttachments(VkCommandBuffer commandBuffer, int attachments) {
    Framebuffer framebuffer = getInstance().boundFramebuffer;
    if (framebuffer != null) {
      clearAttachments(commandBuffer, attachments, framebuffer.getWidth(), framebuffer.getHeight());
    }
  }

  public static void clearAttachments(int attachments, int width, int height) {
    clearAttachments(INSTANCE.currentCmdBuffer, attachments, width, height);
  }

  public static void clearAttachments(int attachments, int x, int y, int width, int height) {
    clearAttachments(INSTANCE.currentCmdBuffer, attachments, x, y, width, height);
  }

  public static void clearAttachments(
      VkCommandBuffer commandBuffer, int attachments, int width, int height) {
    clearAttachments(commandBuffer, attachments, 0, 0, width, height);
  }

  public static void clearAttachments(
      VkCommandBuffer commandBuffer, int attachments, int x, int y, int width, int height) {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      VkClearValue colorValue = VkClearValue.calloc(stack);
      colorValue.color().float32(VRenderSystem.clearColor);
      VkClearValue depthValue = VkClearValue.calloc(stack);
      depthValue.depthStencil().set(VRenderSystem.clearDepthValue, 0);
      int attachmentsCount = attachments == 16640 ? 2 : 1;
      Buffer pAttachments = VkClearAttachment.malloc(attachmentsCount, stack);
      switch (attachments) {
        case 256:
          {
            VkClearAttachment clearDepth = pAttachments.get(0);
            clearDepth.aspectMask(2);
            clearDepth.colorAttachment(0);
            clearDepth.clearValue(depthValue);
            break;
          }
        case 16384:
          {
            VkClearAttachment clearColor = pAttachments.get(0);
            clearColor.aspectMask(1);
            clearColor.colorAttachment(0);
            clearColor.clearValue(colorValue);
            break;
          }
        case 16640:
          {
            VkClearAttachment clearColor = pAttachments.get(0);
            clearColor.aspectMask(1);
            clearColor.colorAttachment(0);
            clearColor.clearValue(colorValue);
            VkClearAttachment clearDepth = pAttachments.get(1);
            clearDepth.aspectMask(2);
            clearDepth.colorAttachment(0);
            clearDepth.clearValue(depthValue);
            break;
          }
        default:
          throw new RuntimeException("unexpected value");
      }

      VkRect2D renderArea = VkRect2D.malloc(stack);
      renderArea.offset().set(x, y);
      renderArea.extent().set(width, height);
      org.lwjgl.vulkan.VkClearRect.Buffer pRect = VkClearRect.malloc(1, stack);
      pRect.rect(renderArea);
      pRect.baseArrayLayer(0);
      pRect.layerCount(1);
      VK10.vkCmdClearAttachments(commandBuffer, pAttachments, pRect);
    }
  }

  public static void setInvertedViewport(int x, int y, int width, int height) {
    setViewportState(x, y + height, width, -height);
  }

  public static void resetViewport() {
    Framebuffer framebuffer = INSTANCE.getMainPass().getMainFramebuffer();
    if (framebuffer != null) {
      int width = framebuffer.getWidth();
      int height = framebuffer.getHeight();
      if (width > 0 && height > 0) {
        setViewportState(0, 0, width, height);
      }
    }
  }

  public static void setViewportState(int x, int y, int width, int height) {
    setViewport(x, y, width, height);
  }

  public static void setViewport(int x, int y, int width, int height) {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      setViewport(x, y, width, height, stack);
    }
  }

  public static void setViewport(int x, int y, int width, int height, MemoryStack stack) {
    if (INSTANCE.recordingCmds) {
      org.lwjgl.vulkan.VkViewport.Buffer viewport = VkViewport.malloc(1, stack);
      viewport.x(x);
      viewport.y(height + y);
      viewport.width(width);
      viewport.height(-height);
      viewport.minDepth(0.0F);
      viewport.maxDepth(1.0F);
      VK10.vkCmdSetViewport(INSTANCE.currentCmdBuffer, 0, viewport);
    }
  }

  public static void setScissor(int x, int y, int width, int height) {
    if (INSTANCE.recordingCmds && INSTANCE.boundFramebuffer != null) {
      try (MemoryStack stack = MemoryStack.stackPush()) {
        Framebuffer framebuffer = INSTANCE.boundFramebuffer;
        int framebufferHeight = framebuffer.getHeight();
        x = Math.max(0, x);
        width = Math.min(width, framebuffer.getWidth());
        org.lwjgl.vulkan.VkRect2D.Buffer scissor = VkRect2D.malloc(1, stack);
        scissor.offset().set(x, framebufferHeight - (y + height));
        scissor.extent().set(width, height);
        VK10.vkCmdSetScissor(INSTANCE.currentCmdBuffer, 0, scissor);
      }
    }
  }

  public static void resetScissor() {
    if (INSTANCE.boundFramebuffer != null) {
      try (MemoryStack stack = MemoryStack.stackPush()) {
        org.lwjgl.vulkan.VkRect2D.Buffer scissor = INSTANCE.boundFramebuffer.scissor(stack);
        VK10.vkCmdSetScissor(INSTANCE.currentCmdBuffer, 0, scissor);
      }
    }
  }

  public static void pushDebugSection(String s) {}

  public static void popDebugSection() {}

  public static void popPushDebugSection(String s) {
    popDebugSection();
    pushDebugSection(s);
  }

  public static int getFramesNum() {
    return INSTANCE.framesNum;
  }

  public static VkCommandBuffer getCommandBuffer() {
    return INSTANCE.currentCmdBuffer;
  }

  public static boolean isRecording() {
    return INSTANCE.recordingCmds;
  }

  public static void scheduleSwapChainUpdate() {
    swapChainUpdate = true;
  }
}
