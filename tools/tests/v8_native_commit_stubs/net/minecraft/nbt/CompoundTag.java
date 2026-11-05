package net.minecraft.nbt;
import java.util.Map;
import java.util.HashMap;
public final class CompoundTag {
  private final Map<String,String> values=new HashMap<>();
  public boolean has(String key){return values.containsKey(key);}
  public void putString(String key,String value){values.put(key,value);}
  public String getString(String key){return values.get(key);}
}
