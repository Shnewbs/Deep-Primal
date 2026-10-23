#version 330
#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <deepprimal_graphics:water_flow.glsl>
uniform sampler2D Sampler0;
in vec2 texCoord;
flat in vec4 flowBounds;
flat in vec4 previousBounds;
in vec2 previousTexCoord;
flat in float previousWeight;
flat in ivec4 waterCorners;
in vec2 cornerSourceTexCoord;
in vec4 vertexColor;
in vec3 relativePosition;
out vec4 fragColor;
vec4 spatialSample(int direction, vec2 local, vec2 dx, vec2 dy) {
    int to = waterCorners.x, from = waterCorners.y;
    if ((to == ((to & 31) * 33825) && direction == (to & 31))
        || (from == ((from & 31) * 33825) && direction == (from & 31))) {
        vec4 tile = direction == 16 ? previousBounds : flowBounds;
        vec2 interpolated = to == ((to & 31) * 33825) && direction == (to & 31)
            ? previousTexCoord : cornerSourceTexCoord;
        vec2 uv = tile.xy + fract((interpolated - tile.xy) / tile.zw) * tile.zw;
        return textureGrad(Sampler0, uv, dFdx(interpolated), dFdy(interpolated));
    }

    if (direction == 16) {
        vec2 uv = previousBounds.xy + fract(local) * previousBounds.zw;
        return textureGrad(Sampler0, uv, dx * previousBounds.zw, dy * previousBounds.zw);
    }
    ivec2 c = FLOW_Q25[direction];
    ivec2 cell = ivec2(waterCorners.w & 15, waterCorners.w >> 4);
    uvec2 world = uvec2(ivec2(TextureMat[3][0], TextureMat[3][1]) + cell);
    uvec2 phase = uvec2(uint(c.y) * world.x - uint(c.x) * world.y,
                       uint(c.x) * world.x + uint(c.y) * world.y) & uvec2(67108863u);
    vec2 flow = vec2(c) / 33554432.0, p = local - vec2(0.5);
    vec2 uv = vec2(0.5) + 0.5 * vec2(flow.y*p.x-flow.x*p.y, flow.x*p.x+flow.y*p.y) + vec2(phase)/67108864.0;
    vec2 ux = 0.5 * vec2(flow.y*dx.x-flow.x*dx.y, flow.x*dx.x+flow.y*dx.y);
    vec2 uy = 0.5 * vec2(flow.y*dy.x-flow.x*dy.y, flow.x*dy.x+flow.y*dy.y);
    return textureGrad(Sampler0, flowBounds.xy + fract(uv) * flowBounds.zw, ux * flowBounds.zw, uy * flowBounds.zw);
}
vec4 spatialColor(vec2 local, vec2 dx, vec2 dy) {
    int to = waterCorners.x, from = waterCorners.y;
    // Uniform settled surfaces retain the original single texture evaluation.
    int ages = waterCorners.z;
    int youngest = min(min(ages & 255, (ages >> 8) & 255), min((ages >> 16) & 255, (ages >> 24) & 255));
    bool uniformTo = to == ((to & 31) * 33825), uniformFrom = from == ((from & 31) * 33825);
    if (uniformTo && (to == from || TextureMat[0][0] + float(youngest) >= 100.0))
        return spatialSample(to & 31, local, dx, dy);
    if (uniformTo && uniformFrom) {
        vec2 w = clamp(local, vec2(0.0), vec2(1.0));
        float t = 0.0;
        for (int n = 0; n < 4; n++) {
            float weight = ((n&1)==0 ? 1.0-w.x : w.x) * ((n&2)==0 ? 1.0-w.y : w.y);
            t += weight * clamp((TextureMat[0][0] + float((ages >> (n*8)) & 255)) / 100.0, 0.0, 1.0);
        }
        return mix(spatialSample(from & 31, local, dx, dy), spatialSample(to & 31, local, dx, dy), t);
    }
    vec2 w = clamp(local, vec2(0.0), vec2(1.0));
    vec4 result = vec4(0.0);
    for (int n = 0; n < 4; n++) {
        int target = (to >> (n*5)) & 31, source = (from >> (n*5)) & 31;
        float age = TextureMat[0][0] + float((waterCorners.z >> (n*8)) & 255);
        float weight = ((n&1)==0 ? 1.0-w.x : w.x) * ((n&2)==0 ? 1.0-w.y : w.y);
        if (weight > 0.0) {
            vec4 sampled = spatialSample(target, local, dx, dy);
            if (source != target && age < 100.0)
                sampled = mix(spatialSample(source, local, dx, dy), sampled, clamp(age/100.0,0.0,1.0));
            result += sampled * weight;
        }
    }
    return result;
}
void main() {
    // Keep interpolated world phase unwrapped until sampling; never interpolate a tile seam.
    vec2 uv = flowBounds.z > 0.0
        ? flowBounds.xy + fract((texCoord - flowBounds.xy) / flowBounds.zw) * flowBounds.zw
        : texCoord;
    float texelY = 0.5 / float(textureSize(Sampler0, 0).y);
    if (flowBounds.z < 0.0) {
        float pad = min(texelY, flowBounds.w * 0.5);
        uv.y = clamp(uv.y, flowBounds.y + pad, flowBounds.y + flowBounds.w - pad);
    }
    // Derive LOD from the unwrapped phase so wrap boundaries do not select coarse mips.
    vec2 dx = dFdx(texCoord), dy = dFdy(texCoord);
    vec4 sampled = previousWeight < 0.0 ? spatialColor(texCoord, dx, dy)
        : flowBounds.z != 0.0 ? textureGrad(Sampler0, uv, dx, dy) : texture(Sampler0, uv);
    vec2 oldDx = dFdx(previousTexCoord), oldDy = dFdy(previousTexCoord);
    if (previousWeight > 0.0) {
        vec2 oldUV = previousBounds.z > 0.0
            ? previousBounds.xy + fract((previousTexCoord - previousBounds.xy) / previousBounds.zw) * previousBounds.zw
            : previousTexCoord;
        if (previousBounds.z < 0.0) {
            float pad = min(texelY, previousBounds.w * 0.5);
            oldUV.y = clamp(oldUV.y, previousBounds.y + pad, previousBounds.y + previousBounds.w - pad);
        }
        sampled = mix(sampled, textureGrad(Sampler0, oldUV, oldDx, oldDy), previousWeight);
    }
    vec4 color = sampled * vertexColor;
    if (color.a < 0.003) discard;
    fragColor = apply_fog(color, length(relativePosition), length(relativePosition.xz), FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
