package com.shnewbs.deepprimal.engine;
import java.lang.reflect.*;
import java.util.*;
public class StreamSourceLifecycleChecks {
 public static void main(String[] args) throws Exception {
  Class<?> work=Class.forName("com.shnewbs.deepprimal.engine.HydrologyRuntime$Work"),cursor=Class.forName("com.shnewbs.deepprimal.engine.HydrologyRuntime$Cursor");
  var wc=work.getDeclaredConstructor();wc.setAccessible(true);var cc=cursor.getDeclaredConstructor();cc.setAccessible(true);
  Object w=wc.newInstance(),c=cc.newInstance();var chunks=work.getDeclaredField("chunks");chunks.setAccessible(true);var sources=work.getDeclaredField("streamChunks");sources.setAccessible(true);
  var count=cursor.getDeclaredField("streamCount");count.setAccessible(true);var surface=cursor.getDeclaredField("streamSurface");surface.setAccessible(true);
  var refresh=HydrologyRuntime.class.getDeclaredMethod("refreshSource",work,long.class,cursor);refresh.setAccessible(true);
  Map map=(Map)chunks.get(w);StreamSourceIndex<?> index=(StreamSourceIndex<?>)sources.get(w);
  map.put(1L,c);count.setInt(c,1);((double[])surface.get(c))[0]=42;refresh.invoke(null,w,1L,c);
  if(index.size()!=1)throw new AssertionError("register");
  count.setInt(c,0);refresh.invoke(null,w,1L,c);if(index.size()!=0)throw new AssertionError("dry removal");
  count.setInt(c,1);refresh.invoke(null,w,1L,c);map.put(1L,cc.newInstance());refresh.invoke(null,w,1L,c);if(index.size()!=0)throw new AssertionError("stale generation");
  System.out.println("SOURCE_LIFECYCLE_PASS actualRuntime=true register=true dryRemoval=true staleGeneration=true");
 }
}
