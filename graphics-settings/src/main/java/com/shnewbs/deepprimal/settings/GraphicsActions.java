package com.shnewbs.deepprimal.settings;

/** Host-provided actions keep settings independent of diagnostics/server implementation classes. */
public final class GraphicsActions {
  public static Runnable dashboard = () -> {};
  public static java.util.function.BooleanSupplier dashboardAvailable = () -> false;

  private GraphicsActions() {}
}
