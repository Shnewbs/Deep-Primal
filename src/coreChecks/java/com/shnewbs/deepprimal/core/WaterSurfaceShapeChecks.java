package com.shnewbs.deepprimal.core;
import java.util.Random;
/** Independent surface invariants and actual generated patch topology. */
final class WaterSurfaceShapeChecks{
  static void run(){
    int checked=0;
    for(int v=0;v<=20;v++)for(int u=0;u<=20;u++){
      double x=u/20.,z=v/20.;
      if(WaterTransport.surface(1,1,1,1,.0625,x,z)!=1)throw new AssertionError("Cell-center dimple");
      if(Math.abs(WaterTransport.surface(.2,.4,.3,.5,.95,x,z)-(.2+.2*x+.1*z))>1e-12)throw new AssertionError("Planar tent");
      checked++;
    }
    var random=new Random(20261022);
    for(int n=0;n<20000;n++){
      double a=.0625+random.nextDouble()*.9375,b=.0625+random.nextDouble()*.9375,c=.0625+random.nextDouble()*.9375,d=.0625+random.nextDouble()*.9375;
      double h=WaterTransport.surface(a,b,c,d,random.nextDouble(),random.nextDouble(),random.nextDouble());
      if(h<Math.min(Math.min(a,b),Math.min(c,d))-1e-12||h>Math.max(Math.max(a,b),Math.max(c,d))+1e-12)throw new AssertionError("New surface extremum");
    }
    byte[] units=new byte[WaterPatchMesher.CELLS],solid=new byte[units.length];
    for(int z=-1;z<=16;z++)for(int x=-1;x<=16;x++){
      units[WaterPatchMesher.index(x,0,z)]=(byte)(((x+z)&1)==0?16:1);
      solid[WaterPatchMesher.index(x,-1,z)]=1;
    }
    var mesh=WaterPatchMesher.build(new WaterPatchMesher.Snapshot(units,solid,new int[units.length],new int[256]),null,true);
    int top=0;
    for(int i=0;i<mesh.vertices().length;i+=WaterPatchMesher.STRIDE){
      if(mesh.vertices()[i+9]!=(8|(8<<4))||mesh.vertices()[i+10]!=0)continue;
      if(mesh.vertices()[i+3]!=17/32f)throw new AssertionError("Patch checkerboard dimples");top++;
    }
    if(top!=4)throw new AssertionError("Unnecessary planar subdivision");
    System.out.println("WATER_SURFACE_SHAPE_PASS planarSamples="+checked+" extremaCases=20000 planarPatchVertices="+top);
  }
}
