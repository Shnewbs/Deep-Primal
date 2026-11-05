// Optional production GPU regression harness; see graphics-settings/tools/README.md.
package com.shnewbs.deepprimal.validation;
import com.mojang.blaze3d.opengl.GlTexture;

import com.mojang.blaze3d.systems.*;
import com.mojang.blaze3d.textures.*;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.shaders.*;
import com.shnewbs.deepprimal.graphics.upscaling.Fsr1Upscaler;
import java.nio.*;
import java.nio.file.*;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

public final class FsrGpuApiChecks {
  static boolean missing;
  public static void main(String[] args) throws Exception {
    RenderSystem.initRenderThread();
    if (!GLFW.glfwInit()) throw new AssertionError("GLFW init failed");
    GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
    GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
    GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 5);
    GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
    long window=GLFW.glfwCreateWindow(64,64,"FSR API regression",0,0);
    if (window==0) throw new AssertionError("GLFW window failed");
    Path shaderDir=Path.of(args[0]);
    ShaderSource shaderSource=(id,type)->{
      if (missing) return null;
      try { return Files.readString(shaderDir.resolve(id.getPath()+(type==ShaderType.VERTEX?".vsh":".fsh"))); }
      catch(Exception e) { throw new IllegalStateException(e); }
    };
    var constructor=Class.forName("com.mojang.blaze3d.opengl.GlDevice").getDeclaredConstructor(long.class,ShaderSource.class,GpuDebugOptions.class);
    constructor.setAccessible(true);
    var backend=(GpuDeviceBackend)constructor.newInstance(window,shaderSource,new GpuDebugOptions(1,true,true));
    var device=new GpuDevice(backend);
    RenderSystem.initRenderer(device);
    var fsr=new Fsr1Upscaler();
    if(!fsr.isReady())throw new AssertionError(fsr.failureReason());
    int renders=0;
    for(int[] size:new int[][]{{7,5,13,9},{1,1,3,5},{31,19,41,29},{16,8,16,8}}) {
      var src=device.createTexture("FSR check input",GpuTexture.USAGE_TEXTURE_BINDING|GpuTexture.USAGE_COPY_DST,TextureFormat.RGBA8,size[0],size[1],1,1);
      var dst=device.createTexture("FSR check output",GpuTexture.USAGE_RENDER_ATTACHMENT|GpuTexture.USAGE_COPY_SRC,TextureFormat.RGBA8,size[2],size[3],1,1);
      var sv=device.createTextureView(src);var dv=device.createTextureView(dst);
      try(var image=new NativeImage(size[0],size[1],false)) {
        for(int y=0;y<size[1];y++)for(int x=0;x<size[0];x++)image.setPixelABGR(x,y,0xffd18535);
        device.createCommandEncoder().writeToTexture(src,image);
      }
      for(float strength:new float[]{0,.5f,1}) {
        fsr.upscale(sv,dv,strength);
        GL11.glFinish();
        ByteBuffer pixels=MemoryUtil.memAlloc(size[2]*size[3]*4);
        try {
          GL45.glGetTextureImage(((GlTexture)dst).glId(),0,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,pixels);
          for(int n=0;n<pixels.capacity();n+=4) {
            if(Math.abs(Byte.toUnsignedInt(pixels.get(n))-53)>1
                ||Math.abs(Byte.toUnsignedInt(pixels.get(n+1))-133)>1
                ||Math.abs(Byte.toUnsignedInt(pixels.get(n+2))-209)>1
                ||Byte.toUnsignedInt(pixels.get(n+3))!=255)
              throw new AssertionError("Unexpected API output at "+n+" dimensions="+java.util.Arrays.toString(size)+" sharpness="+strength);
          }
          if(GL11.glGetError()!=0)throw new AssertionError("OpenGL error");
        } finally { MemoryUtil.memFree(pixels); }
        if(src.isClosed()||dst.isClosed()||sv.isClosed()||dv.isClosed())throw new AssertionError("Caller-owned resource closed");
        renders++;
      }
      boolean rejected=false;
      try { fsr.upscale(sv,sv,.5f); } catch(IllegalArgumentException expected) { rejected=true; }
      if(!rejected)throw new AssertionError("Alias accepted");
      sv.close();dv.close();src.close();dst.close();
    }
    device.clearPipelineCache();missing=true;fsr.invalidateShaders();
    if(fsr.isReady()||fsr.failureReason()==null)throw new AssertionError("Missing shaders accepted");
    missing=false;
    if(fsr.isReady())throw new AssertionError("Shader failure was not latched");
    device.clearPipelineCache();fsr.invalidateShaders();
    if(!fsr.isReady())throw new AssertionError("Shader reload did not recover: "+fsr.failureReason());
    fsr.close();fsr.close();
    if(fsr.isReady())throw new AssertionError("Closed instance became ready");
    System.out.println("FSR_GPU_API_PASS renders="+renders+" resizeCases=4 shaderReload=true missingShadersRejected=true failureLatched=true aliasRejected=true callerOwnership=true idempotentClose=true backend="+device.getRenderer());
    device.close();GLFW.glfwDestroyWindow(window);GLFW.glfwTerminate();
  }
}
