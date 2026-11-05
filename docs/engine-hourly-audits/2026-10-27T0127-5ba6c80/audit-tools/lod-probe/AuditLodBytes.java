import com.shnewbs.deepprimal.core.HorizonMesh;
import com.shnewbs.deepprimal.core.lod.LodMesher;
import java.math.BigInteger;
import java.util.*;
public final class AuditLodBytes {
 static int cases;
 static final class Sequential extends LinkedList<HorizonMesh> {
  Sequential(Collection<HorizonMesh> source){super(source);}
  @Override public HorizonMesh get(int n){throw new AssertionError("sequential indexed access");}
 }
 static HorizonMesh mesh(int v,int i){return new HorizonMesh(new float[0],new int[v],new int[i],new int[0],new short[0],-64,320);}
 static void check(List<HorizonMesh> a,List<HorizonMesh> b,List<HorizonMesh> c){
  BigInteger n=BigInteger.ZERO;
  for(var list:List.of(a,b,c)) for(var m:list) n=n.add(BigInteger.valueOf(m.colors().length).multiply(BigInteger.valueOf(20))).add(BigInteger.valueOf(m.indices().length).multiply(BigInteger.TWO));
  long actual=new LodMesher.Levels(a,b,c).bytes();
  if(!n.equals(BigInteger.valueOf(actual)))throw new AssertionError("expected="+n+" got="+actual);
  cases++;
 }
 public static void main(String[] args){
  var rng=new Random(202610262333L);
  for(int sample=0;sample<400;sample++){
   List<List<HorizonMesh>> lists=new ArrayList<>();
   for(int d=0;d<3;d++){var list=new ArrayList<HorizonMesh>();for(int n=rng.nextInt(11);n>0;n--)list.add(mesh(rng.nextInt(201),rng.nextInt(777)));lists.add(list);}
   for(int mask=0;mask<8;mask++){
    var a=(mask&1)==0?List.<HorizonMesh>of():lists.get(0);var b=(mask&2)==0?List.<HorizonMesh>of():lists.get(1);var c=(mask&4)==0?List.<HorizonMesh>of():lists.get(2);
    check(a,b,c);check(new Sequential(a),new Sequential(b),new Sequential(c));
   }
   var mutable=lists.get(0);check(mutable,lists.get(1),lists.get(2));mutable.clear();check(mutable,lists.get(1),lists.get(2));mutable.add(mesh(17,31));check(mutable,lists.get(1),lists.get(2));
  }
  var shared=Collections.nCopies(100000,mesh(3,6));check(shared,List.of(),shared);check(new Sequential(shared),shared,List.of());
  System.out.println("AUDIT_LOD_BYTES_PASS cases="+cases+" BigIntegerOracle=true allEightDetailMasks=true mutableRecomputed=true sequentialNoGet=true largeRepeatedReferences=true");
 }
}
