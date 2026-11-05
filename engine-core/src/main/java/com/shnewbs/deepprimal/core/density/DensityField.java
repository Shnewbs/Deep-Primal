package com.shnewbs.deepprimal.core.density;

/** Pure world-coordinate field. Implementations supplied to parallel jobs must be thread-safe. */
@FunctionalInterface
public interface DensityField {
  double sample(double x, double y, double z);
}
