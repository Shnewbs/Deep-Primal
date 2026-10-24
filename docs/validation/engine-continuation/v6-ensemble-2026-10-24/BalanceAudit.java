package com.shnewbs.deepprimal.core;
import java.util.*;
/** Fixed ensemble, wider independent trig oracle; no selected feature witnesses. */
public class BalanceAudit {
 static final String[] IDS={"coal","copper","iron","gold","redstone","lapis","diamond","emerald"};
 record Body(DepositPlan.Definition d,double x,double y,double z,double ca,double sa,double cd,double sd) {}
 static List<DepositPlan.Definition> defaults(){
  var all=Set.of(Geology.Rock.values());
  var hosts=List.of(Set.of(Geology.Rock.SANDSTONE,Geology.Rock.TUFF),Set.of(Geology.Rock.ANDESITE,Geology.Rock.CALCITE),all,Set.of(Geology.Rock.GRANITE,Geology.Rock.DIORITE),Set.of(Geology.Rock.BASALT,Geology.Rock.GRANITE),Set.of(Geology.Rock.CALCITE),Set.of(Geology.Rock.BASALT,Geology.Rock.DEEPSLATE),Set.of(Geology.Rock.CALCITE,Geology.Rock.GRANITE));
  var out=new ArrayList<DepositPlan.Definition>();
  for(int i=0;i<8;i++)out.add(new DepositPlan.Definition("deepprimal:"+IDS[i],i==5?DepositPlan.Shape.DISC:i==6?DepositPlan.Shape.PIPE:i==3?DepositPlan.Shape.VEIN:DepositPlan.Shape.LENS,DepositPlan.Reference.DEPTH,i<3?64:96,i<3?.40:.22,i<3?.43:.25,i<3?12:25,i<3?100:180,i<3?14:8,i<3?5:3,i<3?9:5,hosts.get(i),List.of(new DepositPlan.Content(i,1))));
  return out;
 }
 static List<Body> reference(TerrainEngine t,List<DepositPlan.Definition> defs,int x,int z){
  var out=new ArrayList<Body>();var n=t.noise;
  for(var d:defs.stream().sorted(Comparator.comparing(DepositPlan.Definition::id)).toList()) {
   long salt=d.salt(); int cx=Math.floorDiv(x,d.spacing()),cz=Math.floorDiv(z,d.spacing());
   // Wider 7x7 cells, independent of production chunk bounds/column clipping.
   for(int gx=cx-3;gx<=cx+3;gx++)for(int gz=cz-3;gz<=cz+3;gz++){
    if(n.hash(gx,gz,salt)>=d.occurrence())continue;
    double bx=(long)gx*d.spacing()+d.spacing()*(.2+.6*n.hash(gx,gz,salt+1)),bz=(long)gz*d.spacing()+d.spacing()*(.2+.6*n.hash(gx,gz,salt+2));
    if(Math.abs(x-bx)>d.bound()||Math.abs(z-bz)>d.bound())continue;
    double by=t.column(bx,bz).ground()-(d.min()+n.hash(gx,gz,salt+3)*(d.max()-d.min()));
    double a=n.hash(gx,gz,salt+4)*Math.PI,dip=d.shape()==DepositPlan.Shape.PIPE?Math.PI/2:d.shape()==DepositPlan.Shape.DISC?0:(n.hash(gx,gz,salt+5)-.5)*.8;
    out.add(new Body(d,bx,by,bz,Math.cos(a),Math.sin(a),Math.cos(dip),Math.sin(dip)));
   }
  }return out;
 }
 static int[] oracle(TerrainNoise n,List<Body> bodies,int x,int y,int z,Geology.Column g,int ground){
  int result=-1,hits=0;if(y>=ground-5)return new int[]{-1,0};
  for(var b:bodies){var d=b.d;double along=(x-b.x)*b.ca+(z-b.z)*b.sa,across=-(x-b.x)*b.sa+(z-b.z)*b.ca,dy=y-b.y;
   double u=(along*b.cd+dy*b.sd)/d.radiusX(),v=(-along*b.sd+dy*b.cd)/d.radiusY(),w=across/d.radiusZ(),q=u*u+v*v+w*w;
   if(q>1||!d.hosts().contains(g.rock(y)))continue;
   if(n.hash(x,z,d.salt()^((long)y*0x9e3779b97f4a7c15L))>=Math.clamp(d.density()*(1.4-q*.65),0,1))continue;
   hits++;if(result<0)result=d.contents().getFirst().value();
  }return new int[]{result,hits};
 }
 public static void main(String[] args){
  long cases=0,hits=0,overlaps=0,columns=0,candidates=0;var pairs=new TreeMap<String,Long>();long[] ores=new long[8];
  long[] seeds={0,1,20261020,Long.MIN_VALUE};int[] belts={-24000,-16000,-8000,0,8000,16000,24000,32000};var defs=defaults();
  for(long seed:seeds){var locationRandom=new Random(0x5eeda27L ^ seed);var t=new TerrainEngine(seed,6);var plan=new DepositPlan(t.noise,defs);var reversed=new ArrayList<>(defs);Collections.reverse(reversed);var reversePlan=new DepositPlan(t.noise,reversed);
   for(int belt:belts)for(int site=0;site<32;site++){
    int ox=(locationRandom.nextInt(2048)-1024)*16,oz=belt+(locationRandom.nextInt(64)-32)*16;var chunk=plan.chunk(ox,oz,(x,z)->t.column(x,z).ground());var rev=reversePlan.chunk(ox,oz,(x,z)->t.column(x,z).ground());candidates+=chunk.candidates();
    for(int dz=0;dz<16;dz++)for(int dx=0;dx<16;dx++){int x=ox+dx,z=oz+dz;var sample=t.sample(x,z);int ground=(int)sample.ground();var geo=new Geology(t.noise).column(x,z,sample);var actual=chunk.column(x,z,geo,ground);var reverse=rev.column(x,z,geo,ground);var bodies=reference(t,defs,x,z);columns++;
     for(int y=-372;y<Math.min(640,ground-5);y++){int a=actual.at(y);int[] expected=oracle(t.noise,bodies,x,y,z,geo,ground);
      if(a!=expected[0]||a!=reverse.at(y))throw new AssertionError("Mismatch seed="+seed+" x="+x+" y="+y+" z="+z+" actual="+a+" oracle="+expected[0]);cases++;if(a>=0){hits++;ores[a]++;pairs.merge(IDS[a]+":"+geo.rock(y),1L,Long::sum);}if(expected[1]>1)overlaps++;
     }
    }
   }
  }
  if(hits==0)throw new AssertionError("empty ensemble");
  System.out.println("BALANCE_ORACLE_PASS cases="+cases+" columns="+columns+" seeds=4 belts=8 chunks=1024 hits="+hits+" overlaps="+overlaps+" candidates="+candidates);
  System.out.println("ore_counts="+Arrays.toString(ores));System.out.println("host_pairs="+pairs);
 }
}
