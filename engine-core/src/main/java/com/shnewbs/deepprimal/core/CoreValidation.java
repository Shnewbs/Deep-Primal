package com.shnewbs.deepprimal.core;
import java.util.*;

/** Lifecycle-driven telemetry; uses the server tick instead of a second unowned timer thread. */
public final class CoreValidation {
  private static long intervalNanos,next;
  private static boolean running;
  public static synchronized void startPeriodicValidation(int seconds){if(seconds<1||seconds>86400)throw new IllegalArgumentException("interval");EngineHealthCheck.initDefaultChecks();EngineHealthCheck.runOrThrow();intervalNanos=seconds*1_000_000_000L;next=System.nanoTime();running=true;}
  public static synchronized void tick(){if(!running||System.nanoTime()-next<0)return;next=System.nanoTime()+intervalNanos;EngineMetrics.updateMemoryMetrics();for(var result:EngineHealthCheck.runAll()){EngineMetrics.recordAssertionFailure();System.getLogger(CoreValidation.class.getName()).log(System.Logger.Level.WARNING,result.message());}}
  public static synchronized void stop(){running=false;}
  public static ValidationReport validateSeed(long seed){var e=new TerrainEngine(seed,7);var issues=new ArrayList<String>();for(int x:new int[]{-16,0,16})for(int z:new int[]{-16,0,16}){var s=e.column(x,z);if(!Double.isFinite(s.ground())||Double.isInfinite(s.water()))issues.add("Non-finite column "+x+","+z);}return new ValidationReport(seed,issues.isEmpty(),List.copyOf(issues));}
  public record ValidationReport(long seed,boolean passed,List<String> issues){}
  private CoreValidation(){}
}
