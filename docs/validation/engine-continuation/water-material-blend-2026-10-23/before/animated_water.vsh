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
out vec2 previousTexCoord;
flat out float previousWeight;
out vec4 vertexColor;
out vec3 relativePosition;
// Q25 coefficients keep world-origin phase exact using uint modular arithmetic.
const ivec2 FLOW_Q25[16] = ivec2[16](
    ivec2(33554432, 0),
    ivec2(31000252, 12840725),
    ivec2(23726566, 23726566),
    ivec2(12840725, 31000252),
    ivec2(0, 33554432),
    ivec2(-12840725, 31000252),
    ivec2(-23726566, 23726566),
    ivec2(-31000252, 12840725),
    ivec2(-33554432, 0),
    ivec2(-31000252, -12840725),
    ivec2(-23726566, -23726566),
    ivec2(-12840725, -31000252),
    ivec2(0, -33554432),
    ivec2(12840725, -31000252),
    ivec2(23726566, -23726566),
    ivec2(31000252, -12840725));
vec2 previousFlow(int direction, vec4 tile) {
    ivec2 c = FLOW_Q25[direction];
    int cellBits = int(round(Color.a * 255.0));
    ivec2 cell = ivec2(cellBits & 15, cellBits >> 4);
    uvec2 world = uvec2(ivec2(TextureMat[3][0], TextureMat[3][1]) + cell);
    uvec2 phase = uvec2(uint(c.y) * world.x - uint(c.x) * world.y,
                       uint(c.x) * world.x + uint(c.y) * world.y) & uvec2(67108863u);
    vec2 f = vec2(c) / 33554432.0, local = Position.xz - vec2(cell) - vec2(0.5);
    vec2 uv = vec2(0.5) + 0.5 * vec2(f.y * local.x - f.x * local.y, f.x * local.x + f.y * local.y)
              + vec2(phase) / 67108864.0;
    return tile.xy + uv * tile.zw;
}
void main() {
    // TextureMat carries elapsed milliseconds since this mesh was uploaded.
    float t = clamp((TextureMat[0][0] - float(UV1.y)) / 100.0, 0.0, 1.0);
    vec3 p = Position;
    float displacement = float(UV1.x) / 16384.0 * (1.0 - t);
    p.y += displacement;
    relativePosition = p + ModelOffset;
    gl_Position = ProjMat * ModelViewMat * vec4(relativePosition, 1.0);
    // Active top blends reuse the otherwise constant color alpha for cell coordinates.
    vec4 tint = Color;
    if ((UV2.y & 32768) != 0) tint.a = 1.0;
    vertexColor = tint * ColorModulator * texelFetch(Sampler2, clamp(ivec2(UV2.x & 255, UV2.y & 255) / 16, ivec2(0), ivec2(15)), 0);
    // Signed normalized Normal.z stores the original opacity start offset in milliseconds.
    float alphaT = clamp((TextureMat[0][0] - Normal.z * 127.0) / 100.0, 0.0, 1.0);
    vertexColor.a *= mix(Normal.x, Normal.y, alphaT);
    texCoord = UV0;
    flowBounds = (UV2.x & 1024) != 0
        ? vec4(TextureMat[0][1], TextureMat[0][3], TextureMat[0][2], TextureMat[1][2])
        : vec4(0.0);
    previousWeight = (UV2.y & 32768) != 0
        ? clamp(1.0 - (TextureMat[0][0] + float((UV2.y >> 8) & 127)) / 100.0, 0.0, 1.0) : 0.0;
    previousTexCoord = UV0;
    if (previousWeight > 0.0) previousTexCoord = previousFlow(clamp((UV2.x >> 11) & 31, 0, 15), flowBounds);

    if ((UV2.x & 256) != 0) {
        bool flowing = (UV2.x & 512) != 0;
        float span = flowing ? TextureMat[1][1] : TextureMat[1][0];
        float start = flowing ? TextureMat[2][1] : TextureMat[2][0];
        texCoord.y = clamp(UV0.y - displacement * span, start, start + span);
    }
}
