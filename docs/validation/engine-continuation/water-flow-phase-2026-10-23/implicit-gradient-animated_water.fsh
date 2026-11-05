#version 330
#moj_import <minecraft:fog.glsl>
uniform sampler2D Sampler0;
in vec2 texCoord;
flat in vec4 flowBounds;
in vec4 vertexColor;
in vec3 relativePosition;
out vec4 fragColor;
void main() {
    // Keep interpolated world phase unwrapped until sampling; never interpolate a tile seam.
    vec2 uv = flowBounds.z > 0.0
        ? flowBounds.xy + fract((texCoord - flowBounds.xy) / flowBounds.zw) * flowBounds.zw
        : texCoord;
    vec4 color = texture(Sampler0, uv) * vertexColor;
    if (color.a < 0.003) discard;
    fragColor = apply_fog(color, length(relativePosition), length(relativePosition.xz), FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
