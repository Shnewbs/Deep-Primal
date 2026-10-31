package net.minecraft.nbt;
public final class CompoundTag {
  public boolean marker;
  public Object get(String key) {
    return marker && "deepprimal_v8_generation".equals(key) ? "existing marker" : null;
  }
}
