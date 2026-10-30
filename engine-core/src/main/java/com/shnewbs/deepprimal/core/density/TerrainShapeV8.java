package com.shnewbs.deepprimal.core.density;

import java.util.*;
import static com.shnewbs.deepprimal.core.density.DensityGraph.*;

/** Experimental V8 shape recipe. Not a registered generator and not a routed hydrology stage. */
public final class TerrainShapeV8 {
  private TerrainShapeV8() {}

  /** Named channels expose continentalness, erosion, peaks_valleys, canyon and height. */
  public static Definition definition(boolean floatingFeatures) {
    return definition(floatingFeatures, 1);
  }

  /**
   * Explicit authoring-only V8 relief recipe revision 2. Its complete density
   * graph is saved in the V8 snapshot: reopening never substitutes the latest
   * recipe for the saved nodes. The historical {@link #definition(boolean)}
   * node order and exact values remain unchanged.
   *
   * <p>All transitions are smooth density inputs BEFORE hydrology, never a
   * post-generation chunk blur. Canyon incision remains the final relief cut.
   * This is not a registered native generator or a saved-world migration.
   */
  public static Definition definitionReliefV2(boolean floatingFeatures) {
    return definition(floatingFeatures, 2);
  }

  /**
   * Explicit revision 3 fixes the unqualified nearshore sea-plane movement in
   * relief V2 without rewriting any already saved V8 density graph. The V1
   * coastal height is kept bit-identical while continentalness is <= .08;
   * farther inland the new relief joins in smoothly. This is not an ocean
   * mask or hydrology migration, and must not be native-activated without
   * validating both independently.
   */
  public static Definition definitionReliefV3(boolean floatingFeatures) {
    return definition(floatingFeatures, 3);
  }

  /**
   * Explicit V4 authoring graph uses a conservative sea-side relief blend: the
   * previous V1 height determines the sea-plane side, while V2 provides the
   * inland relief proposal. Near historical shorelines, the signed height
   * distance is continuously damped, avoiding the two V3 shoulder witnesses.
   * Away from the sea plane, safe proposals regain full V2 relief exactly.
   * The guard is applied BEFORE terrain density and routed hydrology. It is
   * not an authoritative ocean/river mask and is not native-activated.
   */
  public static Definition definitionReliefV4(boolean floatingFeatures) {
    return definition(floatingFeatures, 4);
  }

  /**
   * Explicit new-world V8 relief recipe V5. A frozen two-scale foothill/
   * plateau overlap adds small continuous geometry variation before river
   * incision and the final density field. Earlier graphs are unchanged.
   * It remains unregistered; this is not a native worldgen migration.
   */
  public static Definition definitionReliefV5(boolean floatingFeatures) {
    return definition(floatingFeatures, 5);
  }

  /** Explicit new-world V8 relief recipe V6 with a bounded plateau/coast join. */
  public static Definition definitionReliefV6(boolean floatingFeatures) {
    return definition(floatingFeatures, 6);
  }

  private static Definition definition(boolean floatingFeatures, int reliefRevision) {
    if (reliefRevision < 1 || reliefRevision > 6)
      throw new IllegalArgumentException("unsupported V8 relief revision");
    Builder b=new Builder();
    b.c("zero",0);b.c("one",1);b.c("negative_one",-1);b.c("sea",96);b.c("continental_scale",320);
    b.n("warp_x",1800,1800,21001,true);b.n("warp_z",1800,1800,21002,true);
    b.n("continental_base",4200,4200,21003,true);
    b.nodes.add(new Node("continentalness",Op.WARP,List.of("continental_base","warp_x","zero","warp_z"),List.of(220.),null));
    b.n("erosion",1100,1100,21004,true);
    b.n("ridge_noise",680,680,21005,true);
    b.op("ridge_abs",Op.ABS,"ridge_noise");b.op("ridge_negative",Op.MULTIPLY,"ridge_abs","negative_one");
    b.op("peaks_valleys",Op.ADD,"one","ridge_negative");
    b.op("ridge_squared",Op.MULTIPLY,"peaks_valleys","peaks_valleys");
    b.bound("inland",Op.SMOOTHSTEP,"continentalness",-.02,.16);
    b.bound("eroded",Op.SMOOTHSTEP,"erosion",-.15,.22);
    b.op("eroded_negative",Op.MULTIPLY,"eroded","negative_one");b.op("mountain_mask",Op.ADD,"one","eroded_negative");
    b.c("mountain_scale",175);b.op("mountains_a",Op.MULTIPLY,"ridge_squared","mountain_mask");
    b.op("mountains_b",Op.MULTIPLY,"mountains_a","inland");b.op("mountains",Op.MULTIPLY,"mountains_b","mountain_scale");
    b.op("continent_height",Op.MULTIPLY,"continentalness","continental_scale");
    b.n("hills_noise",190,190,21006,true);b.c("hill_scale",28);b.op("hills_a",Op.MULTIPLY,"hills_noise","hill_scale");
    b.op("hills",Op.MULTIPLY,"hills_a","inland");
    b.n("canyon_noise",440,440,21007,true);b.op("canyon_abs",Op.ABS,"canyon_noise");
    b.bound("canyon_far",Op.SMOOTHSTEP,"canyon_abs",.008,.045);b.op("canyon_far_negative",Op.MULTIPLY,"canyon_far","negative_one");
    b.op("canyon",Op.ADD,"one","canyon_far_negative");b.c("canyon_scale",-42);
    b.op("canyon_inland",Op.MULTIPLY,"canyon","inland");b.op("canyon_cut",Op.MULTIPLY,"canyon_inland","canyon_scale");
    b.op("base_height",Op.ADD,"sea","continent_height");
    if (reliefRevision == 1) {
      b.op("mountain_height",Op.ADD,"base_height","mountains");
      b.op("hill_height",Op.ADD,"mountain_height","hills");
      b.op("height",Op.ADD,"hill_height","canyon_cut");
    } else {
      // The marker is part of the persisted graph, independent of the density
      // VM schema revision. New authoring cannot change existing graph saves.
      b.c("relief_recipe_revision", reliefRevision);
      b.op("relief_revision_zero",Op.MULTIPLY,"relief_recipe_revision","zero");
      b.op("relief_versioned_base",Op.ADD,"base_height","relief_revision_zero");
      b.c("foothill_gain", .24);
      b.bound("foothill_entry",Op.SMOOTHSTEP,"continentalness",-.04,.28);
      b.bound("highland_entry",Op.SMOOTHSTEP,"continentalness",.18,.62);
      b.op("ridge_gain",Op.LERP,"foothill_gain","one","highland_entry");
      b.op("relief_mountain_ramp",Op.MULTIPLY,"foothill_entry","ridge_gain");
      b.op("relief_mountain",Op.MULTIPLY,"mountains","relief_mountain_ramp");
      // Coast hills fade out without moving physical shore or water levels.
      b.bound("coastal_land_ramp",Op.SMOOTHSTEP,"continentalness",-.11,.15);
      b.op("relief_hills",Op.MULTIPLY,"hills","coastal_land_ramp");
      b.n("foothill_noise",510,510,21010,true);
      b.c("foothill_scale",17);
      b.op("foothill_variation",Op.MULTIPLY,"foothill_noise","foothill_scale");
      b.op("relief_foothills",Op.MULTIPLY,"foothill_variation","foothill_entry");
      // Broad erosional highlands form plateaus with soft shoulders, not a
      // hard altitude step, preserving underlying rock strata and caves.
      b.bound("plateau_inland",Op.SMOOTHSTEP,"continentalness",.02,.26);
      b.bound("plateau_erosion",Op.SMOOTHSTEP,"erosion",-.06,.18);
      b.op("plateau_shoulder",Op.MULTIPLY,"plateau_inland","plateau_erosion");
      b.c("plateau_scale",33);
      b.op("relief_plateau",Op.MULTIPLY,"plateau_shoulder","plateau_scale");
      b.op("relief_highlands",Op.ADD,"relief_versioned_base","relief_mountain");
      b.op("relief_hills_and_foothills",Op.ADD,"relief_hills","relief_foothills");
      b.op("relief_uplift",Op.ADD,"relief_hills_and_foothills","relief_plateau");
      b.op("relief_before_canyon",Op.ADD,"relief_highlands","relief_uplift");
      if (reliefRevision == 5)
        b.nodes.addAll(V8ReliefJoinV5.nodes());
      if (reliefRevision == 6)
        b.nodes.addAll(V8ReliefJoinV6.nodes());
      if (reliefRevision == 3 || reliefRevision == 4 || reliefRevision == 5 || reliefRevision == 6) {
        // V3 is frozen: its historical .08..16 coastal release is unchanged.
        // V4 retains the same relief proposal, but constrains its final
        // sea-plane side relative to the historical height without flattening
        // safe inland ridges/plateaus or modifying the canyon incision input.
        b.op("coastal_v1_mountain_height",Op.ADD,"base_height","mountains");
        b.op("coastal_v1_hill_height",Op.ADD,"coastal_v1_mountain_height","hills");
        b.bound("coastal_guard_release",Op.SMOOTHSTEP,"continentalness",.08,.16);
        b.op("guarded_relief_before_canyon",Op.LERP,
            "coastal_v1_hill_height",
            reliefRevision == 5 ? V8ReliefJoinV5.OUTPUT :
            reliefRevision == 6 ? V8ReliefJoinV6.OUTPUT : "relief_before_canyon",
            "coastal_guard_release");
        if (reliefRevision == 4 || reliefRevision == 5 || reliefRevision == 6)
          b.seaBlend("height","coastal_v1_hill_height",
              "guarded_relief_before_canyon","canyon_cut",96,16);
        else
          b.op("height",Op.ADD,"guarded_relief_before_canyon","canyon_cut");
      } else {
        b.op("height",Op.ADD,"relief_before_canyon","canyon_cut");
      }
    }
    b.op("y",Op.Y);b.op("negative_y",Op.MULTIPLY,"y","negative_one");b.op("height_difference",Op.ADD,"height","negative_y");
    b.c("density_scale",1./48);b.op("base_density",Op.MULTIPLY,"height_difference","density_scale");
    b.op("surface_distance",Op.ABS,"height_difference");b.bound("far_surface",Op.SMOOTHSTEP,"surface_distance",8,88);
    b.op("far_negative",Op.MULTIPLY,"far_surface","negative_one");b.op("near_surface",Op.ADD,"one","far_negative");
    b.n("overhang_noise",76,52,21008,false);b.c("overhang_scale",3.5);
    b.op("overhang_a",Op.MULTIPLY,"overhang_noise","overhang_scale");b.op("overhang_b",Op.MULTIPLY,"overhang_a","near_surface");
    b.op("overhang",Op.MULTIPLY,"overhang_b","inland");b.op("terrain_density",Op.ADD,"base_density","overhang");
    if(floatingFeatures) {
      b.c("island_height",370);b.op("island_delta",Op.ADD,"island_height","negative_y");b.op("island_distance",Op.ABS,"island_delta");
      b.c("island_inverse_thickness",-1./18);b.op("island_vertical",Op.MULTIPLY,"island_distance","island_inverse_thickness");
      b.n("island_field",240,240,21009,true);b.c("island_horizontal_scale",8);b.op("island_horizontal",Op.MULTIPLY,"island_field","island_horizontal_scale");
      b.op("island_shape",Op.ADD,"island_vertical","island_horizontal");b.c("island_threshold",-.8);b.op("island_density",Op.ADD,"island_shape","island_threshold");
      b.op("density",Op.MAX,"terrain_density","island_density");
    } else b.op("density",Op.ADD,"terrain_density","zero");
    return new Definition(VERSION,"density",b.nodes);
  }

  private static final class Builder {
    final List<Node> nodes=new ArrayList<>();
    void c(String id,double value) {nodes.add(Node.constant(id,value));}
    void op(String id,Op op,String... in) {nodes.add(Node.operation(id,op,in));}
    void bound(String id,Op op,String in,double low,double high) {nodes.add(Node.bounded(id,op,in,low,high));}
    void seaBlend(String id,String oldHeight,String proposal,String canyon,double sea,double width) {
      nodes.add(new Node(id,Op.SEA_SIDE_BLEND,List.of(oldHeight,proposal,canyon),
          List.of(sea,width),null));
    }
    void n(String id,double horizontal,double vertical,long salt,boolean flat) {
      nodes.add(Node.noise(id,new NoiseSpec(1/horizontal,1/vertical,1/horizontal,3,2,.5,salt),flat));
    }
  }
}
