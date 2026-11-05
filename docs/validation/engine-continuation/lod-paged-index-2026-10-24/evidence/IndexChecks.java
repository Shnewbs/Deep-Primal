import com.shnewbs.deepprimal.core.lod.*;
import java.nio.file.*;
import java.util.*;
import java.math.BigInteger;
import java.security.*;
public class IndexChecks {
 static int checks;
 static void require(boolean b,String why){checks++;if(!b)throw new AssertionError(why);}
 static long key(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
 static int x(long k){return (int)(k>>32);}static int z(long k){return(int)k;}
 static BigInteger dist(long k,int cx,int cz){var dx=BigInteger.valueOf((long)x(k)-cx);var dz=BigInteger.valueOf((long)z(k)-cz);return dx.multiply(dx).add(dz.multiply(dz));}
 static List<Long> expected(List<Long> all,int cx,int cz,int cap){return all.stream().sorted(Comparator.comparing((Long k)->dist(k,cx,cz)).thenComparingInt(k->x(k)).thenComparingInt(k->z(k))).limit(cap).toList();}
 static List<Long> scan(Path root,int cx,int cz,int cap,int budget,long entries)throws Exception{
  try(var index=new LodDiskIndex(root,cx,cz,cap)){
   long before=0;int turns=0;
   while(!index.isComplete()){
    index.advance(budget);long after=index.visitedEntries();require(after-before<=budget,"entry budget includes junk");before=after;
    require(index.retainedKeys()<=cap,"retained key bound");turns++;require(turns<10000,"finite progress");
   }
   require(index.visitedEntries()==entries,"all entries visited");require(!index.isOpen(),"directory closed at completion");
   var keys=index.keys();require(keys.size()<=cap,"snapshot key bound");System.out.println("INDEX_SCAN entries="+entries+" turns="+turns+" budget="+budget+" retained="+keys.size()+" camera="+cx+","+cz);
   return keys;
  }
 }
 public static void main(String[]args)throws Exception{
  Path root=Files.createTempDirectory("deepprimal-paged-index-");
  try{
   var all=new ArrayList<Long>();var store=new LodStore(root);int count=8205;
   for(int i=0;i<count;i++){
    int cx=i-4102,cz=(i%17)-8;all.add(key(cx,cz));var b=new ColumnarChunk.Builder(cx,cz,-384,1,1);
    for(int c=0;c<256;c++)b.column(new int[]{0});require(store.write(b.finish(List.of("minecraft:stone"))),"real snapshot creation");
   }
   var prefix=BaselineIndex.scan(root);require(prefix.size()==8192,"actual renderer prefix size");
   long omitted=all.stream().filter(k->!prefix.contains(k)).findFirst().orElseThrow();int cx=x(omitted),cz=z(omitted);
   require(!prefix.contains(key(cx,cz)),"camera's saved chunk omitted beyond prefix");
   require(store.read(cx,cz).state(15,-384,15).equals("minecraft:stone"),"omitted page really readable");
   System.out.println("LOD_PREFIX_BUG_REPRODUCED files="+count+" retained="+prefix.size()+" omittedReadableCamera="+cx+","+cz);
   var keys=scan(root,cx,cz,8192,256,count);require(keys.equals(expected(all,cx,cz,8192)),"full nearest set/order versus BigInteger oracle");require(keys.get(0)==omitted,"camera page recovered");
   String[] junk={"junk","0_0.plod.pending","01_0.plod","+1_0.plod","-0_0.plod","1_2_extra.plod","2147483648_0.plod","garbage.plod"};
   for(String name:junk)Files.writeString(root.resolve(name),"ignored");
   for(int[] camera:new int[][]{{-4102,-8},{4102,8},{Integer.MIN_VALUE,Integer.MAX_VALUE},{Integer.MAX_VALUE,Integer.MIN_VALUE}}){
    var found=scan(root,camera[0],camera[1],31,17,count+junk.length);require(found.equals(expected(all,camera[0],camera[1],31)),"travel rescan exact order with invalid names");
   }
   // Exercise the full65-bit squared-distance sum with tiny bounded inputs.
   Path extremes=root.resolve("extremes");Files.createDirectory(extremes);var corners=new ArrayList<Long>();
   for(int xx:new int[]{Integer.MIN_VALUE,Integer.MIN_VALUE+1,0,Integer.MAX_VALUE-1,Integer.MAX_VALUE})for(int zz:new int[]{Integer.MIN_VALUE,0,Integer.MAX_VALUE}){corners.add(key(xx,zz));Files.writeString(extremes.resolve(xx+"_"+zz+".plod"),"");}
   for(int[] camera:new int[][]{{Integer.MIN_VALUE,Integer.MIN_VALUE},{Integer.MAX_VALUE,Integer.MAX_VALUE},{0,0}}){var found=scan(extremes,camera[0],camera[1],15,1,15);require(found.equals(expected(corners,camera[0],camera[1],15)),"unsigned squared distance and deterministic ties");}
   try(var pending=new LodDiskIndex(root,0,0,31)){pending.advance(3);require(pending.isOpen(),"cursor live before cancel");pending.close();require(!pending.isOpen()&&pending.retainedKeys()==0,"cancel closes and releases keys");boolean threw=false;try{pending.keys();}catch(IllegalStateException ok){threw=true;}require(threw,"partial result cannot masquerade as complete");threw=false;try{pending.advance(1);}catch(IllegalStateException ok){threw=true;}require(threw,"cancelled cursor cannot continue");}
   for(int bad:new int[]{0,-1,257})try(var empty=new LodDiskIndex(root.resolve("absent"),0,0,1)){boolean threw=false;try{empty.advance(bad);}catch(IllegalArgumentException ok){threw=true;}require(threw,"invalid turn bound");}
   for(int bad:new int[]{0,-1,8193}){boolean threw=false;try{new LodDiskIndex(root,0,0,bad);}catch(IllegalArgumentException ok){threw=true;}require(threw,"invalid heap bound");}
   try(var missing=new LodDiskIndex(root.resolve("absent"),0,0,1)){require(missing.isComplete()&&!missing.isOpen()&&missing.keys().isEmpty(),"missing cache no mutation");}
   System.out.println("LOD_PAGED_INDEX_PASS checks="+checks+" actualSnapshots="+count+" prefixOmissionFixed=true fullNearestOracle=true turnBound256=true keyBound8192=true invalidNames=true travelRescans=true extremeCoordinates=true cancellation=true");
  }finally{try(var paths=Files.walk(root)){for(var p:paths.sorted(Comparator.reverseOrder()).toList())Files.delete(p);}}
 }
}
