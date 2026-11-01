#version 330
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
in vec3 Position;
in vec4 Color;
out vec4 vertexColor;
out vec3 relativePosition;
void main() {
    relativePosition=Position+ModelOffset;
    gl_Position=ProjMat*ModelViewMat*vec4(relativePosition,1.0);
    vertexColor=Color;
}
