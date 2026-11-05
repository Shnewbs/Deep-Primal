package com.shnewbs.deepprimal.client;

import com.mojang.blaze3d.shaders.ShaderType;
import net.minecraft.resources.Identifier;

/** Scoped source additions retain the platform's fog, filtering, lightmap and item lighting. */
public final class GroundMaterialShaders {
  public static String patch(Identifier location, ShaderType type, String source) {
    boolean unlitItem = location.equals(Identifier.fromNamespaceAndPath("neoforge", "shaders/core/item_unlit.vsh"))
        && type == ShaderType.VERTEX;
    if (!location.getNamespace().equals("minecraft") && !unlitItem) return source;
    String extension = type == ShaderType.VERTEX ? ".vsh" : ".fsh";
    boolean terrain = location.getPath().equals("shaders/core/terrain" + extension);
    boolean entity = location.getPath().equals("shaders/core/entity" + extension);
    boolean item = location.getPath().equals("shaders/core/item" + extension) || unlitItem;
    boolean block = location.getPath().equals("shaders/core/block" + extension);
    if (!terrain && !entity && !item && !block) return source;
    if (type == ShaderType.VERTEX) {
      source = replace(source, "void main() {",
          "flat out int primalGround;\n" + (!entity ? "out vec3 primalSoilLight;\n" : "out vec2 primalSoilShade;\n")
          + "void main() {\n"
          + "    primalGround = (UV2.x & 16384) != 0 ? 1 : 0;\n");
      if (!unlitItem) {
        source = replace(source, "sample_lightmap(Sampler2, UV2)",
            "sample_lightmap(Sampler2, ivec2(UV2.x & 49151, UV2.y))");
      }
      if (!entity) {
        source = replace(source, "texCoord0 = UV0;",
            (unlitItem ? "primalSoilLight = vec3(Color.a);\n"
                : "primalSoilLight = sample_lightmap(Sampler2, ivec2(UV2.x & 49151, UV2.y)).rgb * Color.a;\n")
            + (item && !unlitItem ? "    primalSoilLight *= minecraft_mix_light(Light0_Direction, Light1_Direction, Normal, vec4(1.0)).r;\n" : "")
            + "    if (primalGround != 0) vertexColor.a = 1.0;\n    texCoord0 = UV0;");
      } else {
        source = replace(source, "texCoord0 = UV0;", """
            #ifdef PER_FACE_LIGHTING
                primalSoilShade = vec2(minecraft_mix_light_separate(-light, vec4(1.0)).r,
                    minecraft_mix_light_separate(light, vec4(1.0)).r) * Color.a;
            #elif defined(NO_CARDINAL_LIGHTING)
                primalSoilShade = vec2(Color.a);
            #else
                primalSoilShade = vec2(minecraft_mix_light(Light0_Direction, Light1_Direction, Normal, vec4(1.0)).r * Color.a);
            #endif
                texCoord0 = UV0;
            """);
      }
    } else {
      source = replace(source, "void main() {",
          "flat in int primalGround;\n" + (!entity ? "in vec3 primalSoilLight;\n" : "in vec2 primalSoilShade;\n")
          + "void main() {");
      if (terrain) {
        source = replace(source, " * vertexColor;",
            ";\n    color = primalGround != 0\n"
            + "        ? vec4(color.rgb * primalSoilLight"
            + " + color.a * vertexColor.rgb, 1.0) : color * vertexColor;");
      } else if (block) {
        source = replace(source, "vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;",
            "vec4 primalSample = texture(Sampler0, texCoord0);\n"
            + "    vec4 color = primalGround != 0\n"
            + "        ? vec4(primalSample.rgb * primalSoilLight + primalSample.a * vertexColor.rgb, 1.0) * ColorModulator\n"
            + "        : primalSample * vertexColor * ColorModulator;");
      } else {
        source = replace(source, "vec4 color = texture(Sampler0, texCoord0);",
            "vec4 color = texture(Sampler0, texCoord0);\n"
            + "    vec4 primalSample = color;\n    if (primalGround != 0) color.a = 1.0;");
        if (item) {
          source = replace(source, "color *= vertexColor * ColorModulator;",
              "if (primalGround != 0) color = vec4(primalSample.rgb * primalSoilLight"
              + " + primalSample.a * vertexColor.rgb, 1.0) * ColorModulator;\n"
              + "    else color *= vertexColor * ColorModulator;");
        } else source = replace(source, "color *= faceVertexColor * ColorModulator;", """
                if (primalGround != 0) {
            #ifdef PER_FACE_LIGHTING
                    float primalShade = gl_FrontFacing ? primalSoilShade.y : primalSoilShade.x;
            #else
                    float primalShade = primalSoilShade.x;
            #endif
                    color = vec4(primalSample.rgb * primalShade + primalSample.a * faceVertexColor.rgb, 1.0) * ColorModulator;
                } else color *= faceVertexColor * ColorModulator;
            """);
      }
    }
    return source;
  }

  private static String replace(String source, String expected, String replacement) {
    int at = source.indexOf(expected);
    if (at < 0 || source.indexOf(expected, at + expected.length()) >= 0)
      throw new IllegalStateException("Ground material shader contract changed: " + expected);
    return source.substring(0, at) + replacement + source.substring(at + expected.length());
  }

  private GroundMaterialShaders() {}
}
