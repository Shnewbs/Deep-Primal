#version 330
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
in vec3 Position;
in vec4 Color;
out vec4 vertexColor;
out vec3 relativePosition;
out float crest;
void main() {
    float p=TextureMat[0][0];
    float storm=TextureMat[0][1];
    float x=Position.x+TextureMat[1][0];
    float z=Position.z+TextureMat[1][1];
    float wave=(sin(x*.095+z*.031-p)+.45*sin(z*.17-x*.04-p*1.41)+.2*sin(x*.29+z*.12-p*1.9)
        +.12*sin(x*.37-z*.23-p*2.3)+.08*sin(x*.51+z*.41-p*2.7)
        +.05*sin(z*.67-x*.32-p*3.1)+.03*sin(x*.83+z*.53-p*3.6)
        +.02*sin(z*1.07-x*.73-p*4.1))*(1.65/1.95)*(.10+storm*.15);
    crest=max(0.0,wave);
    relativePosition=Position+ModelOffset+vec3(0.0,crest,0.0);
    gl_Position=ProjMat*ModelViewMat*vec4(relativePosition,1.0);
    vertexColor=Color;
}

