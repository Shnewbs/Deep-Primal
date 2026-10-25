package com.shnewbs.deepprimal.core;
import com.shnewbs.deepprimal.core.density.*;
import java.math.BigDecimal;
import java.util.Random;

public final class DensityGridFiniteChecks {
  public static void main(String[] args) {
    int checks=0;
    double[] endpoints={-Double.MAX_VALUE,-Double.MAX_VALUE/2,-1,-0.0,0,1,Double.MAX_VALUE/2,Double.MAX_VALUE};
    for(int axis=0;axis<3;axis++)for(double a:endpoints)for(double b:endpoints){
      final int dim=axis;
      var g=DensityGrid.sample((x,y,z)->(dim==0?x:dim==1?y:z)==0?a:b,0,0,0,1,1,1,2,2,2);
      for(double t:new double[]{0,Math.nextUp(0.0),.125,.25,.5,.75,.875,Math.nextDown(1.0),1}){
        double v=g.sample(axis==0?t:0,axis==1?t:0,axis==2?t:0);
        if(!Double.isFinite(v))throw new AssertionError("nonfinite axis="+axis+" a="+a+" b="+b+" t="+t+" got="+v);
        double expected=new BigDecimal(a).multiply(BigDecimal.ONE.subtract(new BigDecimal(t))).add(new BigDecimal(b).multiply(new BigDecimal(t))).doubleValue();
        double tolerance=4*Math.ulp(Math.max(Math.abs(a),Math.abs(b)));
        if(Math.abs(v-expected)>tolerance)throw new AssertionError("convex oracle");
        if(t==0&&Double.doubleToLongBits(v)!=Double.doubleToLongBits(a)||t==1&&Double.doubleToLongBits(v)!=Double.doubleToLongBits(b))throw new AssertionError("endpoint bits");
        checks++;
      }
    }
    Random random=new Random(8281);
    for(int i=0;i<10000;i++){
      double a=random.nextDouble()*2e12-1e12,b=random.nextDouble()*2e12-1e12,t=random.nextDouble();
      var g=DensityGrid.sample((x,y,z)->x==0?a:b,0,0,0,1,1,1,2,2,2);
      if(Double.doubleToLongBits(g.sample(t,0,0))!=Double.doubleToLongBits(a+(b-a)*t))throw new AssertionError("ordinary bits changed");
      checks++;
    }
    System.out.println("DENSITY_GRID_FINITE_PASS checks="+checks);
  }
}
