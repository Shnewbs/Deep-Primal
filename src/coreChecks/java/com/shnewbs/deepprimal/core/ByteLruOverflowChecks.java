package com.shnewbs.deepprimal.core;
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
    testMutableWeights();
    System.out.println("BYTE_LRU_OVERFLOW_PASS checks="+checks+" bigIntegerOracle=true mutableWeightOracle=true");
  }
  /** Weight is a reservation made at insertion, not a fresh reading of mutable payloads. */
  private static final class MutablePayload {
    long weight;
    MutablePayload(long weight) { this.weight = weight; }
  }
  private record SavedPayload(MutablePayload value, long admittedWeight) {}

  private static void testMutableWeights() {
    var eviction = new ByteLru<Integer,MutablePayload>(10,4,p->p.weight);
    MutablePayload first = new MutablePayload(6);
    eviction.put(1,first);
    first.weight=1;
    eviction.put(2,new MutablePayload(7));
    check(eviction.bytes()==7 && eviction.size()==1 && eviction.get(1)==null,
        "Mutable eviction must use original admitted weight");

    var replacement = new ByteLru<Integer,MutablePayload>(10,4,p->p.weight);
    MutablePayload previous = new MutablePayload(6);
    replacement.put(1,previous);
    previous.weight=100;
    replacement.put(1,new MutablePayload(2));
    check(replacement.bytes()==2 && replacement.size()==1,
        "Mutable replacement must use original admitted weight");

    Random random=new Random(0xB17EL);
    for(long limit:new long[]{0,1,10,100,Long.MAX_VALUE})
      for(int cap:new int[]{0,1,4,20}) {
        var actual=new ByteLru<Integer,MutablePayload>(limit,cap,p->p.weight);
        var oracle=new LinkedHashMap<Integer,SavedPayload>(16,.75f,true);
        for(int step=0;step<4000;step++) {
          int key=random.nextInt(32);
          if(random.nextInt(3)==0) {
            SavedPayload expected=oracle.get(key);
            check(actual.get(key)==(expected==null?null:expected.value()),"mutable lookup");
          } else if(random.nextInt(3)==0 && !oracle.isEmpty()) {
            for(SavedPayload saved:oracle.values()) saved.value().weight=random.nextInt(200);
          } else {
            long weight=switch(random.nextInt(7)) {
              case 0->0L; case 1->1L; case 2->5L; case 3->10L;
              case 4->100L; case 5->Long.MAX_VALUE; default->random.nextInt(200);
            };
            MutablePayload value=new MutablePayload(weight);
            actual.put(key,value);
            oracle.remove(key);
            if(cap>0 && weight<=limit) {
              while(!oracle.isEmpty() && (mutableSum(oracle)>limit-weight || oracle.size()>=cap))
                oracle.remove(oracle.keySet().iterator().next());
              oracle.put(key,new SavedPayload(value,weight));
            }
          }
          check(actual.size()==oracle.size(),"mutable entry count at "+step);
          check(actual.bytes()==mutableSum(oracle),"mutable byte budget at "+step);
        }
      }
  }
  private static long mutableSum(Map<Integer,SavedPayload> map) {
    long total=0;
    for(SavedPayload saved:map.values()) total=Math.addExact(total,saved.admittedWeight());
    return total;
  }
  static BigInteger sum(Map<Integer,Long> map){BigInteger s=BigInteger.ZERO;for(long v:map.values())s=s.add(BigInteger.valueOf(v));return s;}
  static void check(boolean ok,String reason){if(!ok)throw new AssertionError(reason);checks++;}
}
