import com.shnewbs.deepprimal.core.*;
public class DeltaDiagnostic {
 public static void main(String[] args) {
  int fans=0, raisedDry=0, dry=0, wet=0, surveyed=0;
  for(long seed:new long[]{0,73,20261020}) {
   var a=new TerrainEngine(seed,7,TerrainOptions.DEFAULT,3);
   var b=new TerrainEngine(seed,7,TerrainOptions.DEFAULT,2);
   for(int cz=-1;cz<=1;cz++)for(int cx=-1;cx<=1;cx++)for(var d:a.waterways(cx*8192,cz*8192).deltaProbes()) {
    int found=0,orig=0;fans++;
    for(double u:new double[]{.15,.4,.65,.9})for(double v:new double[]{-.9,-.75,-.5,-.25,0,.25,.5,.75,.9}) {
     double x=d.mouthX()-d.seawardX()*u*d.reach()-d.seawardZ()*v*d.halfWidth();
     double z=d.mouthZ()-d.seawardZ()*u*d.reach()+d.seawardX()*v*d.halfWidth();
     var now=a.column(x,z);var old=b.column(x,z);surveyed++;
     if(now.wet())wet++;else dry++;
     if(!now.wet()&&now.ground()>old.ground()+.05){raisedDry++;found++;}
    }
    double spread=Math.max(d.reach(),d.halfWidth())*.5;
    for(int n=-2;n<=2;n++) {
     double x=d.mouthX()-d.seawardX()*spread*.4-d.seawardZ()*n*spread*.3;
     double z=d.mouthZ()-d.seawardZ()*spread*.4+d.seawardX()*n*spread*.3;
     if(a.column(x,z).wet())orig++;
    }
    System.out.printf("fan seed=%d mouth=(%.2f,%.2f) raisedDry=%d originalWet=%d/5%n",seed,d.mouthX(),d.mouthZ(),found,orig);
   }
  }
  System.out.printf("DELTA_DIAGNOSTIC fans=%d surveyed=%d raisedDry=%d dry=%d wet=%d%n",fans,surveyed,raisedDry,dry,wet);
 }
}
