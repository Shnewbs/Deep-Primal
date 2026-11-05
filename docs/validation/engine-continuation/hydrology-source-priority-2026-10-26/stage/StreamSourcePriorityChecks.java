package com.shnewbs.deepprimal.engine;
import java.util.*;
public final class StreamSourcePriorityChecks {
 static final class Value { double height; boolean wet=true; Value(double h){height=h;} }
 static final StreamSourceIndex<Value> index=new StreamSourceIndex<>();
 static final LinkedHashMap<Long,Value> model=new LinkedHashMap<>();
 static long checks;
 static void put(long k,double h){Value v=model.get(k);if(v==null){v=new Value(h);model.put(k,v);}v.height=h;v.wet=true;index.put(k,v,h);}
 static void dry(long k){Value v=model.get(k);if(v!=null)v.wet=false;index.deactivate(k);}
 static void phase(){model.entrySet().removeIf(e->!e.getValue().wet);index.beginPass();}
 static void check(){
  var expected=new ArrayList<>(model.entrySet());expected.removeIf(e->!e.getValue().wet);
  expected.sort((a,b)->Double.compare(b.getValue().height,a.getValue().height));
  var it=index.iterator();
  for(var e:expected){if(!it.hasNext()||!it.next().getKey().equals(e.getKey()))throw new AssertionError("source priority changed after dry/re-add");}
  if(it.hasNext()||index.size()!=expected.size())throw new AssertionError("active size");
  for(long k=0;k<64;k++)if(index.contains(k)!=model.containsKey(k))throw new AssertionError("registration visibility");
  checks++;
 }
 public static void main(String[] a){
  put(0,42);put(1,42);dry(0);put(0,42);check(); // same registration window retains priority
  dry(0);phase();put(0,42);check(); // after cleanup boundary gets new priority
  var random=new Random(20261026);
  for(int i=0;i<100000;i++){
   long k=random.nextInt(64);
   switch(random.nextInt(5)){
    case 0,1 -> put(k,random.nextInt(8));
    case 2 -> dry(k);
    case 3 -> {model.remove(k);index.remove(k);}
    case 4 -> phase();
   }
   check();
  }
  for(long k=0;k<64;k++){model.remove(k);index.remove(k);}phase();check();
  try{var f=StreamSourceIndex.class.getDeclaredField("byKey");f.setAccessible(true);if(!((Map<?,?>)f.get(index)).isEmpty())throw new AssertionError("dormant entries survived unload");}catch(ReflectiveOperationException e){throw new AssertionError(e);}
  System.out.println("SOURCE_PRIORITY_PASS checks="+checks+" transitions=100000 sameWindow=true nextPass=true unloadReleasesDormant=true");
 }
}
