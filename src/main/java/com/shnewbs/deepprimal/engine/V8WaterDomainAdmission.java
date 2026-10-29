package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.biome.EcotoneBlend;
import com.shnewbs.deepprimal.core.density.DensityGraph;
import java.util.Objects;

/**
 * Fail-closed, explicit V8-r4 water-domain admission for new-chunk ecotone
 * materialization. The V7 reference alone is NOT an authoritative V8 water mask.
 * A caller must supply a second, geometry-bound V8 water-domain observation.
 * Both wet masks are protected; disagreement never paints a dry soil cap over
 * a possible ocean, river or lake. This class does not generate water, change
 * liquid volume, alter an existing saved profile or register native worldgen.
 *
 * <p>Until V8 has a separately persisted routed hydrology source, the mask
 * callback and its claimed revision require native validation. Requiring exact
 * saved seed, density definition and climate binding prevents accidental reuse
 * from another world, graph or changed creation defaults; it cannot establish
 * that an arbitrary caller's hydrology implementation is physically correct.
 */
public final class V8WaterDomainAdmission implements V8EcotoneColumnPlan.ClimateSource {
  /** Fixed gameplay sea surface from the frozen V7 reference policy. */
  public static final int SEA_PLANE = 96;
  /** This interface revision is a preflight contract, not a saved V8 water engine. */
  public static final int MASK_INTERFACE_REVISION = 1;

  public enum WaterKind { DRY, OCEAN, RIVER, LAKE }

  /** Physical water stage is observational only; it is never mixed into density. */
  public record WaterDatum(WaterKind kind, double physicalStage) {
    public WaterDatum {
      Objects.requireNonNull(kind, "water kind");
      if (kind == WaterKind.DRY ? !Double.isNaN(physicalStage)
          : !Double.isFinite(physicalStage))
        throw new IllegalArgumentException("water stage must be NaN for dry and finite for wet");
      if (kind == WaterKind.OCEAN && physicalStage != SEA_PLANE)
        throw new IllegalArgumentException("V8 ocean stage mismatches frozen sea plane");
    }

    public static WaterDatum dry() { return new WaterDatum(WaterKind.DRY, Double.NaN); }
    public static WaterDatum ocean() { return new WaterDatum(WaterKind.OCEAN, SEA_PLANE); }
  }

  @FunctionalInterface
  public interface WaterSource {
    WaterDatum sample(int absoluteWorldX, int absoluteWorldZ);
  }

  /** Explicit authority identity. The source must use the SAME saved V8 graph. */
  public record Authority(long worldSeed, DensityGraph.Definition savedDensity,
      V8ClimateBinding savedClimate, int maskInterfaceRevision, WaterSource source) {
    public Authority {
      Objects.requireNonNull(savedDensity, "saved density definition");
      Objects.requireNonNull(savedClimate, "saved climate binding");
      Objects.requireNonNull(source, "V8 hydrology observer");
      if (maskInterfaceRevision != MASK_INTERFACE_REVISION)
        throw new IllegalArgumentException("unsupported V8 hydrology observation revision");
    }
  }

  private final V8EcotoneColumnPlan.ClimateSource referenceClimate;
  private final WaterSource water;

  private V8WaterDomainAdmission(V8EcotoneColumnPlan.ClimateSource referenceClimate,
      WaterSource water) {
    this.referenceClimate = referenceClimate;
    this.water = water;
  }

  /**
   * Bind before sampling any native cells. The referenced r4 climate and graph
   * are frozen by the saved snapshot; r1-r3 remain entirely unaffected.
   */
  public static V8WaterDomainAdmission bind(V8GeneratorSnapshot.Prepared prepared,
      V8EcotoneColumnPlan.ClimateSource referenceClimate, Authority authority) {
    Objects.requireNonNull(prepared, "prepared saved V8 profile");
    Objects.requireNonNull(referenceClimate, "reference climate");
    Objects.requireNonNull(authority, "hydrology authority");
    V8GeneratorSnapshot saved = prepared.snapshot();
    if (saved.version() != V8GeneratorSnapshot.CLIMATE_VERSION
        || !V8SurfacePolicy.FROZEN_R1.equals(saved.surfacePolicy()))
      throw new IllegalArgumentException("water-domain admission requires frozen V8 revision 4");
    if (authority.worldSeed() != saved.seed()
        || !authority.savedDensity().equals(saved.density())
        || !authority.savedClimate().equals(saved.climateBinding()))
      throw new IllegalArgumentException("V8 hydrology authority does not match saved world/graph/climate");
    return new V8WaterDomainAdmission(referenceClimate, authority.source());
  }

  @Override
  public V8EcotoneColumnPlan.ClimateDatum sample(int worldX, int topY, int worldZ) {
    var climate = Objects.requireNonNull(referenceClimate.sample(worldX, topY, worldZ),
        "saved reference climate observation");
    var datum = Objects.requireNonNull(water.sample(worldX, worldZ),
        "V8 hydrology observation");
    // Two independently versioned masks may disagree at the old/new coastline
    // and routed lake outlets. Until a saved V8 hydrology policy is proven,
    // conservatively keep BOTH domains free of ecotone surface paints.
    EcotoneBlend.Domain domain;
    if (climate.domain() == EcotoneBlend.Domain.OCEAN || datum.kind() == WaterKind.OCEAN)
      domain = EcotoneBlend.Domain.OCEAN;
    else if (climate.domain() == EcotoneBlend.Domain.RIVER
        || datum.kind() == WaterKind.RIVER || datum.kind() == WaterKind.LAKE)
      domain = EcotoneBlend.Domain.RIVER;
    else if (topY < SEA_PLANE)
      // A dry declaration below the sea plane may be a V8 coastline mismatch.
      // Skipping material is safer than assuming this is a stable dry lowland.
      domain = EcotoneBlend.Domain.OCEAN;
    else domain = EcotoneBlend.Domain.LAND;
    return new V8EcotoneColumnPlan.ClimateDatum(climate.climate(), domain);
  }
}
