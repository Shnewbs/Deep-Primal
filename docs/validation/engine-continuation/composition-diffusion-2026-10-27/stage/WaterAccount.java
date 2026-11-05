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

  /** Signed constituent amounts moved from this account to the other account. */
  public record Diffusion(long saltToOther, long sedimentToOther) {}

  /**
   * Bounded composition-only contact. Water volumes never change. Callers must own both
   * accounts on the server thread and establish actual physical connectivity first.
   * Dry deposits are retained until rewetted. Limits are constituent mass per call,
   * not time advancement; unloaded catch-up and scheduling remain the caller's policy.
   */
  public Diffusion diffuseWith(WaterAccount other, long saltLimit, long sedimentLimit) {
    java.util.Objects.requireNonNull(other);
    if (saltLimit < 0 || sedimentLimit < 0)
      throw new IllegalArgumentException("Negative diffusion limit");
    if (other == this || water == 0 || other.water == 0) return new Diffusion(0, 0);
    long ds = diffusion(salt, other.salt, water, other.water, saltLimit);
    long dd = diffusion(sediment, other.sediment, water, other.water, sedimentLimit);
    salt -= ds; other.salt += ds;
    sediment -= dd; other.sediment += dd;
    return new Diffusion(ds, dd);
  }

  private static long diffusion(long a, long b, long va, long vb, long limit) {
    // Cross products can exceed long even for valid parcels. Truncate the signed
    // excess toward zero so reversing contact order cannot shuttle rounding dust.
    var excess = java.math.BigInteger.valueOf(a).multiply(java.math.BigInteger.valueOf(vb))
        .subtract(java.math.BigInteger.valueOf(b).multiply(java.math.BigInteger.valueOf(va)));
    long desired = excess.divide(java.math.BigInteger.valueOf(va + vb)).longValueExact();
    if (desired > 0) return Math.min(desired, Math.min(limit, Math.min(a, MAX * 1024 - b)));
    return -Math.min(-desired, Math.min(limit, Math.min(b, MAX * 1024 - a)));
  }

  /** Evaporation moves water alone; dissolved/entrained constituents remain in the source. */
  public long evaporateInto(WaterAccount atmosphere, long requested) {
    if (atmosphere == this) return 0;
    long n = Math.min(Math.min(Math.max(0, requested), water), atmosphere.room());
    if (!atmosphere.add(new Parcel(n, 0, 0))) return 0;
    water -= n; return n;
  }
}

