#version 330
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
in vec3 Position;
in vec4 Color;
in vec3 Normal;
out vec4 vertexColor;
out vec3 relativePosition;
out vec3 surfaceNormal;
out vec2 tilePosition;
void main() {
    relativePosition = Position + ModelOffset;
    gl_Position = ProjMat * ModelViewMat * vec4(relativePosition, 1.0);
    vertexColor = Color * ColorModulator;
    surfaceNormal = Normal;
    tilePosition = Position.xz;
}
