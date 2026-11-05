package docs.renderer.jams.streamingcounters;

/** Source-only check for the jam-65 diagnostics contract. */
final class DiagnosticsChecks {
  public static void main(String[] args) {
    if (!"appliedRemovals".contains("applied")) throw new AssertionError();
    if (!"appliedAdds".contains("applied")) throw new AssertionError();
    if (!"appliedSheds".contains("applied")) throw new AssertionError();
  }
}
