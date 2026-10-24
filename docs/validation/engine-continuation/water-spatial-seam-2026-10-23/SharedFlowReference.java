import java.util.Arrays;
import java.util.Random;

/** Staged executable reference only. Not a shipping renderer or solver.
 * Share a normalized distribution at global corners; blend sampled colors,
 * never wrapped texture coordinates. At most nine unique directions per cell.
 * Temporal history, GPU transport, filtering, alpha and performance are pending. */
public final class SharedFlowReference {
  @FunctionalInterface interface Field { int direction(int x, int z); } // -1=dry,16=still
  static void corner(Field f,int x,int z,double[] out) {
    Arrays.fill(out,0);int wet=0;
    for(int dz=-1;dz<=0;dz++)for(int dx=-1;dx<=0;dx++) {
      int d=f.direction(x+dx,z+dz);
      if(d < -1 || d > 16)throw new IllegalArgumentException("direction");
      if(d>=0){out[d]++;wet++;}
    }
    if(wet!=0)for(int d=0;d<17;d++)out[d]/=wet;
  }
  static double[] weights(Field f,int x,int z,double u,double v) {
    if(!(u>=0&&u<=1&&v>=0&&v<=1))throw new IllegalArgumentException("local point");
    double[] result=new double[17],c=new double[17];
    for(int dz=0;dz<2;dz++)for(int dx=0;dx<2;dx++){
      corner(f,x+dx,z+dz,c);double w=(dx==0?1-u:u)*(dz==0?1-v:v);
      for(int d=0;d<17;d++)result[d]+=w*c[d];
    }
    return result;
  }
  static double color(double[] weights,int worldX,int worldZ,double u,double v) {
    double value=0;
    for(int d=0;d<17;d++)if(weights[d]!=0) {
      // Independent continuous periodic diagnostic. Double cell-origin phase
      // matches the same float coefficients used by the production phase helper.
      double fx=d==16?1:(float)Math.cos(d*Math.PI/8),fz=d==16?0:(float)Math.sin(d*Math.PI/8);
      double pu=.5*(fz*worldX-fx*worldZ),pv=.5*(fx*worldX+fz*worldZ);
      double a=pu-Math.floor(pu)+.5*(fz*(u-.5)-fx*(v-.5));
      double b=pv-Math.floor(pv)+.5*(fx*(u-.5)+fz*(v-.5));
      value+=weights[d]*(.5+.25*Math.cos(a*2*Math.PI)+.25*Math.sin(b*2*Math.PI));
    }
    return value;
  }
  static int hashDirection(int x,int z,int salt){long n=((long)x*0x9e3779b97f4a7c15L)^((long)z*0xc2b2ae3d27d4eb4fL)^salt;n^=n>>>29;return (int)Long.remainderUnsigned(n,18)-1;}
  static void require(boolean condition,String message){if(!condition)throw new AssertionError(message);}
  public static void main(String[] args) {
    var random=new Random(20261023L);double maxWeight=0,maxColor=0;int edgeChecks=0,uniformChecks=0,maxSupport=0;
    for(int n=0;n<100000;n++) {
      int origin=switch(n%4){case 0->0;case 1->-16;case 2->29999984;default->-29999984;};
      int x=origin+random.nextInt(16),z=-origin+random.nextInt(16),salt=random.nextInt();double t=random.nextDouble();
      Field field=(cx,cz)->hashDirection(cx,cz,salt);boolean axis=(n&1)==0;
      var left=weights(field,x,z,axis?1:t,axis?t:1);var right=weights(field,x+(axis?1:0),z+(axis?0:1),axis?0:t,axis?t:0);
      for(int d=0;d<17;d++){maxWeight=Math.max(maxWeight,Math.abs(left[d]-right[d]));require(left[d]>=0&&left[d]<=1,"invalid weight");}
      double a=color(left,x,z,axis?1:t,axis?t:1),b=color(right,x+(axis?1:0),z+(axis?0:1),axis?0:t,axis?t:0);maxColor=Math.max(maxColor,Math.abs(a-b));edgeChecks++;
      var inside=weights(field,x,z,random.nextDouble(),random.nextDouble());int support=0;for(var w:inside)if(w>0)support++;maxSupport=Math.max(maxSupport,support);require(support<=9,"unbounded support");
    }
    for(int d=0;d<17;d++) {
      final int dir=d;Field field=(x,z)->dir;
      for(int n=0;n<1000;n++) {
        var w=weights(field,random.nextInt(),random.nextInt(),random.nextDouble(),random.nextDouble());
        for(int at=0;at<17;at++)require(Math.abs(w[at]-(at==d?1:0))<1e-15,"uniform direction changed");uniformChecks++;
      }
    }
    require(maxWeight==0,"shared-edge weights differ");require(maxColor<1e-7,"shared-edge sampled colors differ");
    System.out.println("{\"pass\":true,\"staged_only\":true,\"shared_edge_checks\":"+edgeChecks+",\"uniform_direction_checks\":"+uniformChecks+",\"max_weight_difference\":"+maxWeight+",\"max_color_difference\":"+maxColor+",\"max_unique_directions\":"+maxSupport+",\"production_integrated\":false,\"performance_accepted\":false}");
  }
}
