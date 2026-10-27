// Optional production GPU regression harness; see graphics-settings/tools/README.md.
package com.shnewbs.deepprimal.validation;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.nio.file.*;
import net.vulkanmod.vulkan.shader.converter.*;

public final class FsrVulkanShaderChecks {
  public static void main(String[] args) throws Exception {
    Path source=Path.of(args[0]),out=Path.of(args[1]);Files.createDirectories(out);
    for(String name:new String[]{"fsr_easu","fsr_rcas"}) {
      GLSLParser parser=new GLSLParser();
      parser.setVertexFormat(DefaultVertexFormat.EMPTY);
      parser.parse(new Lexer(Files.readString(source.resolve("fsr_fullscreen.vsh"))),GLSLParser.Stage.VERTEX);
      parser.parse(new Lexer(Files.readString(source.resolve(name+".fsh"))),GLSLParser.Stage.FRAGMENT);
      var ubos=parser.createUBOs();var samplers=parser.getSamplerList();
      int expected=name.equals("fsr_easu")?64:16;
      if(ubos.length!=1||ubos[0].getSize()!=expected||samplers.size()!=1)
        throw new AssertionError("Incorrect Vulkan descriptor layout "+name);
      String vertex=parser.getOutput(GLSLParser.Stage.VERTEX);
      String fragment=parser.getOutput(GLSLParser.Stage.FRAGMENT);
      if(!fragment.contains("#define FSR_OUT out")||!fragment.contains("FSR_OUT"))
        throw new AssertionError("FSR output parameter qualifiers lost");
      Files.writeString(out.resolve(name+".vert"),vertex);
      Files.writeString(out.resolve(name+".frag"),fragment);
      System.out.println("FSR_VULKAN_CONVERTER_PASS kernel="+name+" uniformBytes="+ubos[0].getSize()+" samplers="+samplers.size());
    }
  }
}
