package com.shnewbs.deepprimal.core;
import java.util.*;
/** A wider independent body search and uncompiled trigonometric oracle. */
public final class DepositPlanChecks {
  private static void require(boolean b,String s){if(!b)throw new AssertionError(s);}
  public static void main(String[]args){run();}
  public static void run(){
    long checked=0,hits=0;var noise=new TerrainNoise(917);
    for(var shape:DepositPlan.Shape.values())for(var ref:DepositPlan.Reference.values()) {
      var d=new DepositPlan.Definition("test:body",shape,ref,32,.8,.6,15,85,19,6,12,Set.of(Geology.Rock.values()),List.of(new DepositPlan.Content(4,1),new DepositPlan.Content(7,3)));
      var plan=new DepositPlan(noise,List.of(d));
      for(int x=-43;x<44;x+=3)for(int z=-41;z<42;z+=3) {
        var sample=new TerrainEngine.Sample(250,Double.NaN,.5,.5,.1,0,0,0,0,0);
        var g=new Geology(noise).column(x,z,sample);
        var c=plan.chunk(Math.floorDiv(x,16)*16,Math.floorDiv(z,16)*16,(a,b)->250).column(x,z,g,250);
        for(int y=-70;y<250;y+=2) {int actual=c.at(y),expected=oracle(noise,d,x,y,z);
          require(actual==expected,"deposit oracle "+shape+" "+ref+" "+x+" "+y+" "+z+" "+actual+" != "+expected);checked++;if(actual>=0)hits++;}
      }
    }
    require(hits>1000,"meaningful deposit coverage");
    try {new DepositPlan(noise,Collections.nCopies(33,new DepositPlan.Definition("test:a",DepositPlan.Shape.LENS,DepositPlan.Reference.ABSOLUTE,32,1,1,0,0,1,1,1,Set.of(Geology.Rock.TUFF),List.of(new DepositPlan.Content(0,1)))));throw new AssertionError("unbounded authoring accepted");}catch(IllegalArgumentException expected){}
    System.out.println("DEPOSIT_PLAN_CORE_PASS cases="+checked+" hits="+hits+" shapes=4 verticalReferences=3 signedChunkSeams=true widerOracle=true");
  }
  static int oracle(TerrainNoise n,DepositPlan.Definition d,int x,int y,int z){
    if(y>=245)return -1;long salt=d.salt();
    for(int gx=-8;gx<=8;gx++)for(int gz=-8;gz<=8;gz++) {
      if(n.hash(gx,gz,salt)>=d.occurrence())continue;
      double bx=gx*32+32*(.2+.6*n.hash(gx,gz,salt+1)),bz=gz*32+32*(.2+.6*n.hash(gx,gz,salt+2));
      double by=15+n.hash(gx,gz,salt+3)*70;
      if(d.reference()==DepositPlan.Reference.DEPTH)by=250-by;
      if(d.reference()==DepositPlan.Reference.GEOLOGICAL)by-=n.field(bx,bz,720,1801)*32+Math.sin(bx*.006+bz*.003)*9+(n.field(bx,bz,1400,1802)>=0?18:-18);
      double angle=n.hash(gx,gz,salt+4)*Math.PI,dip=d.shape()==DepositPlan.Shape.PIPE?Math.PI/2:d.shape()==DepositPlan.Shape.DISC?0:(n.hash(gx,gz,salt+5)-.5)*.8;
      double a=(x-bx)*Math.cos(angle)+(z-bz)*Math.sin(angle),b=-(x-bx)*Math.sin(angle)+(z-bz)*Math.cos(angle),dy=y-by;
      double u=(a*Math.cos(dip)+dy*Math.sin(dip))/19,v=(-a*Math.sin(dip)+dy*Math.cos(dip))/6,w=b/12,q=u*u+v*v+w*w;
      if(q<=1&&n.hash(x,z,salt^((long)y*0x9e3779b97f4a7c15L))<Math.clamp(.6*(1.4-q*.65),0,1))return n.hash(x,z,(salt+43)^((long)y*0xd1b54a32d192ed03L))<.25?4:7;
    }return -1;
  }
}
