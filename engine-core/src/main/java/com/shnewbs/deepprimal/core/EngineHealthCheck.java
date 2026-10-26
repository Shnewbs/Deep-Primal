package com.shnewbs.deepprimal.core;
import java.util.*;
import java.util.function.Supplier;

/** Explicit invariant checks. No heavy terrain sweeps or hard-coded machine budgets at startup. */
public final class EngineHealthCheck {
  private static final Map<String,Supplier<CheckResult>> checks=new LinkedHashMap<>();
  private static boolean enabled=true;
  public static synchronized void register(String name,Supplier<CheckResult> check){checks.put(Objects.requireNonNull(name),Objects.requireNonNull(check));}
  public static synchronized void setEnabled(boolean value){enabled=value;}public static synchronized boolean isEnabled(){return enabled;}
  public static List<CheckResult> runAll(){Map<String,Supplier<CheckResult>> copy;synchronized(EngineHealthCheck.class){if(!enabled)return List.of();copy=new LinkedHashMap<>(checks);}
    var failures=new ArrayList<CheckResult>();copy.forEach((name,check)->{try{var result=check.get();if(!result.passed())failures.add(result);}catch(Exception e){failures.add(new CheckResult(name,false,e.toString(),e));}});return List.copyOf(failures);}
  public static void runOrThrow(){var failures=runAll();if(!failures.isEmpty())throw new IllegalStateException("Engine invariants: "+failures);}
  public static synchronized void initDefaultChecks(){
    checks.putIfAbsent("water_conservation",()->{var e=WaterTransport.exchange(16,0,false);return e.from()+e.to()==16?CheckResult.pass("water_conservation"):CheckResult.fail("water_conservation","volume changed");});
    checks.putIfAbsent("cache_bounds",()->{var cache=new ChunkCache(1,1);cache.put(1,"first",1);cache.put(2,"second",1);return cache.size()==1&&cache.getMemoryUsage()==1?CheckResult.pass("cache_bounds"):CheckResult.fail("cache_bounds","cache exceeded budget");});
  }
  public record CheckResult(String name,boolean passed,String message,Exception exception){public static CheckResult pass(String name){return new CheckResult(name,true,"OK",null);}public static CheckResult fail(String name,String message){return new CheckResult(name,false,message,null);}}
  private EngineHealthCheck(){}
}
