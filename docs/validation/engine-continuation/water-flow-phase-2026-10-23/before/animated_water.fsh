#version 330
#moj_import <minecraft:fog.glsl>
uniform sampler2D Sampler0;
in vec2 texCoord;
in vec4 vertexColor;
in vec3 relativePosition;
out vec4 fragColor;
void main() {
    vec4 color = texture(Sampler0, texCoord) * vertexColor;
    if (color.a < 0.003) discard;
    fragColor = apply_fog(color, length(relativePosition), length(relativePosition.xz), FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
