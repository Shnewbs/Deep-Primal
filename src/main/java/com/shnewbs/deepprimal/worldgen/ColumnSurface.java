package com.shnewbs.deepprimal.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.shnewbs.deepprimal.core.ColumnHeight;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

/** A seed/position-dependent height is evaluated once instead of scanning an entire tall column. */
public record ColumnSurface(DensityFunction height, int lower, int upper, int step)
        implements DensityFunction.SimpleFunction {
    public static final MapCodec<ColumnSurface> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        DensityFunction.HOLDER_HELPER_CODEC.fieldOf("height").forGetter(ColumnSurface::height),
        Codec.INT.fieldOf("lower_bound").forGetter(ColumnSurface::lower),
        Codec.INT.fieldOf("upper_bound").forGetter(ColumnSurface::upper),
        Codec.intRange(1, 64).fieldOf("cell_height").forGetter(ColumnSurface::step)
    ).apply(instance, ColumnSurface::new));
    private static final KeyDispatchDataCodec<ColumnSurface> CODEC = KeyDispatchDataCodec.of(MAP_CODEC);

    public ColumnSurface {
        if (upper < lower || step < 1) throw new IllegalArgumentException("Invalid surface range");
    }

    @Override public double compute(FunctionContext context) {
        return ColumnHeight.surfaceY(height.compute(context), lower, upper, step);
    }
    @Override public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new ColumnSurface(height.mapAll(visitor), lower, upper, step));
    }
    @Override public double minValue() { return lower; }
    @Override public double maxValue() { return upper; }
    @Override public KeyDispatchDataCodec<? extends DensityFunction> codec() { return CODEC; }
}
