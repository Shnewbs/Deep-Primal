#version 330
#moj_import <minecraft:fog.glsl>
uniform sampler2D NativeLight;
in vec4 vertexColor;
in vec3 relativePosition;
in float crest;
out vec4 fragColor;
void main() {
    float alpha=vertexColor.a*smoothstep(.01,.08,crest);
    if(alpha<.005)discard;
    vec3 normal=normalize(cross(dFdx(relativePosition),dFdy(relativePosition)));
    float shine=pow(abs(dot(normal,normalize(vec3(.25,1.0,.4)))),12.0)*.10;
    vec3 light=texelFetch(NativeLight,ivec2(0,15),0).rgb;
    vec3 color=(vertexColor.rgb+shine)*light;
    fragColor=apply_fog(vec4(color,alpha),length(relativePosition),fog_cylindrical_distance(relativePosition),
        FogEnvironmentalStart,FogEnvironmentalEnd,FogRenderDistanceStart,FogRenderDistanceEnd,FogColor);
}
