import com.shnewbs.deepprimal.core.density.*;
import java.nio.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;

/** Private evidence exporter, never bundled in the mod. */
public final class DensityEvidence {
  public static void main(String[] args)throws Exception {
    if(args.length==0||args[0].equals("fingerprint")) {
      var hash=MessageDigest.getInstance("SHA-256");var buffer=ByteBuffer.allocate(8);
      for(long seed:new long[]{0,1,73,Long.MIN_VALUE})for(boolean floating:new boolean[]{false,true}) {
        var graph=TerrainShapeV8.definition(floating).compile(seed);
        for(int i=0;i<256;i++) {
          double value=graph.sample((i-128)*229001./1.,(i%769)-128,(127-i)*228997.);
          buffer.clear();buffer.putLong(Double.doubleToLongBits(value));hash.update(buffer.array());
        }
      }
      System.out.println("DENSITY_FINGERPRINT "+HexFormat.of().formatHex(hash.digest()));return;
    }
    Path out=Path.of(args[1]);Files.createDirectories(out);
    for(long seed:new long[]{0,1,73,20261020}) {
      var graph=TerrainShapeV8.definition(false).compile(seed);var data=new StringBuilder("x,z,height\n");
      for(int z=-4096;z<=4096;z+=64)for(int x=-4096;x<=4096;x+=64)data.append(x).append(',').append(z).append(',').append(graph.channel("height",x,0,z)).append('\n');
      Files.writeString(out.resolve("height-"+seed+".csv"),data);
    }
    var shape=TerrainShapeV8.definition(false).compile(73);var data=new StringBuilder("x,y,density\n");
    for(int y=-128;y<=640;y+=4)for(int x=-1024;x<=1024;x+=8)data.append(x).append(',').append(y).append(',').append(shape.sample(x,y,0)).append('\n');
    Files.writeString(out.resolve("density-section.csv"),data);
    System.out.println("DENSITY_PREVIEW_DATA_PASS seeds=4 heightSamples=66564 sectionSamples=49601");
  }
}
