#version 330
#moj_import <minecraft:fog.glsl>
in vec4 vertexColor;
in vec3 relativePosition;
out vec4 fragColor;
void main() {
    float horizontal = max(abs(relativePosition.x-DeepPrimalNearCenterX), abs(relativePosition.z-DeepPrimalNearCenterZ));
    // Native full-detail terrain is depth-tested over the macro surface.
    // The near overlap prevents the horizon mesh exposing through nearby caves.
    if ((vertexColor.a > 0.99 && horizontal < DeepPrimalNearDistance) || abs(relativePosition.y) > DeepPrimalVerticalDistance) discard;
    vec3 normal = normalize(cross(dFdx(relativePosition), dFdy(relativePosition)));
    float shade = 0.72 + 0.28 * abs(dot(normal, normalize(vec3(-0.4, 0.85, 0.3))));
    fragColor = apply_fog(vec4(vertexColor.rgb * shade, 1.0), length(relativePosition), fog_cylindrical_distance(relativePosition),
        FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
