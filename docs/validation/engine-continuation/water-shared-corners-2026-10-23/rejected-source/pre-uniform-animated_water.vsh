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
flat out vec4 previousBounds;
out vec2 previousTexCoord;
flat out float previousWeight;
flat out ivec4 waterCorners;
out vec4 vertexColor;
out vec3 relativePosition;
#moj_import <deepprimal_graphics:water_flow.glsl>
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
    bool spatial = UV0.x < -1.0;
    int heightOffset = spatial ? (UV1.y & 255) : UV1.y;
    if (spatial && heightOffset > 127) heightOffset -= 256;
    float t = clamp((TextureMat[0][0] - float(heightOffset)) / 100.0, 0.0, 1.0);
    vec3 p = Position;
    float displacement = float(UV1.x) / 16384.0 * (1.0 - t);
    p.y += displacement;
    relativePosition = p + ModelOffset;
    gl_Position = ProjMat * ModelViewMat * vec4(relativePosition, 1.0);
    // Active material blends reuse the otherwise constant color alpha for cell coordinates.
    vec4 tint = Color;
    if (spatial || (UV2.y & 32768) != 0) tint.a = 1.0;
    vertexColor = tint * ColorModulator * texelFetch(Sampler2, clamp(ivec2(UV2.x & 255, UV2.y & 255) / 16, ivec2(0), ivec2(15)), 0);
    // Signed normalized Normal.z stores the original opacity start offset in milliseconds.
    float alphaT = clamp((TextureMat[0][0] - Normal.z * 127.0) / 100.0, 0.0, 1.0);
    vertexColor.a *= mix(Normal.x, Normal.y, alphaT);
    texCoord = UV0;
    vec4 flowTile = vec4(TextureMat[0][1], TextureMat[0][3], TextureMat[0][2], TextureMat[1][2]);
    vec4 stillTile = vec4(TextureMat[1][3], TextureMat[2][0], TextureMat[2][2], TextureMat[1][0]);
    waterCorners = ivec4(0);
    if (spatial) {
        int to = int(round(-UV0.x)) - 2, from = int(round(UV0.y));
        int fourthAge = ((to >> 20) & 15) | (((from >> 20) & 7) << 4);
        int ages = ((UV1.y >> 8) & 255) | (((UV2.x >> 8) & 255) << 8)
                 | (((UV2.y >> 8) & 255) << 16) | (fourthAge << 24);
        int cellBits = int(round(Color.a * 255.0));
        waterCorners = ivec4(to & 1048575, from & 1048575, ages, cellBits);
        flowBounds = flowTile; previousBounds = stillTile;
        texCoord = Position.xz - vec2(cellBits & 15, cellBits >> 4);
        previousTexCoord = vec2(0.0); previousWeight = -1.0;
        return;
    }
    flowBounds = (UV2.x & 1024) != 0 ? flowTile : vec4(0.0);
    previousBounds = vec4(0.0);
    previousWeight = (UV2.y & 32768) != 0
        ? clamp(1.0 - (TextureMat[0][0] + float((UV2.y >> 8) & 127)) / 100.0, 0.0, 1.0) : 0.0;
    previousTexCoord = UV0;
    if (previousWeight > 0.0) {
        int sourceDirection = clamp((UV2.x >> 11) & 31, 0, 16);
        if ((UV2.x & 256) != 0) {
            bool currentFlow = (UV2.x & 512) != 0;
            bool oldFlow = sourceDirection < 16;
            vec4 currentTile = currentFlow ? flowTile : stillTile;
            vec4 oldTile = oldFlow ? flowTile : stillTile;
            float currentScale = currentFlow ? 0.5 : 1.0;
            float oldScale = oldFlow ? 0.5 : 1.0;
            // Convert admitted UVs to cell-local coordinates, then apply the same
            // displayed height displacement to both material endpoints.
            vec2 localUV = (UV0 - currentTile.xy) / (currentTile.zw * currentScale);
            localUV.y -= displacement;
            previousTexCoord = oldTile.xy + localUV * oldTile.zw * oldScale;
            // Negative width marks nonperiodic side bounds for fragment-space clamping.
            previousBounds = vec4(oldTile.xy, -oldTile.z, oldTile.w * oldScale);
        } else if (sourceDirection < 16) {
            previousBounds = flowTile;
            previousTexCoord = previousFlow(sourceDirection, flowTile);
        } else {
            int cellBits = int(round(Color.a * 255.0));
            vec2 cell = vec2(cellBits & 15, cellBits >> 4);
            previousTexCoord = stillTile.xy + (Position.xz - cell) * stillTile.zw;
        }
    }

    if ((UV2.x & 256) != 0) {
        bool flowing = (UV2.x & 512) != 0;
        float span = flowing ? TextureMat[1][1] : TextureMat[1][0];
        texCoord.y = UV0.y - displacement * span;
        vec4 tile = flowing ? flowTile : stillTile;
        flowBounds = vec4(tile.xy, -tile.z, span);
    }
}
