import java.nio.file.*;
public class PackagedReduction {
 public static void main(String[] a) throws Exception {
  for(String n:new String[]{"LodMesher","ColumnarChunk"}) {
   var c=Class.forName("com.shnewbs.deepprimal.core.lod."+n);
   var actual=Path.of(c.getProtectionDomain().getCodeSource().getLocation().toURI()).toRealPath();
   if(!actual.equals(Path.of(a[0]).toRealPath()))throw new AssertionError(actual);
   System.out.println("PACKAGED_CLASS "+n+" "+actual);
  }
  com.shnewbs.deepprimal.core.lod.LodReductionChecks.main(new String[0]);
  com.shnewbs.deepprimal.core.lod.LodPartBoundsChecks.main(new String[0]);
 }
}
