import com.shnewbs.deepprimal.core.lod.*;
import java.security.*;
import java.util.*;
public class PackagedChecks {
 public static void main(String[] args)throws Exception{
  int i=0;for(Class<?> type:List.of(LodStore.class,LodDiskIndex.class,LodIndexRequest.class)){
   String hash;try(var in=type.getResourceAsStream(type.getSimpleName()+".class")){hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(in.readAllBytes()));}
   if(!hash.equals(args[i++]))throw new AssertionError("Loaded class mismatch "+type+" "+hash);
   System.out.println("PACKAGED_SOURCE_IDENTITY_PASS class="+type.getName()+" sha256="+hash+" origin="+type.getProtectionDomain().getCodeSource().getLocation());
  }
  RequestChecks.main(new String[0]);
 }
}
