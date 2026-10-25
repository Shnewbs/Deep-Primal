#version 460 core

layout(set = 0, binding = 0) uniform sampler2D uInputTexture;
layout(set = 0, binding = 1) uniform sampler2D uDepthTexture;
layout(set = 0, binding = 2) uniform sampler2D uMotionVectors;

layout(location = 0) in vec2 vTexCoord;
layout(location = 0) out vec4 outColor;

layout(push_constant) uniform UpscaleConstants {
    float rcpScale;      // 1.0 / scaleFactor
    float renderWidth;
    float renderHeight;
    int   mode;          // 0 = FSR, 1 = DLSS
} push;

// FSR 2.0 constants
#define FSR2_EASU_SHARPNESS 0.0
#define FSR2_RCAS_SHARPNESS 0.15

// Lanczos2 kernel for EASU
float lanczos2(float x) {
    if (x == 0.0) return 1.0;
    float pix = x * 3.14159265359;
    float pix2 = pix * 2.0;
    return (sin(pix) / pix - sin(pix2) / pix2) * 0.5;
}

vec4 easu(sampler2D tex, vec2 uv, vec2 rcpSize, float sharpness) {
    vec2 centerUv = uv * vec2(textureSize(tex, 0)) - 0.5;
    vec2 idx = floor(centerUv);
    vec2 frac = fract(centerUv);
    
    vec4 samples[4][4];
    float weights[4][4];
    float weightSum = 0.0;
    
    for (int y = -1; y <= 2; y++) {
        for (int x = -1; x <= 2; x++) {
            vec2 sampleUv = (idx + vec2(x, y) + 0.5) * rcpSize;
            samples[y+1][x+1] = texture(tex, sampleUv);
            float wx = lanczos2(x - frac.x + 0.5);
            float wy = lanczos2(y - frac.y + 0.5);
            weights[y+1][x+1] = wx * wy;
            weightSum += weights[y+1][x+1];
        }
    }
    
    vec4 result = vec4(0.0);
    for (int y = 0; y < 4; y++) {
        for (int x = 0; x < 4; x++) {
            result += samples[y][x] * (weights[y][x] / weightSum);
        }
    }
    
    // Sharpening (RCAS-like)
    vec2 rcp = rcpSize;
    vec4 n = texture(tex, uv + vec2(-rcp.x, 0.0));
    vec4 s = texture(tex, uv + vec2( rcp.x, 0.0));
    vec4 w = texture(tex, uv + vec2(0.0, -rcp.y));
    vec4 e = texture(tex, uv + vec2(0.0,  rcp.y));
    
    vec4 horz = (n + s) * 0.5;
    vec4 vert = (w + e) * 0.5;
    float horzDiff = length(n - s);
    float vertDiff = length(w - e);
    
    vec4 sharp = mix(horz, vert, step(vertDiff, horzDiff));
    result = mix(result, sharp, sharpness);
    
    return result;
}

void main() {
    vec2 rcpRenderSize = vec2(1.0 / push.renderWidth, 1.0 / push.renderHeight);
    
    if (push.mode == 0) { // FSR
        vec4 color = easu(uInputTexture, vTexCoord, rcpRenderSize, FSR2_RCAS_SHARPNESS);
        outColor = color;
    } else { // DLSS placeholder - would use NVIDIA SDK
        // For now, simple bilinear upscale
        outColor = texture(uInputTexture, vTexCoord);
    }
}