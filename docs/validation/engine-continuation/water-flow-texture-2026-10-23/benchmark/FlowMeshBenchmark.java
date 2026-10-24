import com.shnewbs.deepprimal.core.WaterPatchMesher;
import java.util.*;

public final class FlowMeshBenchmark {
  private static volatile long sink;
  public static void main(String[] args)throws Exception {
    int stride=WaterPatchMesher.class.getField("STRIDE").getInt(null);
    var constructors=WaterPatchMesher.Snapshot.class.getConstructors();
    for(String scenario:new String[]{"still-flat","flow-flat","flow-slopes","flow-porous"}) {
      int cells=WaterPatchMesher.CELLS;byte[] u=new byte[cells],s=new byte[cells],f=new byte[cells];int[] l=new int[cells],c=new int[256];Arrays.fill(c,0xff3f76e4);Arrays.fill(f,(byte)16);
      int layers=scenario.equals("flow-porous")?8:1;
      for(int y=-1;y<=16;y++)for(int z=-1;z<=16;z++)for(int x=-1;x<=16;x++) {
        int i=WaterPatchMesher.index(x,y,z);if(y==-1)s[i]=1;
        if(y>=0&&y<layers) {
          if(scenario.equals("flow-porous") && ((x+y+z)&1)!=0)continue;
          u[i]=(byte)(scenario.equals("flow-slopes")?1+Math.floorMod(x*7+z*11,16):16);
          if(!scenario.equals("still-flat"))f[i]=(byte)Math.floorMod(x+3*z+y,16);
        }
      }
      WaterPatchMesher.Snapshot snap=null;
      for(var ctor:constructors)if(ctor.getParameterCount()==5)snap=(WaterPatchMesher.Snapshot)ctor.newInstance(u,s,l,c,f);
      if(snap==null)snap=new WaterPatchMesher.Snapshot(u,s,l,c);
      var first=WaterPatchMesher.build(snap,snap,true);long digest=17;
      for(int i=0;i<first.vertices().length;i+=stride)for(int j=0;j<14;j++)if(j!=5&&j!=6)digest=digest*1000003+Float.floatToRawIntBits(j==10?(int)first.vertices()[i+j]&7:first.vertices()[i+j]);
      for(int i=0;i<1000;i++)sink+=WaterPatchMesher.build(snap,snap,true).count();
      long[] ns=new long[400];
      for(int i=0;i<ns.length;i++){long start=System.nanoTime();var mesh=WaterPatchMesher.build(snap,snap,true);ns[i]=System.nanoTime()-start;sink+=mesh.count();}
      Arrays.sort(ns);System.out.println("{\"scenario\":\""+scenario+"\",\"stride\":"+stride+",\"vertices\":"+first.count()+",\"floatPayloadBytes\":"+(first.vertices().length*4L)+",\"geometryDigest\":\""+Long.toHexString(digest)+"\",\"samples\":400,\"warmups\":1000,\"medianNs\":"+ns[200]+",\"p95Ns\":"+ns[379]+"}");
    }
  }
}
