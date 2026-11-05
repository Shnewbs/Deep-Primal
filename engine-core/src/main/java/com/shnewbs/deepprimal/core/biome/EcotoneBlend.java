package com.shnewbs.deepprimal.core.biome;

import java.util.Objects;

/**
 * Explicit-call-only climate ecotones using Landscape's serialized 40-biome palette.
 * This does not change the legacy classifier, register a generator or modify water/terrain.
 * A native consumer must persist its blend policy in a new versioned world profile first.
 *
 * <p>Each categorical decision splits its remaining weight with a quintic transition.
 * The finite decision tree has no neighborhood queries, cache, RNG or chunk-local state.
 * Continuous inputs produce continuous weights within a fixed hydrological domain when
 * all widths are positive. Ocean/river/land ownership is supplied, never interpolated.
 */
public final class EcotoneBlend {
  public static final int PALETTE_SIZE = 40;
  public static final int POLICY_VERSION = 1;

  private EcotoneBlend() {}

  /** Authoritative hydrology chooses the domain; blends cannot invent or move water. */
  public enum Domain { LAND, RIVER, OCEAN }

  /** Continuous fields sampled at the same absolute world position by the caller. */
  public record Climate(double height, double temperature, double rainfall,
                        double slope, double patch) {
    public Climate {
      finite(height, "height");
      interval(temperature, 0, 1, "temperature");
      interval(rainfall, 0, 1, "rainfall");
      finite(slope, "slope");
      if (slope < 0) throw new IllegalArgumentException("negative slope");
      finite(patch, "patch");
    }
  }

  /**
   * Half-widths around each original threshold, in the corresponding field's units.
   * Zero retains the original strict comparison on that axis. These are not block radii.
   * PREVIEW is an authoring proposal, not an enabled/default or calibrated world setting.
   */
  public record Widths(double temperature, double rainfall, double height,
                       double slope, double patch) {
    public static final Widths HARD = new Widths(0, 0, 0, 0, 0);
    public static final Widths PREVIEW = new Widths(.025, .04, 16, .15, .08);

    public Widths {
      interval(temperature, 0, .5, "temperature width");
      interval(rainfall, 0, .5, "rainfall width");
      interval(height, 0, 256, "height width");
      interval(slope, 0, 4, "slope width");
      interval(patch, 0, 2, "patch width");
    }
  }

  /** Immutable normalized weights; do not collapse to dominantBiome for smooth consumers. */
  public static final class Weights {
    private final double[] values;

    private Weights(double[] owned) {
      double sum = 0;
      for (double value : owned) sum += value;
      if (!(sum > 0) || !Double.isFinite(sum)) throw new IllegalStateException("empty blend");
      for (int i = 0; i < owned.length; i++) owned[i] /= sum;
      values = owned;
    }

    public double weight(int biome) {
      if (biome < 0 || biome >= PALETTE_SIZE) throw new IndexOutOfBoundsException(biome);
      return values[biome];
    }

    public double[] toArray() { return values.clone(); }

    /** Diagnostic only: largest weight, resolving exact ties to the smallest palette id. */
    public int dominantBiome() {
      int best = 0;
      for (int i = 1; i < PALETTE_SIZE; i++) if (values[i] > values[best]) best = i;
      return best;
    }

    /**
     * Discrete palette choice from caller-owned deterministic noise in [0,1).
     * Use the full weights for continuous material/vegetation parameters, not this choice.
     * The caller controls spatial correlation; this method does not promise smooth blocks.
     */
    public int choose(double unit) {
      if (!Double.isFinite(unit) || unit < 0 || unit >= 1)
        throw new IllegalArgumentException("choice must be in [0,1)");
      double cumulative = 0;
      int last = -1;
      for (int i = 0; i < PALETTE_SIZE; i++) {
        if (values[i] == 0) continue;
        last = i;
        cumulative += values[i];
        if (unit < cumulative) return i;
      }
      return last; // Only a final rounding remainder can reach here.
    }

    /** Blend unit-range properties, e.g. vegetation cover, without averaging surface heights. */
    public double blendUnitValues(double[] palette) {
      return blendBoundedValues(palette, 0, 1, "palette value");
    }

    /**
     * Blend a finite continuous material property against the saved biome weights.
     * This is for bounded cover/snow/soil coefficients only; callers must not use it
     * for physical heights, liquid volume or hydrology ownership.
     */
    public double blendBoundedValues(double[] palette, double min, double max, String name) {
      Objects.requireNonNull(palette, "palette");
      Objects.requireNonNull(name, "name");
      if (palette.length != PALETTE_SIZE) throw new IllegalArgumentException("palette length");
      if (!Double.isFinite(min) || !Double.isFinite(max) || min > max)
        throw new IllegalArgumentException("invalid bounds");
      double result = 0;
      for (int i = 0; i < PALETTE_SIZE; i++) {
        interval(palette[i], min, max, name);
        result += values[i] * palette[i];
      }
      return Math.max(min, Math.min(max, result));
    }
  }

  public static Weights sample(Climate c, Domain domain, Widths w) {
    Objects.requireNonNull(c, "climate");
    Objects.requireNonNull(domain, "domain");
    Objects.requireNonNull(w, "widths");
    Accumulator a = new Accumulator();
    double h = c.height(), t = c.temperature(), r = c.rainfall();
    double s = c.slope(), p = c.patch();
    double tw = w.temperature(), rw = w.rainfall(), hw = w.height();
    double sw = w.slope(), pw = w.patch();

    if (domain == Domain.OCEAN) {
      double deep = below(h, 45, hw);
      double cold = below(t, .18, tw), cool = below(t, .42, tw);
      a.add(35, deep * cold);
      a.add(34, deep * (1 - cold) * cool);
      a.add(33, deep * (1 - cold) * (1 - cool));
      double shallow = 1 - deep;
      a.add(32, shallow * cold);
      a.add(31, shallow * (1 - cold) * cool);
      double warm = shallow * (1 - cold) * (1 - cool);
      double hot = above(t, .8, tw), mild = above(t, .64, tw);
      a.add(29, warm * hot);
      a.add(30, warm * (1 - hot) * mild);
      a.add(3, warm * (1 - hot) * (1 - mild));
      return new Weights(a.values);
    }
    if (domain == Domain.RIVER) {
      a.split(1, below(t, .22, tw), 37, 36);
      return new Weights(a.values);
    }

    double weight = a.take(below(h, 100, hw));
    double cold = below(t, .25, tw);
    a.add(26, weight * cold);
    a.split(weight * (1 - cold), above(s, 1.4, sw), 27, 25);

    weight = a.take(above(h, 260, hw));
    cold = below(t, .26, tw);
    a.add(19, weight * cold);
    a.split(weight * (1 - cold), above(s, 1.2, sw), 20, 7);

    weight = a.take(above(h, 210, hw));
    cold = below(t, .3, tw);
    double cool = below(t, .45, tw);
    a.add(18, weight * cold);
    a.add(17, weight * (1 - cold) * cool);
    a.split(weight * (1 - cold) * (1 - cool), above(s, .9, sw), 23, 21);

    a.split(a.take(below(t, .2, tw)), above(r, .50, rw), 16, 5);
    a.split(a.take(below(t, .39, tw)), above(r, .66, rw), 15, 4);
    a.split(a.take(below(h, 105, hw) * above(r, .74, rw)), above(t, .72, tw), 28, 6);
    a.split(a.take(below(r, .32, rw)), above(p, .18, pw), 9, 2);
    a.split(a.take(below(r, .46, rw)), above(t, .62, tw), 8, 0);
    a.split(a.take(above(t, .73, tw) * above(r, .70, rw)), above(p, .25, pw), 11, 10);
    a.add(12, a.take(above(t, .71, tw) * above(r, .56, rw)));
    a.add(13, a.take(above(r, .77, rw)));
    a.add(22, a.take(above(h, 165, hw) * above(p, .30, pw)));
    a.add(24, a.take(above(s, 1.2, sw)));
    a.split(a.take(above(r, .57, rw)), below(p, -.12, pw), 14, 1);
    a.split(a.remaining, above(h, 170, hw), 21, 0);
    return new Weights(a.values);
  }

  private static final class Accumulator {
    private final double[] values = new double[PALETTE_SIZE];
    private double remaining = 1;
    void add(int biome, double weight) { values[biome] += weight; }
    void split(double weight, double yes, int yesBiome, int noBiome) {
      add(yesBiome, weight * yes);
      add(noBiome, weight * (1 - yes));
    }
    double take(double gate) {
      double weight = remaining * gate;
      remaining *= 1 - gate;
      return weight;
    }
  }

  private static double above(double value, double threshold, double width) {
    return width == 0 ? (value > threshold ? 1 : 0) : transition(value, threshold, width);
  }

  private static double below(double value, double threshold, double width) {
    return width == 0 ? (value < threshold ? 1 : 0) : 1 - transition(value, threshold, width);
  }

  private static double transition(double value, double center, double width) {
    if (value == center) return .5; // Also well-defined when a tiny width rounds to the center.
    double low = center - width, high = center + width;
    if (value <= low) return 0;
    if (value >= high) return 1;
    double x = (value - low) / (high - low);
    return Math.max(0, Math.min(1, x * x * x * (x * (x * 6 - 15) + 10)));
  }

  private static void finite(double value, String name) {
    if (!Double.isFinite(value)) throw new IllegalArgumentException(name + " must be finite");
  }

  private static void interval(double value, double min, double max, String name) {
    finite(value, name);
    if (value < min || value > max) throw new IllegalArgumentException(name + " out of range");
  }
}
