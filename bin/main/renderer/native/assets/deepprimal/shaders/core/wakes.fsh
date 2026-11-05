#version 330
#moj_import <minecraft:fog.glsl>
uniform sampler2D NativeLight;
in vec4 vertexColor;
in vec3 relativePosition;
out vec4 fragColor;
void main() {
    vec3 light=texelFetch(NativeLight,ivec2(0,15),0).rgb;
    fragColor=apply_fog(vec4(vertexColor.rgb*light,vertexColor.a),length(relativePosition),fog_cylindrical_distance(relativePosition),
        FogEnvironmentalStart,FogEnvironmentalEnd,FogRenderDistanceStart,FogRenderDistanceEnd,FogColor);
}
