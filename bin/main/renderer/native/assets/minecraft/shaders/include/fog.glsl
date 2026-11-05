#version 330

// Deep Primal's authored implementation of the native fog include interface.
// Environmental fog keeps its spherical distance; render fog has separate axes.
layout(std140) uniform Fog {
    vec4 FogColor;
    float FogEnvironmentalStart;
    float FogEnvironmentalEnd;
    float FogRenderDistanceStart;
    float FogRenderDistanceEnd;
    float FogSkyEnd;
    float FogCloudsEnd;
    float DeepPrimalVerticalScale;
    float DeepPrimalNearDistance;
    float DeepPrimalVerticalDistance;
    float DeepPrimalNearCenterX;
    float DeepPrimalNearCenterZ;
};

float linear_fog_value(float distance, float start, float end) {
    if (distance <= start) return 0.0;
    if (distance >= end) return 1.0;
    return clamp((distance - start) / (end - start), 0.0, 1.0);
}

float total_fog_value(float sphere, float cylinder, float envStart, float envEnd, float viewStart, float viewEnd) {
    float environment = linear_fog_value(sphere, envStart, envEnd);
    float viewLimit = linear_fog_value(cylinder, viewStart, viewEnd);
    return max(environment, viewLimit);
}

vec4 apply_fog(vec4 color, float sphere, float cylinder, float envStart, float envEnd, float viewStart, float viewEnd, vec4 fogColor) {
    float opacity = total_fog_value(sphere, cylinder, envStart, envEnd, viewStart, viewEnd) * fogColor.a;
    return vec4(mix(color.rgb, fogColor.rgb, opacity), color.a);
}

float fog_spherical_distance(vec3 position) { return length(position); }

float fog_cylindrical_distance(vec3 position) {
    float scale = DeepPrimalVerticalScale > 0.0 ? DeepPrimalVerticalScale : 1.0;
    return max(length(position.xz), abs(position.y) * scale);
}
