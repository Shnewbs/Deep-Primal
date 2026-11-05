package net.vulkanmod.deepprimal;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

/** GLFW lives in FML's parent loader; use opaque handles at the Vulkan boundary. */
public final class GlfwVulkanBridge {
    private static long function(String name) {
        long address = GLFW.getLibrary().getFunctionAddress(name);
        if (address == 0) throw new IllegalStateException("Missing GLFW entry point: " + name);
        return address;
    }
    public static PointerBuffer extensions() {
        try (var stack = MemoryStack.stackPush()) {
            var count = stack.callocInt(1);
            long pointer = JNI.invokePP(MemoryUtil.memAddress(count), function("glfwGetRequiredInstanceExtensions"));
            if (pointer == 0) throw new IllegalStateException("GLFW cannot find a Vulkan surface extension");
            return MemoryUtil.memPointerBuffer(pointer, count.get(0));
        }
    }
    public static int createSurface(long instance, long window, long destination) {
        return JNI.invokePPPPI(instance, window, 0L, destination, function("glfwCreateWindowSurface"));
    }
    private GlfwVulkanBridge() {}
}
