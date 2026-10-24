#version 330
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;
uniform sampler2D Sampler2;
out vec2 texCoord;
flat out vec4 flowBounds;
flat out vec4 previousBounds;
out vec2 previousTexCoord;
flat out float previousWeight;
flat out ivec4 waterCorners;
out vec2 cornerSourceTexCoord;
out vec4 vertexColor;
out vec3 relativePosition;
#moj_import <deepprimal_graphics:water_flow.glsl>
vec2 previousFlow(int direction, vec4 tile) {
    ivec2 c = FLOW_Q25[direction];
    int cellBits = int(round(Color.a * 255.0));
    ivec2 cell = ivec2(cellBits & 15, cellBits >> 4);
    uvec2 world = uvec2(ivec2(TextureMat[3][0], TextureMat[3][1]) + cell);
    uvec2 phase = uvec2(uint(c.y) * world.x - uint(c.x) * world.y,
                       uint(c.x) * world.x + uint(c.y) * world.y) & uvec2(67108863u);
    vec2 f = vec2(c) / 33554432.0, local = Position.xz - vec2(cell) - vec2(0.5);
    vec2 uv = vec2(0.5) + 0.5 * vec2(f.y * local.x - f.x * local.y, f.x * local.x + f.y * local.y)
              + vec2(phase) / 67108864.0;
    return tile.xy + uv * tile.zw;
}
void main() {
    // TextureMat carries elapsed milliseconds since this mesh was uploaded.
    // Negative target normal is a packet marker; ordinary repeating UVs remain legal.
    bool packet = Normal.y < 0.0;
    int packetNormal = int(round(Normal.y * 127.0)) & 127;
    if (packet && UV0.x < 0.0) packetNormal = 0; // -128/-127 SNORM escape
    bool spatial = packet || UV0.x < -1.0;
    int heightOffset = spatial ? (UV1.y & 255) : UV1.y;
    if (spatial && heightOffset > 127) heightOffset -= 256;
    float t = clamp((TextureMat[0][0] - float(heightOffset)) / 100.0, 0.0, 1.0);
    vec3 p = Position;
    float displacement = float(UV1.x) / 16384.0 * (1.0 - t);
    p.y += displacement;
    relativePosition = p + ModelOffset;
    gl_Position = ProjMat * ModelViewMat * vec4(relativePosition, 1.0);
    // Active material blends reuse the otherwise constant color alpha for cell coordinates.
    vec4 tint = Color;
    if (spatial || (UV2.y & 32768) != 0) tint.a = 1.0;
    vertexColor = tint * ColorModulator * texelFetch(Sampler2, clamp(ivec2(UV2.x & 255, UV2.y & 255) / 16, ivec2(0), ivec2(15)), 0);
    // Signed normalized Normal.z stores the original opacity start offset in milliseconds.
    float alphaT = clamp((TextureMat[0][0] - Normal.z * 127.0) / 100.0, 0.0, 1.0);
    vertexColor.a *= mix(Normal.x, packet ? (packetNormal>=64?1.0:0.0) : Normal.y, alphaT);
    texCoord = UV0;
    vec4 flowTile = vec4(TextureMat[0][1], TextureMat[0][3], TextureMat[0][2], TextureMat[1][2]);
    vec4 stillTile = vec4(TextureMat[1][3], TextureMat[2][0], TextureMat[2][2], TextureMat[1][0]);
    waterCorners = ivec4(0);
    cornerSourceTexCoord = vec2(0.0);
    if (spatial) {
        int to,from,ages;
        if(packet){
            uint x=floatBitsToUint(UV0.x),y=floatBitsToUint(UV0.y);
            uint a=(x&8388607u)|((x>>1)&528482304u);
            uint b=(y&8388607u)|((y>>1)&528482304u)|((y>>2)&536870912u);
            uint w0=a|((b&7u)<<29),w1=(b>>3)|((uint(UV1.y>>8)&31u)<<27);
            uint w2=(uint(UV1.y>>13)&7u)|((uint(UV2.x>>8)&255u)<<3)|((uint(UV2.y>>8)&255u)<<11)
                |((uint(UV2.x)&15u)<<19)|((uint(UV2.y)&15u)<<23)|((uint(packetNormal)&31u)<<27);
            uint w3=(uint(packetNormal)>>5)&1u;
            uint rem=w0%289u+(w1&1u)*154u;
            uint rest=w0/289u+(w1&1u)*14861478u+rem/289u;
            uvec4 pairs=uvec4(rem%289u,rest%289u,(rest/289u)%289u,rest/83521u);
            uvec4 starts=uvec4((w1>>1)&65535u,(w1>>17)|((w2&1u)<<15),(w2>>1)&65535u,(w2>>17)|(w3<<15));
            uint now=uint(int(TextureMat[2][3]+TextureMat[0][0]))&65535u;
            uvec4 age=min(uvec4(100u),(uvec4(now)-starts)&uvec4(65535u));
            to=0;from=0;ages=0;
            for(int n=0;n<4;n++){to|=int(pairs[n]/17u)<<(n*5);from|=int(pairs[n]%17u)<<(n*5);ages|=int(age[n])<<(n*8);}
        }else{
            to=int(round(-UV0.x))-2;from=int(round(UV0.y));
            int fourthAge=((to>>20)&15)|(((from>>20)&7)<<4);
            ages=((UV1.y>>8)&255)|(((UV2.x>>8)&255)<<8)|(((UV2.y>>8)&255)<<16)|(fourthAge<<24);
        }
        int cellBits = int(round(Color.a * 255.0));
        waterCorners = ivec4(to & 1048575, from & 1048575, ages, cellBits | (packet ? 256 : 0));
        flowBounds = flowTile; previousBounds = stillTile;
        texCoord = Position.xz - vec2(cellBits & 15, cellBits >> 4);
        int uniformDirection = to & 31;
        previousTexCoord = uniformDirection == 16 ? stillTile.xy + texCoord * stillTile.zw
            : previousFlow(uniformDirection, flowTile);
        int sourceDirection = from & 31;
        cornerSourceTexCoord = sourceDirection == 16 ? stillTile.xy + texCoord * stillTile.zw
            : previousFlow(sourceDirection, flowTile);
        previousWeight = -1.0;
        return;
    }
    flowBounds = (UV2.x & 1024) != 0 ? flowTile : vec4(0.0);
    previousBounds = vec4(0.0);
    previousWeight = (UV2.y & 32768) != 0
        ? clamp(1.0 - (TextureMat[0][0] + float((UV2.y >> 8) & 127)) / 100.0, 0.0, 1.0) : 0.0;
    previousTexCoord = UV0;
    if (previousWeight > 0.0) {
        int sourceDirection = clamp((UV2.x >> 11) & 31, 0, 16);
        if ((UV2.x & 256) != 0) {
            bool currentFlow = (UV2.x & 512) != 0;
            bool oldFlow = sourceDirection < 16;
            vec4 currentTile = currentFlow ? flowTile : stillTile;
            vec4 oldTile = oldFlow ? flowTile : stillTile;
            float currentScale = currentFlow ? 0.5 : 1.0;
            float oldScale = oldFlow ? 0.5 : 1.0;
            // Convert admitted UVs to cell-local coordinates, then apply the same
            // displayed height displacement to both material endpoints.
            vec2 localUV = (UV0 - currentTile.xy) / (currentTile.zw * currentScale);
            localUV.y -= displacement;
            previousTexCoord = oldTile.xy + localUV * oldTile.zw * oldScale;
            // Negative width marks nonperiodic side bounds for fragment-space clamping.
            previousBounds = vec4(oldTile.xy, -oldTile.z, oldTile.w * oldScale);
        } else if (sourceDirection < 16) {
            previousBounds = flowTile;
            previousTexCoord = previousFlow(sourceDirection, flowTile);
        } else {
            int cellBits = int(round(Color.a * 255.0));
            vec2 cell = vec2(cellBits & 15, cellBits >> 4);
            previousTexCoord = stillTile.xy + (Position.xz - cell) * stillTile.zw;
        }
    }

    if ((UV2.x & 256) != 0) {
        bool flowing = (UV2.x & 512) != 0;
        float span = flowing ? TextureMat[1][1] : TextureMat[1][0];
        texCoord.y = UV0.y - displacement * span;
        vec4 tile = flowing ? flowTile : stillTile;
        flowBounds = vec4(tile.xy, -tile.z, span);
    }
}
