package com.shnewbs.deepprimal.core.density;

import java.util.*;
import static com.shnewbs.deepprimal.core.density.DensityGraph.*;

/** Experimental V8 shape recipe. Not a registered generator and not a routed hydrology stage. */
public final class TerrainShapeV8 {
  private TerrainShapeV8() {}

  /** Named channels expose continentalness, erosion, peaks_valleys, canyon and height. */
  public static Definition definition(boolean floatingFeatures) {
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
    b.op("base_height",Op.ADD,"sea","continent_height");b.op("mountain_height",Op.ADD,"base_height","mountains");
    b.op("hill_height",Op.ADD,"mountain_height","hills");b.op("height",Op.ADD,"hill_height","canyon_cut");
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
    void n(String id,double horizontal,double vertical,long salt,boolean flat) {
      nodes.add(Node.noise(id,new NoiseSpec(1/horizontal,1/vertical,1/horizontal,3,2,.5,salt),flat));
    }
  }
}
