package com.shnewbs.deepprimal.core;

/** Conserved fixed-point water and constituent amounts; 256 quanta equal one native fluid unit. */
public final class WaterAccount {
  public static final int SCALE = 256, OCEAN_SALT = 35;
  public static final long MAX = 1L << 40;
  public record Parcel(long water, long salt, long sediment) {
    public static final Parcel EMPTY = new Parcel(0, 0, 0);
    public Parcel {
      if (water < 0 || water > MAX || salt < 0 || sediment < 0
          || salt > MAX * 1024 || sediment > MAX * 1024)
        throw new IllegalArgumentException("Invalid water parcel");
    }
  }
  private final long capacity;
  private long water, salt, sediment;
  public WaterAccount(long capacity, Parcel initial) {
    if (capacity < 0 || capacity > MAX || initial.water() > capacity)
      throw new IllegalArgumentException("Water capacity");
    this.capacity = capacity;
    water = initial.water(); salt = initial.salt(); sediment = initial.sediment();
  }
  public long water() { return water; }
  public long room() { return capacity - water; }
  public long capacity() { return capacity; }
  public Parcel snapshot() { return new Parcel(water, salt, sediment); }
  public Parcel preview(long requested) {
    long n = Math.clamp(requested, 0, water);
    return n == 0 ? Parcel.EMPTY : new Parcel(n, portion(salt, n, water), portion(sediment, n, water));
  }
  private static long portion(long mass, long amount, long volume) {
    if (mass == 0 || amount == 0) return 0;
    if (mass <= Long.MAX_VALUE / amount) return mass * amount / volume;
    return java.math.BigInteger.valueOf(mass).multiply(java.math.BigInteger.valueOf(amount))
        .divide(java.math.BigInteger.valueOf(volume)).longValueExact();
  }
  public Parcel take(long requested) {
    var p = preview(requested);
    water -= p.water(); salt -= p.salt(); sediment -= p.sediment();
    return p;
  }
  public boolean add(Parcel p) {
    if (p.water() > room() || salt + p.salt() > MAX * 1024
        || sediment + p.sediment() > MAX * 1024) return false;
    water += p.water(); salt += p.salt(); sediment += p.sediment(); return true;
  }
  public static long move(WaterAccount from, WaterAccount to, long requested) {
    if (from == to) return 0;
    var p = from.preview(Math.min(Math.max(0, requested), to.room()));
    if (!to.add(p)) return 0;
    from.take(p.water()); return p.water();
  }
  /** Evaporation moves water alone; dissolved/entrained constituents remain in the source. */
  public long evaporateInto(WaterAccount atmosphere, long requested) {
    if (atmosphere == this) return 0;
    long n = Math.min(Math.min(Math.max(0, requested), water), atmosphere.room());
    if (!atmosphere.add(new Parcel(n, 0, 0))) return 0;
    water -= n; return n;
  }
}
