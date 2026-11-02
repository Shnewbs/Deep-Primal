package com.shnewbs.deepprimal.core;
import java.util.HashMap;
import java.util.Map;
public class ChunkCache {
  private final Map<Object,Object> items=new HashMap<>();
  public synchronized <T> T get(Object key){@SuppressWarnings("unchecked") T v=(T)items.get(key);return v;}
  public synchronized void put(Object key,Object value,long weight){items.put(key,value);}
  public synchronized void clear(){items.clear();}
  public synchronized int size(){return items.size();}
}
