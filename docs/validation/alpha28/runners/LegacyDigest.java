import com.shnewbs.deepprimal.core.*;
import java.security.*;import java.nio.*;import java.util.*;
public class LegacyDigest {
 public static void main(String[] args)throws Exception {
  for(int rev=1;rev<=6;rev++)for(long seed:new long[]{0,73,20261020}){
   var t=new TerrainEngine(seed,rev);var d=MessageDigest.getInstance("SHA-256");var b=ByteBuffer.allocate(100);int count=0;
   for(int z=-2048;z<=2048;z+=256)for(int x=-2048;x<=2048;x+=256){var s=t.sample(x,z);b.clear();
    for(double v:new double[]{t.raw(x,z),s.ground(),s.water(),s.rain(),s.temperature(),s.slope(),s.sediment(),s.flowX(),s.flowZ(),s.speed()})b.putDouble(v);
    b.putInt(t.biome(x,z)).putInt(s.rock());d.update(b.array(),0,b.position());count++;
   }
   System.out.println(rev+","+seed+","+count+","+HexFormat.of().formatHex(d.digest()));
  }
 }
}
