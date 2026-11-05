package com.shnewbs.deepprimal.core.organic;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Read-only, dependency-free interpretation of Foundations Soil 0.1.0-beta
 * physical soil snapshots (SoilTexture, SoilProfile and SoilPhysics).
 *
 * <p>The host owns the authoritative soil cell, pore-water amount, calendar,
 * geology and nutrients. This bridge never creates soil, supplies missing
 * texture/hydration, moves water, or credits/debits N/P/K. A future native
 * adapter must obtain inputs from the accepted source on the server thread.</p>
 */
public final class FoundationsSoilPhysicsBridge {
  public static final int SCHEMA_VERSION = 1;

  /** Normalization matches Foundations Soil's SoilTexture record. */
  public record Texture(double sand, double silt, double clay) {
    public Texture {
      if (!Double.isFinite(sand) || !Double.isFinite(silt) || !Double.isFinite(clay)
          || sand < 0 || silt < 0 || clay < 0)
        throw new IllegalArgumentException("invalid soil texture fractions");
      double maximum = Math.max(sand, Math.max(silt, clay));
      if (maximum == 0) throw new IllegalArgumentException("empty soil texture");
      double sum = sand + silt + clay;
      if (!Double.isFinite(sum) || Math.abs(sum - 1.0) > 1.0e-12) {
        sand /= maximum;
        silt /= maximum;
        clay /= maximum;
        double total = sand + silt + clay;
        sand /= total;
        silt /= total;
        clay /= total;
      }
    }
  }

  /** The seven authoritative Foundations Soil statistics, not stored by this bridge. */
  public record Profile(double organicMatter, double pH, double drainage,
                        double compaction, double structure, double biology,
                        double salinity) {
    public Profile {
      unit("organicMatter", organicMatter);
      if (!Double.isFinite(pH) || pH < 3.5 || pH > 9.5)
        throw new IllegalArgumentException("invalid soil pH");
      unit("drainage", drainage);
      unit("compaction", compaction);
      unit("structure", structure);
      unit("biology", biology);
      unit("salinity", salinity);
    }
  }

  public enum Moisture { DRY, LIMITED, OPTIMAL, WET, WATERLOGGED }

  /** Derived, non-authoritative readings; all values except nutrientAccess are 0..1. */
  public record Conditions(double waterHolding, double fieldCapacity,
                           double aeration, double rootResistance,
                           double effectiveDrainage, double availableWater,
                           Moisture moisture, double nutrientAccess,
                           double responseModifier) {
    public Conditions {
      unit("waterHolding", waterHolding);
      unit("fieldCapacity", fieldCapacity);
      unit("aeration", aeration);
      unit("rootResistance", rootResistance);
      unit("effectiveDrainage", effectiveDrainage);
      unit("availableWater", availableWater);
      Objects.requireNonNull(moisture, "moisture");
      if (!Double.isFinite(nutrientAccess) || nutrientAccess < .35 || nutrientAccess > 1.08)
        throw new IllegalArgumentException("invalid nutrient access");
      unit("responseModifier", responseModifier);
    }
  }

  /** Missing legacy texture or missing authoritative hydration is not ideal loam. */
  public static Optional<Conditions> evaluate(Profile profile, Optional<Texture> texture,
                                               OptionalDouble hydration) {
    Objects.requireNonNull(profile, "profile");
    Objects.requireNonNull(texture, "texture");
    Objects.requireNonNull(hydration, "hydration");
    if (texture.isEmpty() || hydration.isEmpty()) return Optional.empty();
    return Optional.of(evaluate(profile, texture.get(), hydration.getAsDouble()));
  }

  /** Hydration is an admitted normalized reading from the water owner, never a deposit. */
  public static Conditions evaluate(Profile profile, Texture texture, double hydration) {
    Objects.requireNonNull(profile, "profile");
    Objects.requireNonNull(texture, "texture");
    unit("hydration", hydration);
    double holding = clamp(.15 + .50 * texture.silt() + .40 * texture.clay()
        + .20 * profile.organicMatter(), 0, 1);
    double capacity = clamp(.12 + .60 * holding + .12 * profile.structure(), 0, 1);
    double aeration = clamp(.25 + .60 * texture.sand() + .20 * profile.structure()
        - .35 * profile.compaction() - .20 * texture.clay(), 0, 1);
    double resistance = clamp(.60 * profile.compaction() + .25 * texture.clay()
        + .15 * (1 - profile.structure()), 0, 1);
    double drainage = clamp(.55 * profile.drainage() + .35 * texture.sand()
        + .10 * profile.structure() - .20 * texture.clay()
        - .25 * profile.compaction(), 0, 1);
    double available = clamp(hydration * (.60 + .60 * holding)
        - .12 * profile.salinity(), 0, 1);
    Moisture moisture = hydration >= .90 && drainage < .35 ? Moisture.WATERLOGGED
        : available < .18 ? Moisture.DRY : available < .35 ? Moisture.LIMITED
        : hydration >= .85 ? Moisture.WET : Moisture.OPTIMAL;
    double waterResponse = switch (moisture) {
      case DRY -> .65;
      case LIMITED -> .85;
      case WATERLOGGED -> .70;
      default -> 1.0;
    };
    double ph = 1.0 - Math.min(.45, Math.abs(profile.pH() - 6.5) * .09);
    double rootAccess = 1.0 - profile.compaction() * .35;
    double biological = .88 + profile.biology() * .20;
    double condition = .88 + profile.structure() * .12;
    double nutrientAccess = clamp(ph * rootAccess * biological * condition, .35, 1.08);
    return new Conditions(holding, capacity, aeration, resistance, drainage,
        available, moisture, nutrientAccess, waterResponse * (1.0 - .15 * resistance));
  }

  private static void unit(String name, double value) {
    if (!Double.isFinite(value) || value < 0 || value > 1)
      throw new IllegalArgumentException("invalid normalized " + name);
  }

  private static double clamp(double value, double min, double max) {
    return Math.max(min, Math.min(max, value));
  }

  private FoundationsSoilPhysicsBridge() {}
}
