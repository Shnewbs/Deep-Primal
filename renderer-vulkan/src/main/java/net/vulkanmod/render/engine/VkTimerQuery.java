package net.vulkanmod.render.engine;

import com.mojang.blaze3d.systems.GpuQuery;
import java.util.OptionalLong;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.device.DeviceManager;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkQueryPoolCreateInfo;

/** Two device timestamps; no CPU time is reported as GPU time. */
final class VkTimerQuery implements GpuQuery {
    private long pool;
    private boolean ended;
    VkTimerQuery() {
        var renderer = Renderer.getInstance(); renderer.endRenderPass();
        try (var stack = MemoryStack.stackPush()) {
            var info = VkQueryPoolCreateInfo.calloc(stack).sType(VK10.VK_STRUCTURE_TYPE_QUERY_POOL_CREATE_INFO)
                .queryType(VK10.VK_QUERY_TYPE_TIMESTAMP).queryCount(2);
            var result = stack.mallocLong(1);
            int status = VK10.vkCreateQueryPool(DeviceManager.vkDevice, info, null, result);
            if (status != VK10.VK_SUCCESS) throw new IllegalStateException("Cannot create timestamp pool: " + status);
            pool = result.get(0);
            VK10.vkCmdResetQueryPool(Renderer.getCommandBuffer(), pool, 0, 2);
            VK10.vkCmdWriteTimestamp(Renderer.getCommandBuffer(), VK10.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, pool, 0);
        }
    }
    void end() {
        if (ended || pool == 0) throw new IllegalStateException("Timestamp query already ended/closed");
        VK10.vkCmdWriteTimestamp(Renderer.getCommandBuffer(), VK10.VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT, pool, 1); ended = true;
    }
    public OptionalLong getValue() {
        if (!ended || pool == 0) return OptionalLong.empty();
        try (var stack = MemoryStack.stackPush()) {
            var values = stack.mallocLong(4);
            int status = VK10.vkGetQueryPoolResults(DeviceManager.vkDevice, pool, 0, 2, values, 16,
                VK10.VK_QUERY_RESULT_64_BIT | VK10.VK_QUERY_RESULT_WITH_AVAILABILITY_BIT);
            if (status == VK10.VK_NOT_READY || values.get(1) == 0 || values.get(3) == 0) return OptionalLong.empty();
            if (status != VK10.VK_SUCCESS) throw new IllegalStateException("Timestamp query failed: " + status);
            return OptionalLong.of(Math.max(1L, (long)((values.get(2)-values.get(0))*DeviceManager.deviceProperties.limits().timestampPeriod())));
        }
    }
    public void close() {
        if (pool == 0) return;
        // Cancellation may happen before the containing frame has completed.
        long retired = pool; pool = 0;
        net.vulkanmod.vulkan.memory.MemoryManager.getInstance().addFrameOp(
            () -> VK10.vkDestroyQueryPool(DeviceManager.vkDevice, retired, null));
    }
}
