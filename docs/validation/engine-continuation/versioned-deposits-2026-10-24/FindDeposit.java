import com.shnewbs.deepprimal.core.*;
public class FindDeposit {
 public static void main(String[] args) {
  for(long seed:new long[]{0,20261020}){
   var e=new TerrainEngine(seed,5); var g=new Geology(e.noise);var d=new DepositField(e.noise);
   var dummy=new TerrainEngine.Sample(512,0,.6,.5,.2,.4,0,0,0,0);int found=0;
   for(int z=-512;z<=512&&found<4;z+=7)for(int x=-2048;x<=2048&&found<4;x+=32) {
    var host=g.column(x,z,dummy);var f=d.column(x,z,host);
    for(int y=-96;y<256&&found<4;y++)if(f.ore(y,512)!=Geology.Ore.NONE&&host.ore(y,512)==Geology.Ore.NONE) {
     var s=e.column(x,z);int bed=(int)Math.floor(s.ground());
     if(y>=bed-8)continue;
     var c=!s.wet()&&s.sediment()<.05?new CaveColumn(e.noise,x,z,s.ground(),-128,639):null;
     if(c!=null&&c.contains(y))continue;
     System.out.println("seed="+seed+" x="+x+" z="+z+" y="+y+" bed="+bed+" ore="+f.ore(y,bed)+" chunk="+Math.floorDiv(x,16)+","+Math.floorDiv(z,16));found++;break;
    }
   }
  }
 }
}
