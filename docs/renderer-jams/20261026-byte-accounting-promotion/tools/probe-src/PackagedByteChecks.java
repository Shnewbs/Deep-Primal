import java.nio.file.*;
public final class PackagedByteChecks {
 public static void main(String[] args) throws Exception {
  Path loaded=Path.of(com.shnewbs.deepprimal.core.lod.LodMesher.class.getProtectionDomain().getCodeSource().getLocation().toURI());
  if(!loaded.toRealPath().equals(Path.of(args[0]).toRealPath()))throw new AssertionError("Product shadowed "+loaded);
  System.out.println("PACKAGED_LOD_CLASS "+loaded);
  com.shnewbs.deepprimal.core.lod.LodByteAccountingChecks.main(new String[0]);
  com.shnewbs.deepprimal.core.lod.ByteAccountingChecks.main(new String[0]);
 }
}
