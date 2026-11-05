import com.shnewbs.deepprimal.core.*;
import java.util.*;
public class FindCoverage {
 static final long[] SEEDS={0,42,-914,20261020,Long.MIN_VALUE,Long.MAX_VALUE,71,123456789,-1,99473,81231,-68742,2701,1901,314159,271828};
 public static void main(String[] args) {
  System.out.println("[");boolean first=true;int total=0;
  for(long seed:SEEDS) {
   var e=new TerrainEngine(seed,5);var g=new Geology(e.noise);var d=new DepositField(e.noise);
   var dummy=new TerrainEngine.Sample(512,0,.6,.5,.2,.4,0,0,0,0);boolean[] found=new boolean[9];int types=0;
   for(int gx=-64;gx<=64&&types<8;gx++)for(int gz=-64;gz<=64&&types<8;gz++) {
    int x=(int)Math.round((long)gx*32+8+e.noise.hash(gx,gz,1810)*16),z=(int)Math.round((long)gz*32+8+e.noise.hash(gx,gz,1811)*16);
    int y=(int)Math.round(-96+e.noise.hash(gx,gz,1812)*352);
    var host=g.column(x,z,dummy);var ore=d.column(x,z,host).ore(y,512);if(ore==Geology.Ore.NONE||found[ore.ordinal()])continue;
    var s=e.column(x,z);if(y>=(int)Math.floor(s.ground())-8)continue;
    var caves=!s.wet()&&s.sediment()<.05?new CaveColumn(e.noise,x,z,s.ground(),-128,639):null;
    if(caves!=null&&caves.contains(y))continue;
    found[ore.ordinal()]=true;types++;total++;
    int cx=Math.floorDiv(x,16),cz=Math.floorDiv(z,16),active=0,max=0;
    for(int zz=cz*16;zz<cz*16+16;zz++)for(int xx=cx*16;xx<cx*16+16;xx++){
     int count=d.column(xx,zz,g.column(xx,zz,dummy)).activeCandidateCount();if(count>0)active++;max=Math.max(max,count);
    }
    System.out.print((first?"":",\n")+"{\"seed\":"+seed+",\"x\":"+x+",\"y\":"+y+",\"z\":"+z+",\"cx\":"+cx+",\"cz\":"+cz+",\"ore\":\""+ore+"\",\"rock\":\""+host.rock(y)+"\",\"active_columns\":"+active+",\"max_overlap\":"+max+"}");first=false;
   }
   if(types!=8)throw new AssertionError("missing types for "+seed+": "+types);
  }
  System.out.println("\n]");System.err.println("COVERAGE_SITES_PASS seeds="+SEEDS.length+" witnesses="+total);
 }
}
