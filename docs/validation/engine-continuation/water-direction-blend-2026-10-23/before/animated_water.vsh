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
flat out vec4 flowBounds;
out vec4 vertexColor;
out vec3 relativePosition;
void main() {
    // TextureMat carries elapsed milliseconds since this mesh was uploaded.
    float t = clamp((TextureMat[0][0] - float(UV1.y)) / 100.0, 0.0, 1.0);
    vec3 p = Position;
    float displacement = float(UV1.x) / 16384.0 * (1.0 - t);
    p.y += displacement;
    relativePosition = p + ModelOffset;
    gl_Position = ProjMat * ModelViewMat * vec4(relativePosition, 1.0);
    vertexColor = Color * ColorModulator * texelFetch(Sampler2, clamp(ivec2(UV2.x & 255, UV2.y) / 16, ivec2(0), ivec2(15)), 0);
    // Signed normalized Normal.z stores the original opacity start offset in milliseconds.
    float alphaT = clamp((TextureMat[0][0] - Normal.z * 127.0) / 100.0, 0.0, 1.0);
    vertexColor.a *= mix(Normal.x, Normal.y, alphaT);
    texCoord = UV0;
    flowBounds = (UV2.x & 1024) != 0
        ? vec4(TextureMat[0][1], TextureMat[0][3], TextureMat[0][2], TextureMat[1][2])
        : vec4(0.0);
    if ((UV2.x & 256) != 0) {
        bool flowing = (UV2.x & 512) != 0;
        float span = flowing ? TextureMat[1][1] : TextureMat[1][0];
        float start = flowing ? TextureMat[2][1] : TextureMat[2][0];
        texCoord.y = clamp(UV0.y - displacement * span, start, start + span);
    }
}
