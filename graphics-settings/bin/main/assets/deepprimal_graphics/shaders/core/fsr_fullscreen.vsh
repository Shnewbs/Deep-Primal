#version 330

// A triangle covering the attachment, without a vertex/index allocation.
// Both passes address image pixels directly with gl_FragCoord, so no texture
// coordinate flip or half-texel interpolation is introduced between backends.
void main() {
    vec2 corner = vec2((gl_VertexID << 1) & 2, gl_VertexID & 2);
    gl_Position = vec4(corner * 2.0 - 1.0, 0.0, 1.0);
}
