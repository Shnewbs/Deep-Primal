package com.shnewbs.deepprimal.engine;
import java.lang.reflect.*;
import java.util.*;
/** Actual staged runtime methods; no server/native world is constructed here. */
public final class StreamSourceLifecycleChecks {
 public static void main(String[] args) throws Exception {
  Class<?> work=Class.forName("com.shnewbs.deepprimal.engine.HydrologyRuntime$Work"),cursor=Class.forName("com.shnewbs.deepprimal.engine.HydrologyRuntime$Cursor");
  var wc=work.getDeclaredConstructor();wc.setAccessible(true);var cc=cursor.getDeclaredConstructor();cc.setAccessible(true);
  var chunks=work.getDeclaredField("chunks");chunks.setAccessible(true);var sources=work.getDeclaredField("streamChunks");sources.setAccessible(true);
  var survey=work.getDeclaredField("survey");survey.setAccessible(true);var regions=work.getDeclaredField("regions");regions.setAccessible(true);
  var count=cursor.getDeclaredField("streamCount");count.setAccessible(true);var surface=cursor.getDeclaredField("streamSurface");surface.setAccessible(true);
  var refresh=HydrologyRuntime.class.getDeclaredMethod("refreshSource",work,long.class,cursor);refresh.setAccessible(true);
  int checks=0;
  for(int i=0;i<1000;i++) {
   Object w=wc.newInstance(),c=cc.newInstance(),newer=cc.newInstance();
   Map map=(Map)chunks.get(w);StreamSourceIndex<?> index=(StreamSourceIndex<?>)sources.get(w);
   long key=i%2==0?Long.MIN_VALUE+i:Long.MAX_VALUE-i;
   map.put(key,c);count.setInt(c,1);((double[])surface.get(c))[0]=42;refresh.invoke(null,w,key,c);
   if(index.size()!=1)throw new AssertionError("register");
   count.setInt(c,0);refresh.invoke(null,w,key,c);if(index.size()!=0)throw new AssertionError("dry removal");
   count.setInt(c,1);refresh.invoke(null,w,key,c);map.put(key,newer);refresh.invoke(null,w,key,c);
   if(index.size()!=0)throw new AssertionError("stale generation retained");
   count.setInt(newer,1);((double[])surface.get(newer))[0]=43;refresh.invoke(null,w,key,newer);
   refresh.invoke(null,w,key,c);
   if(index.size()!=1||index.iterator().next().getValue()!=newer)
    throw new AssertionError("retired cursor removed newer source generation");
   count.setInt(c,0);refresh.invoke(null,w,key,c);
   if(index.size()!=1||index.iterator().next().getValue()!=newer)throw new AssertionError("dry retired cursor removed newer source");
   checks+=5;
  }
  var forget=HydrologyRuntime.class.getDeclaredMethod("forgetChunk",work,long.class);forget.setAccessible(true);
  Object w=wc.newInstance();Map map=(Map)chunks.get(w);Set pending=(Set)survey.get(w);Map regional=(Map)regions.get(w);StreamSourceIndex<?> index=(StreamSourceIndex<?>)sources.get(w);
  for(long k=0;k<512;k++) {Object c=cc.newInstance();count.setInt(c,1);((double[])surface.get(c))[0]=k%7;map.put(k,c);pending.add(k);regional.put(k,null);refresh.invoke(null,w,k,c);}
  for(long k=0;k<512;k++) {
   forget.invoke(null,w,k);forget.invoke(null,w,k); // unload after the missing-FULL path is idempotent
   if(map.containsKey(k)||pending.contains(k)||index.contains(k)||index.size()!=511-k)throw new AssertionError("partial forget");
   if(regional.size()>map.size())throw new AssertionError("unbounded regional retention");checks++;
  }
  if(!map.isEmpty()||!pending.isEmpty()||!regional.isEmpty()||index.size()!=0)throw new AssertionError("final unload");
  System.out.println("SOURCE_LIFECYCLE_PASS checks="+checks+" generations=1000 actualRuntime=true staleRefresh=true dryRemoval=true unloadIdempotence=true allIndexesEmpty=true");
 }
}
