#version 330
uniform sampler2D Source;
layout(std140) uniform UpscaleSize {
    vec4 DestinationSize;
};
out vec4 fragColor;
void main() {
    fragColor = vec4(texture(Source, gl_FragCoord.xy / DestinationSize.xy).rgb, 1.0);
}
