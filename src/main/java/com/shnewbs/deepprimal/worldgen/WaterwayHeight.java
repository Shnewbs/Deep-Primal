package com.shnewbs.deepprimal.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.shnewbs.deepprimal.core.WaterwayShape;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

/** Seeded platform adapter. The enclosing flat_cache retains the native 5x5 column grid. */
public record WaterwayHeight(DensityFunction terrain, DensityFunction continents,
                             DensityFunction ridges, DensityFunction erosion,
                             DensityFunction detail, DensityFunction tributaries, int seaLevel)
        implements DensityFunction.SimpleFunction {
    public static final MapCodec<WaterwayHeight> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        DensityFunction.HOLDER_HELPER_CODEC.fieldOf("terrain_height").forGetter(WaterwayHeight::terrain),
        DensityFunction.HOLDER_HELPER_CODEC.fieldOf("continents").forGetter(WaterwayHeight::continents),
        DensityFunction.HOLDER_HELPER_CODEC.fieldOf("ridges").forGetter(WaterwayHeight::ridges),
        DensityFunction.HOLDER_HELPER_CODEC.fieldOf("erosion").forGetter(WaterwayHeight::erosion),
        DensityFunction.HOLDER_HELPER_CODEC.fieldOf("detail").forGetter(WaterwayHeight::detail),
        DensityFunction.HOLDER_HELPER_CODEC.fieldOf("tributaries").forGetter(WaterwayHeight::tributaries),
        Codec.intRange(-4096, 4096).fieldOf("sea_level").forGetter(WaterwayHeight::seaLevel)
    ).apply(instance, WaterwayHeight::new));
    private static final KeyDispatchDataCodec<WaterwayHeight> CODEC = KeyDispatchDataCodec.of(MAP_CODEC);

    @Override public double compute(FunctionContext context) {
        return WaterwayShape.height(terrain.compute(context), continents.compute(context),
            ridges.compute(context), erosion.compute(context), detail.compute(context),
            tributaries.compute(context), seaLevel);
    }

    @Override public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new WaterwayHeight(terrain.mapAll(visitor), continents.mapAll(visitor),
            ridges.mapAll(visitor), erosion.mapAll(visitor), detail.mapAll(visitor),
            tributaries.mapAll(visitor), seaLevel));
    }

    @Override public double minValue() { return Math.min(terrain.minValue(), seaLevel - 14.0); }
    @Override public double maxValue() { return Math.max(terrain.maxValue(), seaLevel + 23.0); }
    @Override public KeyDispatchDataCodec<? extends DensityFunction> codec() { return CODEC; }
}
