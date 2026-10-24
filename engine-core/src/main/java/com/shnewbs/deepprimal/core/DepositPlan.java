package com.shnewbs.deepprimal.core;

import java.util.*;
import java.util.function.DoubleBinaryOperator;

/** Compiled, bounded data-authored deposits. One immutable candidate list per chunk, no per-voxel IO. */
public final class DepositPlan {
  public enum Shape { LENS, VEIN, PIPE, DISC }
  public enum Reference { ABSOLUTE, DEPTH, GEOLOGICAL }
  public record Content(int value,int weight) {
    public Content { if(value<0||weight<1||weight>10000)throw new IllegalArgumentException("Deposit content"); }
  }
  public record Definition(String id,Shape shape,Reference reference,int spacing,double occurrence,
      double density,int min,int max,double radiusX,double radiusY,double radiusZ,
      Set<Geology.Rock> hosts,List<Content> contents) {
    public Definition {
      Objects.requireNonNull(shape);Objects.requireNonNull(reference);
      if(id==null||!id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")||spacing<32||spacing>1024
          ||!Double.isFinite(occurrence)||occurrence<0||occurrence>1
          ||!Double.isFinite(density)||density<0||density>1||min>max||min< -2048||max>2048
          ||!validRadius(radiusX)||!validRadius(radiusY)||!validRadius(radiusZ))throw new IllegalArgumentException("Deposit definition "+id);
      hosts=Set.copyOf(hosts);contents=List.copyOf(contents);
      if(hosts.isEmpty()||contents.isEmpty()||contents.size()>16)throw new IllegalArgumentException("Empty/oversized deposit "+id);
      if(reference==Reference.DEPTH && min<5)throw new IllegalArgumentException("Depth deposits require soil cover");
    }
    private static boolean validRadius(double n) { return Double.isFinite(n)&&n>=.5&&n<=96; }
    public double bound() { return Math.sqrt(radiusX*radiusX+radiusY*radiusY+radiusZ*radiusZ); }
    public long salt() { long h=0xcbf29ce484222325L;for(int i=0;i<id.length();i++)h=(h^id.charAt(i))*0x100000001b3L;return h; }
  }
  private record Body(Definition d,double x,double y,double z,double ca,double sa,double cd,double sd,
      double bound,int weight,long salt) {}
  private final TerrainNoise noise;
  private final List<Definition> definitions;
  public DepositPlan(TerrainNoise noise,List<Definition> definitions) {
    this.noise=Objects.requireNonNull(noise);
    if(definitions.size()>32)throw new IllegalArgumentException("At most 32 deposit definitions");
    var ids=new HashSet<String>();int budget=0;
    for(var d:definitions) {
      if(!ids.add(d.id))throw new IllegalArgumentException("Duplicate deposit "+d.id);
      int side=(int)Math.ceil((16+2*d.bound())/d.spacing)+2;budget+=side*side;
    }
    if(budget>1024)throw new IllegalArgumentException("Deposit neighborhood exceeds 1024 candidates/chunk");
    this.definitions=definitions.stream().sorted(Comparator.comparing(Definition::id)).toList();
  }
  public final class Chunk {
    private final List<Body> bodies;
    private Chunk(List<Body> bodies) { this.bodies=List.copyOf(bodies); }
    public int candidates() { return bodies.size(); }
    public Column column(int x,int z,Geology.Column geology,int surface) {
      var prepared=new ArrayList<Prepared>();
      for(var b:bodies) {
        double dx=x-b.x,dz=z-b.z;
        if(Math.abs(dx)>b.bound||Math.abs(dz)>b.bound)continue;
        var d=b.d;double along=dx*b.ca+dz*b.sa,across=-dx*b.sa+dz*b.ca;
        double rx2=d.radiusX*d.radiusX,ry2=d.radiusY*d.radiusY;
        double a=b.sd*b.sd/rx2+b.cd*b.cd/ry2;
        double beta=2*along*b.cd*b.sd*(1/rx2-1/ry2);
        double c=along*along*(b.cd*b.cd/rx2+b.sd*b.sd/ry2)+across*across/(d.radiusZ*d.radiusZ);
        double disc=beta*beta-4*a*(c-1);
        if(disc<0)continue;
        double root=Math.sqrt(disc);
        prepared.add(new Prepared(b,a,beta,c,(int)Math.ceil(b.y+(-beta-root)/(2*a)),(int)Math.floor(b.y+(-beta+root)/(2*a))));
      }
      return new Column(x,z,geology,surface,prepared.toArray(Prepared[]::new));
    }
    public int at(int x,int y,int z,Geology.Column geology,int surface) { return column(x,z,geology,surface).at(y); }
  }
  private record Prepared(Body body,double a,double b,double c,int low,int high) {}
  public final class Column {
    private final int x,z,surface;private final Geology.Column geology;private final Prepared[] bodies;
    private Column(int x,int z,Geology.Column geology,int surface,Prepared[] bodies) {
      this.x=x;this.z=z;this.geology=geology;this.surface=surface;this.bodies=bodies;
    }
    public int at(int y) {
      if(y>=surface-5)return -1;
      Geology.Rock host=null;
      for(var p:bodies) {
        if(y<p.low||y>p.high)continue;
        var b=p.body;var d=b.d;double dy=y-b.y;
        double distance=(p.a*dy+p.b)*dy+p.c;
        if(distance>1)continue;
        if(host==null)host=geology.rock(y);
        if(!d.hosts.contains(host))continue;
        double grade=Math.clamp(d.density*(1.4-distance*.65),0,1);
        if(noise.hash(x,z,b.salt^((long)y*0x9e3779b97f4a7c15L))>=grade)continue;
        int chosen=(int)(noise.hash(x,z,(b.salt+43)^((long)y*0xd1b54a32d192ed03L))*b.weight);
        for(var c:d.contents) { chosen-=c.weight;if(chosen<0)return c.value; }
      }
      return -1;
    }
  }
  public Chunk chunk(int ox,int oz,DoubleBinaryOperator surface) {
    var bodies=new ArrayList<Body>();
    for(var d:definitions) {
      double bound=d.bound();long salt=d.salt();
      int x0=(int)Math.floor((ox-bound)/d.spacing),x1=(int)Math.floor(((long)ox+15+bound)/d.spacing);
      int z0=(int)Math.floor((oz-bound)/d.spacing),z1=(int)Math.floor(((long)oz+15+bound)/d.spacing);
      for(int gx=x0;gx<=x1;gx++)for(int gz=z0;gz<=z1;gz++) {
        if(noise.hash(gx,gz,salt)>=d.occurrence)continue;
        double x=(long)gx*d.spacing+d.spacing*(.2+.6*noise.hash(gx,gz,salt+1));
        double z=(long)gz*d.spacing+d.spacing*(.2+.6*noise.hash(gx,gz,salt+2));
        if(x+bound<ox||z+bound<oz||x-bound>(long)ox+15||z-bound>(long)oz+15)continue;
        double value=d.min+noise.hash(gx,gz,salt+3)*(d.max-d.min),ground=surface.applyAsDouble(x,z);
        double y=switch(d.reference) {
          case ABSOLUTE -> value;
          case DEPTH -> ground-value;
          case GEOLOGICAL -> {
            double fold=noise.field(x,z,720,1801)*32+Math.sin(x*.006+z*.003)*9;
            double fault=noise.field(x,z,1400,1802)>=0?18:-18;
            yield value-fold-fault;
          }
        };
        double angle=noise.hash(gx,gz,salt+4)*Math.PI;
        double dip=d.shape==Shape.PIPE?Math.PI/2:d.shape==Shape.DISC?0:(noise.hash(gx,gz,salt+5)-.5)*.8;
        bodies.add(new Body(d,x,y,z,Math.cos(angle),Math.sin(angle),Math.cos(dip),Math.sin(dip),bound,d.contents.stream().mapToInt(Content::weight).sum(),salt));
      }
    }
    if(bodies.size()>1024)throw new IllegalStateException("Compiled candidate bound violated");
    return new Chunk(bodies);
  }
}
