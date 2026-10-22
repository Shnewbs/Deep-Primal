#version 330
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;
uniform sampler2D Sampler2;
out vec2 texCoord;
out vec4 vertexColor;
out vec3 relativePosition;
void main() {
    // TextureMat carries elapsed milliseconds since this mesh was uploaded.
    float t = clamp((TextureMat[0][0] - float(UV1.y)) / 100.0, 0.0, 1.0);
    vec3 p = Position;
    p.y += float(UV1.x) / 16384.0 * (1.0 - t);
    relativePosition = p + ModelOffset;
    gl_Position = ProjMat * ModelViewMat * vec4(relativePosition, 1.0);
    vertexColor = Color * ColorModulator * texelFetch(Sampler2, clamp(UV2 / 16, ivec2(0), ivec2(15)), 0);
    vertexColor.a *= mix(Normal.x, Normal.y, clamp(TextureMat[0][0] / 100.0, 0.0, 1.0));
    texCoord = UV0;
}
