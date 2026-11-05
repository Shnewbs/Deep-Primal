#version 330
#moj_import <minecraft:fog.glsl>
uniform sampler2D Sampler0;
in vec2 texCoord;
flat in vec4 flowBounds;
flat in vec4 previousBounds;
in vec2 previousTexCoord;
flat in float previousWeight;
in vec4 vertexColor;
in vec3 relativePosition;
out vec4 fragColor;
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
    vec4 sampled = flowBounds.z != 0.0 ? textureGrad(Sampler0, uv, dx, dy) : texture(Sampler0, uv);
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
