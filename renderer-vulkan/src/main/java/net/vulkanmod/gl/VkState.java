// Adapted from VulkanMod's LGPL-3.0-only GlStateManagerM, Collateral and contributors.
package net.vulkanmod.gl;
import com.mojang.blaze3d.systems.RenderSystem;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.VRenderSystem;
public final class VkState {
   public static void _texSubImage2D(int target, int level, int x, int y, int w, int h, int format, int type, java.nio.ByteBuffer data) {
      VkGlTexture.texSubImage2D(target,level,x,y,w,h,format,type,data);
   }
   public static void _activeTexture(int i) {
      VkGlTexture.activeTexture(i);
   }
   public static void _bindTexture(int i) {
      VkGlTexture.bindTexture(i);
   }
   public static void _blendFuncSeparate(int i, int j, int k, int l) {
      RenderSystem.assertOnRenderThread();
      VRenderSystem.blendFuncSeparate(i, j, k, l);
   }
   public static void _clear(int mask) {
      RenderSystem.assertOnRenderThread();
      VRenderSystem.clear(mask);
   }
   public static void _colorMask(int writeMask) {
      RenderSystem.assertOnRenderThread();
      VRenderSystem.colorMask(writeMask);
   }
   public static void _deleteTexture(int i) {
      RenderSystem.assertOnRenderThread();
      VkGlTexture.glDeleteTextures(i);
   }
   public static void _depthFunc(int i) {
      RenderSystem.assertOnRenderThread();
      VRenderSystem.depthFunc(i);
   }
   public static void _depthMask(boolean bl) {
      RenderSystem.assertOnRenderThread();
      VRenderSystem.depthMask(bl);
   }
   public static void _disableBlend() {
      RenderSystem.assertOnRenderThread();
      VRenderSystem.disableBlend();
   }
   public static void _disableCull() {
      VRenderSystem.disableCull();
   }
   public static void _disableDepthTest() {
      RenderSystem.assertOnRenderThread();
      VRenderSystem.disableDepthTest();
   }
   public static void _disablePolygonOffset() {
      RenderSystem.assertOnRenderThread();
      VRenderSystem.disablePolygonOffset();
   }
   public static void _disableScissorTest() {
      Renderer.resetScissor();
   }
   public static void _enableBlend() {
      RenderSystem.assertOnRenderThread();
      VRenderSystem.enableBlend();
   }
   public static void _enableCull() {
      VRenderSystem.enableCull();
   }
   public static void _enableDepthTest() {
      RenderSystem.assertOnRenderThread();
      VRenderSystem.enableDepthTest();
   }
   public static void _enablePolygonOffset() {
      RenderSystem.assertOnRenderThread();
      VRenderSystem.enablePolygonOffset();
   }
   public static void _enableScissorTest() {
   }
   public static void _pixelStore(int pname, int param) {
      RenderSystem.assertOnRenderThread();
      VkGlTexture.pixelStoreI(pname, param);
   }
   public static void _polygonMode(int face, int mode) {
      RenderSystem.assertOnRenderThread();
      VRenderSystem.setPolygonModeGL(mode);
   }
   public static void _polygonOffset(float f, float g) {
      RenderSystem.assertOnRenderThread();
      VRenderSystem.polygonOffset(f, g);
   }
   public static void _scissorBox(int x, int y, int width, int height) {
      Renderer.setScissor(x, y, width, height);
   }
   public static void _texSubImage2D(int target, int level, int offsetX, int offsetY, int width, int height, int format, int type, long pixels) {
      RenderSystem.assertOnRenderThread();
      VkGlTexture.texSubImage2D(target, level, offsetX, offsetY, width, height, format, type, pixels);
   }
   public static void _viewport(int x, int y, int width, int height) {
      Renderer.setViewport(x, y, width, height);
   }
}
