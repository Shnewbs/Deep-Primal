package com.shnewbs.deepprimal.core.organic;

import java.util.Objects;

/**
 * Portable, immutable N/P/K bookkeeping for one authoritative soil patch.
 * Values are nonnegative integer game nutrient units, not kilograms.
 * No calendar, world, irrigation or player state is inferred here.
 *
 * <p>Callers must durably commit the returned ledger AND the counterparty's
 * debit/credit as one accepted transaction. This class is not a persistence,
 * water, plant growth or exactly-once transaction implementation.</p>
 */
public record SoilNutrientLedger(int schemaVersion, long revision, Nutrients mineral, Nutrients detritus) {
  public static final int SCHEMA_VERSION = 1;
  public static final SoilNutrientLedger EMPTY =
      new SoilNutrientLedger(SCHEMA_VERSION, 0, Nutrients.ZERO, Nutrients.ZERO);

  /** Separate immutable N/P/K components; all arithmetic is checked. */
  public record Nutrients(long nitrogen, long phosphorus, long potassium) {
    public static final Nutrients ZERO = new Nutrients(0, 0, 0);

    public Nutrients {
      if (nitrogen < 0 || phosphorus < 0 || potassium < 0)
        throw new IllegalArgumentException("nutrient quantities must be nonnegative");
    }

    public Nutrients plus(Nutrients other) {
      Objects.requireNonNull(other, "other");
      return new Nutrients(Math.addExact(nitrogen, other.nitrogen),
          Math.addExact(phosphorus, other.phosphorus),
          Math.addExact(potassium, other.potassium));
    }

    public Nutrients minus(Nutrients other) {
      Objects.requireNonNull(other, "other");
      if (other.nitrogen > nitrogen || other.phosphorus > phosphorus
          || other.potassium > potassium)
        throw new IllegalArgumentException("insufficient nutrient balance");
      return new Nutrients(nitrogen - other.nitrogen,
          phosphorus - other.phosphorus, potassium - other.potassium);
    }

    public Nutrients limitedTo(Nutrients limit) {
      Objects.requireNonNull(limit, "limit");
      return new Nutrients(Math.min(nitrogen, limit.nitrogen),
          Math.min(phosphorus, limit.phosphorus),
          Math.min(potassium, limit.potassium));
    }

    public boolean isZero() { return nitrogen == 0 && phosphorus == 0 && potassium == 0; }
  }

  /** An accepted withdrawal and the resulting ledger; the recipient owns amount. */
  public record Withdrawal(SoilNutrientLedger ledger, Nutrients amount) {
    public Withdrawal {
      Objects.requireNonNull(ledger, "ledger");
      Objects.requireNonNull(amount, "amount");
    }
  }

  public SoilNutrientLedger {
    if (schemaVersion != SCHEMA_VERSION)
      throw new IllegalArgumentException("unsupported soil nutrient ledger schema");
    if (revision < 0) throw new IllegalArgumentException("negative ledger revision");
    Objects.requireNonNull(mineral, "mineral");
    Objects.requireNonNull(detritus, "detritus");
  }

  /** External mineral fertilizer, weathering or other *already paid* input. */
  public SoilNutrientLedger creditMineral(Nutrients acceptedExternal) {
    Objects.requireNonNull(acceptedExternal, "acceptedExternal");
    if (acceptedExternal.isZero()) return this;
    Nutrients next = mineral.plus(acceptedExternal);
    return new SoilNutrientLedger(schemaVersion, Math.incrementExact(revision), next, detritus);
  }

  /** External litter, manure or returned biomass *already debited* elsewhere. */
  public SoilNutrientLedger creditDetritus(Nutrients acceptedExternal) {
    Objects.requireNonNull(acceptedExternal, "acceptedExternal");
    if (acceptedExternal.isZero()) return this;
    Nutrients next = detritus.plus(acceptedExternal);
    return new SoilNutrientLedger(schemaVersion, Math.incrementExact(revision), mineral, next);
  }

  /** Transfer only existing detrital nutrients to the mineral pool. */
  public SoilNutrientLedger mineralize(Nutrients requested) {
    Objects.requireNonNull(requested, "requested");
    Nutrients moved = detritus.limitedTo(requested);
    if (moved.isZero()) return this;
    Nutrients nextMineral = mineral.plus(moved);
    Nutrients nextDetritus = detritus.minus(moved);
    return new SoilNutrientLedger(schemaVersion, Math.incrementExact(revision), nextMineral, nextDetritus);
  }

  /** Withdraw only the mineral pool's available amount for plant uptake. */
  public Withdrawal uptake(Nutrients requested) { return withdraw(requested); }

  /** Withdraw minerals for an accounted drainage/leaching counterparty. */
  public Withdrawal leach(Nutrients requested) { return withdraw(requested); }

  private Withdrawal withdraw(Nutrients requested) {
    Objects.requireNonNull(requested, "requested");
    Nutrients moved = mineral.limitedTo(requested);
    if (moved.isZero()) return new Withdrawal(this, moved);
    return new Withdrawal(new SoilNutrientLedger(schemaVersion, Math.incrementExact(revision),
        mineral.minus(moved), detritus), moved);
  }
}
