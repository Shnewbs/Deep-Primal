#version 330
#moj_import <minecraft:fog.glsl>
uniform sampler2D NativeLight;
in vec4 vertexColor;
in vec3 relativePosition;
in vec3 surfaceNormal;
in vec2 tilePosition;
out vec4 fragColor;
void main() {
  vec3 normal=normalize(surfaceNormal);
  float shade=0.70+0.30*abs(dot(normal,normalize(vec3(-0.4,0.85,0.3))));
  vec3 light=texelFetch(NativeLight,ivec2(0,15),0).rgb;
  bool water=vertexColor.a<0.982;
  fragColor=apply_fog(vec4(vertexColor.rgb*shade*light,water?0.82:1.0),length(relativePosition),fog_cylindrical_distance(relativePosition),FogEnvironmentalStart,FogEnvironmentalEnd,FogRenderDistanceStart,FogRenderDistanceEnd,FogColor);
}
