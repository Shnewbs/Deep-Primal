package com.shnewbs.deepprimal.engine;
import java.util.*;
public final class StreamSourceIndexChecks {
 record Value(double height,int generation) {}
 public static void main(String[] args) {
  var index=new StreamSourceIndex<Value>();var model=new LinkedHashMap<Long,Value>();
  var random=new Random(20261026);int checks=0;
  for(int i=0;i<20000;i++){
   long key=random.nextInt(512)-256;
   if(random.nextInt(4)==0){index.remove(key);model.remove(key);}
   else {var v=new Value(random.nextInt(16),i);index.put(key,v,v.height());model.put(key,v);}
   var expected=new ArrayList<>(model.entrySet());expected.sort((a,b)->Double.compare(b.getValue().height(),a.getValue().height()));
   var it=index.iterator();int seen=0;
   for(var e:expected){if(!it.hasNext())throw new AssertionError("missing");var got=it.next();if(!got.getKey().equals(e.getKey())||got.getValue()!=e.getValue())throw new AssertionError("ordering/replacement");seen++;}
   if(it.hasNext()||seen!=index.size())throw new AssertionError("extra");checks++;
  }
  int size=index.size();
  for(double bad:new double[]{Double.NaN,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY}){
   try {index.put(999L,new Value(bad,0),bad);throw new AssertionError("accepted invalid");}catch(IllegalArgumentException expected){}
   if(index.size()!=size)throw new AssertionError("mutated rejection");checks++;
  }
  var iterator=index.iterator();if(iterator.hasNext()){
   var node=iterator.next();try{node.setValue(new Value(0,0));throw new AssertionError("mutable entry");}catch(UnsupportedOperationException expected){}
  }
  for(long key:new ArrayList<>(model.keySet()))index.remove(key);
  if(index.size()!=0||index.iterator().hasNext())throw new AssertionError("unload");
  // Stable ticks perform no updates: iteration can stop at any caller budget without copying.
  for(int i=0;i<8192;i++)index.put(i,new Value(i%16,i),i%16);
  for(int budget:new int[]{0,1,17,128,8192}){int visited=0;var it=index.iterator();while(visited<budget&&it.hasNext()){it.next();visited++;}if(visited!=budget)throw new AssertionError("prefix");checks++;}
  System.out.println("STREAM_SOURCE_INDEX_PASS mutations=20000 checks="+checks+" ordering=true ties=true replacements=true unload=true callerPrefix=true");
 }
}
