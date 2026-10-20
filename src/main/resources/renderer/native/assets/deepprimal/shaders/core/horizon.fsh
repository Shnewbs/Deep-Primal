#version 330
#moj_import <minecraft:fog.glsl>
uniform sampler2D NativeLight;
in vec4 vertexColor;
in vec3 relativePosition;
in vec3 surfaceNormal;
in vec2 tilePosition;
out vec4 fragColor;
void main() {
    float horizontal = max(abs(relativePosition.x-DeepPrimalNearCenterX), abs(relativePosition.z-DeepPrimalNearCenterZ));
    bool transition = vertexColor.a > 0.982 && vertexColor.a < 0.995;
    vec3 plane = normalize(cross(dFdx(relativePosition), dFdy(relativePosition)));
    bool water = vertexColor.a < 0.982 && abs(plane.y) > 0.98;
    if ((!transition && horizontal < DeepPrimalNearDistance - (water ? 16.0 : 0.0)) || abs(relativePosition.y) > DeepPrimalVerticalDistance) discard;
    vec3 normal = normalize(surfaceNormal);
    float shade = 0.72 + 0.28 * abs(dot(normal, normalize(vec3(-0.4, 0.85, 0.3))));
    vec2 phase = tilePosition * (6.28318530718 / 512.0);
    float grain = sin(phase.x*131.0+phase.y*196.0)*sin(phase.x*60.0-phase.y*107.0);
    grain *= 0.025 / (1.0+length(fwidth(tilePosition)));
    vec3 color = vertexColor.rgb;
    if (vertexColor.a < 0.982 && !water) color = vec3(0.40,0.48,0.34);
    float alpha = water ? smoothstep(DeepPrimalNearDistance-16.0, DeepPrimalNearDistance+16.0, horizontal) : 1.0;
    if (alpha <= 0.0) discard;
    vec3 light = texelFetch(NativeLight, ivec2(0,15), 0).rgb;
    fragColor = apply_fog(vec4((color * shade + grain) * light, alpha), length(relativePosition), fog_cylindrical_distance(relativePosition),
        FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
