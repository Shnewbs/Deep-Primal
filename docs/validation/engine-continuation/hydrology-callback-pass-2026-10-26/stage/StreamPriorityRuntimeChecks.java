package com.shnewbs.deepprimal.engine;
import java.util.*;
public final class StreamPriorityRuntimeChecks {
 public static void main(String[] a)throws Exception {
  Class<?> w=Class.forName(HydrologyRuntime.class.getName()+"$Work"),c=Class.forName(HydrologyRuntime.class.getName()+"$Cursor");
  var wc=w.getDeclaredConstructor();wc.setAccessible(true);var cc=c.getDeclaredConstructor();cc.setAccessible(true);
  var chunks=w.getDeclaredField("chunks");chunks.setAccessible(true);var sources=w.getDeclaredField("streamChunks");sources.setAccessible(true);
  var count=c.getDeclaredField("streamCount");count.setAccessible(true);var surface=c.getDeclaredField("streamSurface");surface.setAccessible(true);
  var refresh=HydrologyRuntime.class.getDeclaredMethod("refreshSource",w,long.class,c);refresh.setAccessible(true);
  for(int n=0;n<1000;n++) {
   Object work=wc.newInstance(),first=cc.newInstance(),second=cc.newInstance();Map map=(Map)chunks.get(work);Object idx=sources.get(work);
   map.put(0L,first);map.put(1L,second);
   for(Object v:new Object[]{first,second}) {count.setInt(v,1);((double[])surface.get(v))[0]=42;}
   refresh.invoke(null,work,0L,first);refresh.invoke(null,work,1L,second);
   count.setInt(first,0);refresh.invoke(null,work,0L,first);count.setInt(first,1);refresh.invoke(null,work,0L,first);
   if(!((Map.Entry<?,?>)((Iterable<?>)idx).iterator().next()).getKey().equals(0L))throw new AssertionError("runtime same-window source priority changed");
   count.setInt(first,0);refresh.invoke(null,work,0L,first);
   var phase=idx.getClass().getDeclaredMethod("beginPass");phase.setAccessible(true);phase.invoke(idx);
   count.setInt(first,1);refresh.invoke(null,work,0L,first);
   if(!((Map.Entry<?,?>)((Iterable<?>)idx).iterator().next()).getKey().equals(1L))throw new AssertionError("runtime next-pass registration priority wrong");
  }
  System.out.println("SOURCE_PRIORITY_RUNTIME_PASS cases=1000 sameWindow=true nextPass=true actualRuntimeMethods=true nativeWorld=false");
 }
}
