package com.shnewbs.deepprimal.core;
/** Main-mod-owned bootstrap; no platform imports and no duplicate mod registration. */
public final class EngineBootstrap {
  private static boolean initialized;
  public static synchronized void initialize(){if(initialized)return;CoreValidation.startPeriodicValidation(300);initialized=true;}
  public static synchronized void shutdown(){CoreValidation.stop();initialized=false;}
  public static synchronized boolean isInitialized(){return initialized;}
  private EngineBootstrap(){}
}
