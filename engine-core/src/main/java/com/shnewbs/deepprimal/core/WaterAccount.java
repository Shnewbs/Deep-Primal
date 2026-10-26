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
    if (!canAccept(p)) return false;
    water += p.water(); salt += p.salt(); sediment += p.sediment(); return true;
  }
  /** Read-only admission check; callers must retain account ownership until committing. */
  public boolean canAccept(Parcel p) {
    return p.water() <= room() && salt + p.salt() <= MAX * 1024
        && sediment + p.sediment() <= MAX * 1024;
  }
  public static long move(WaterAccount from, WaterAccount to, long requested) {
    if (from == to) return 0;
    var p = from.preview(Math.min(Math.max(0, requested), to.room()));
    if (!to.add(p)) return 0;
    from.take(p.water()); return p.water();
  }
  /** Split a finite supply; the second store receives rounding dust.
   * A receiver admission failure returns zero without mutating any of the three accounts. */
  public static long distribute(WaterAccount from, WaterAccount first, WaterAccount second,
      long firstDemand, long secondDemand) {
    if (from == first || from == second || first == second)
      throw new IllegalArgumentException("Split transfer requires distinct water accounts");
    long a = Math.min(Math.max(0, firstDemand), first.room());
    long b = Math.min(Math.max(0, secondDemand), second.room());
    long total = Math.min(from.water(), a + b);
    if (total == 0) return 0;
    long firstWater = portion(total, a, a + b);
    var parcel = from.preview(total);
    var firstParcel = new Parcel(firstWater, portion(parcel.salt(), firstWater, total),
        portion(parcel.sediment(), firstWater, total));
    var secondParcel = new Parcel(total - firstWater, parcel.salt() - firstParcel.salt(),
        parcel.sediment() - firstParcel.sediment());
    if (!first.canAccept(firstParcel) || !second.canAccept(secondParcel))
      return 0;
    first.add(firstParcel);
    second.add(secondParcel);
    from.take(total);
    return total;
  }
  /** Evaporation moves water alone; dissolved/entrained constituents remain in the source. */
  public long evaporateInto(WaterAccount atmosphere, long requested) {
    if (atmosphere == this) return 0;
    long n = Math.min(Math.min(Math.max(0, requested), water), atmosphere.room());
    if (!atmosphere.add(new Parcel(n, 0, 0))) return 0;
    water -= n; return n;
  }
}
