import com.shnewbs.deepprimal.core.ByteLru;
import java.math.BigInteger;
import java.util.*;

public final class ByteLruOverflowChecks {
  private static int checks;
  public static void main(String[] args) {
    var witness = new ByteLru<Integer,Long>(Long.MAX_VALUE, 4, x -> x);
    witness.put(1, Long.MAX_VALUE); witness.put(2, 1L);
    check(witness.bytes()==1 && witness.size()==1 && witness.get(1)==null,
        "overflow bypass: bytes="+witness.bytes()+" size="+witness.size());
    for(long limit:new long[]{0,1,100,Long.MAX_VALUE/2,Long.MAX_VALUE})
      for(int cap:new int[]{0,1,4,32}) {
        var actual=new ByteLru<Integer,Long>(limit,cap,x->x);
        var expected=new LinkedHashMap<Integer,Long>(16,.75f,true);
        var rng=new Random(40917+cap);
        for(int n=0;n<4000;n++) {
          int key=rng.nextInt(40);
          if(rng.nextInt(4)==0)check(Objects.equals(actual.get(key),expected.get(key)),"lookup");
          else {
            long value=switch(rng.nextInt(7)){case 0->0;case 1->1;case 2->Long.MAX_VALUE;case 3->Long.MAX_VALUE/2;case 4->limit;default->rng.nextInt(200);};
            actual.put(key,value); expected.remove(key);
            if(value<=limit&&cap>0)expected.put(key,value);
            while(sum(expected).compareTo(BigInteger.valueOf(limit))>0||expected.size()>cap) {
              var it=expected.entrySet().iterator();it.next();it.remove();
            }
          }
          check(actual.bytes()>=0&&BigInteger.valueOf(actual.bytes()).equals(sum(expected)),"exact accounting");
          check(actual.size()==expected.size(),"entry bound");
          // Read in the same order in both implementations so recency remains comparable.
          for(int k=0;k<40;k++)check(Objects.equals(actual.get(k),expected.get(k)),"retention "+k);
        }
        long bytes=actual.bytes();int size=actual.size();
        try {actual.put(100,-1L);throw new AssertionError("negative accepted");}
        catch(IllegalArgumentException expectedError) {check(actual.bytes()==bytes&&actual.size()==size,"negative request mutation");}
        actual.clear();check(actual.bytes()==0&&actual.size()==0&&actual.hits()==0,"clear");
      }
    System.out.println("BYTE_LRU_OVERFLOW_PASS checks="+checks+" bigIntegerOracle=true");
  }
  static BigInteger sum(Map<Integer,Long> map){BigInteger s=BigInteger.ZERO;for(long v:map.values())s=s.add(BigInteger.valueOf(v));return s;}
  static void check(boolean ok,String reason){if(!ok)throw new AssertionError(reason);checks++;}
}
