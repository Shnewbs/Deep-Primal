package com.shnewbs.deepprimal.core;

import java.lang.management.ManagementFactory;
import java.util.HashSet;
import java.util.Random;

public final class WaterContactBudgetChecks {
  private record Pair(long low, long high) {
    static Pair of(long a, long b) { return new Pair(Math.min(a,b), Math.max(a,b)); }
  }
  private static long assertions;
  private static long sink;
  private static void check(boolean value, String message) {
    assertions++;
    if (!value) throw new AssertionError(message);
  }
  private static void differential() {
    Random random = new Random(2800);
    for (int limit : new int[]{1,2,3,7,31,2048}) {
      var budget = new WaterContactBudget(limit);
      var expected = new HashSet<Pair>();
      for (int tick=0; tick<300; tick++) {
        long now = tick%4 == 0 ? Long.MAX_VALUE-tick : tick%4 == 1 ? Long.MIN_VALUE+tick : -tick;
        budget.beginTick(now); expected.clear();
        check(budget.visits()==0 && budget.retainedContacts()==0,"new epoch empty");
        for(int n=0;n<limit*3;n++) {
          check(budget.tryVisit()==(n<limit),"admission cap");
          long a=random.nextLong(), b=random.nextLong();
          if(n%3==0){a=0;b=Long.MIN_VALUE;}
          if(n%5==0){a=Long.MAX_VALUE;b=Long.MIN_VALUE;}
          Pair key=Pair.of(a,b);
          boolean accept=expected.size()<limit && expected.add(key);
          check(budget.firstContact(a,b)==accept,"exact pair set");
          check(!budget.firstContact(b,a),"reciprocal pair rejected");
          budget.beginTick(now);
          check(budget.visits()==Math.min(n+1,limit),"same tick preserves visits");
          check(budget.retainedContacts()==expected.size(),"same tick preserves contacts");
        }
      }
    }
  }
  private static void collisionAndRollover() throws Exception {
    // Manufacture identical starting buckets, then compare exact identities, including reverse order.
    var budget=new WaterContactBudget(128);budget.beginTick(0);
    var mix=WaterContactBudget.class.getDeclaredMethod("mix",long.class);mix.setAccessible(true);
    long[] values=new long[128];int found=0;
    for(long value=1;found<values.length;value++) {
      long inner=(long)mix.invoke(null,value);
      long hash=(long)mix.invoke(null,Long.rotateLeft(inner,29));
      if((hash&255)==255)values[found++]=value;
    }
    for(long value:values)check(budget.firstContact(0,value),"colliding distinct pair");
    for(long value:values)check(!budget.firstContact(value,0),"colliding reciprocal");
    check(!budget.firstContact(-1,-2)&&budget.retainedContacts()==128,"full table fails closed");
    budget.beginTick(Long.MIN_VALUE);
    for(long value:values)check(budget.firstContact(0,value),"epoch invalidates collision chain");
    // Exercise signed wrap and actual reuse of the reserved empty stamp without 2^32 ticks.
    var generation=WaterContactBudget.class.getDeclaredField("generation");generation.setAccessible(true);
    generation.setInt(budget,Integer.MAX_VALUE);budget.beginTick(Long.MAX_VALUE);
    check(budget.firstContact(-1,0),"signed generation wrap");
    generation.setInt(budget,-1);budget.beginTick(0);
    check(generation.getInt(budget)==1,"zero generation skipped");
    for(long value:values)check(budget.firstContact(0,value),"ancient generation-one entries cleared");
    var other=new WaterContactBudget(128);other.beginTick(0);
    check(other.firstContact(0,values[0]),"independent owner");
    var wrap=new WaterContactBudget(8);wrap.beginTick(0);
    check(wrap.firstContact(123,456),"ancient epoch insertion");
    generation.setInt(wrap,-1);wrap.beginTick(1);
    check(wrap.firstContact(123,456),"ancient identical pair admitted after stamp reuse");
  }
  private static void lifecycle() {
    for(int invalid:new int[]{Integer.MIN_VALUE,-1,0,65537,Integer.MAX_VALUE}) {
      try{new WaterContactBudget(invalid);throw new AssertionError("invalid capacity accepted");}
      catch(IllegalArgumentException expected){assertions++;}
    }
    var budget=new WaterContactBudget(1);
    try{budget.tryVisit();throw new AssertionError("uninitialized visit");}
    catch(IllegalStateException expected){assertions++;}
    try{budget.firstContact(0,0);throw new AssertionError("uninitialized pair");}
    catch(IllegalStateException expected){assertions++;}
    budget.beginTick(0);check(budget.tryVisit(),"first zero tick");
    check(budget.firstContact(0,0),"self pair exact identity");
    check(!budget.firstContact(0,0),"self pair once");
    var max=new WaterContactBudget(65536);max.beginTick(1);
    for(int n=0;n<65536;n++)check(max.tryVisit()&&max.firstContact(n,~(long)n),"maximum cap");
    check(!max.tryVisit()&&!max.firstContact(1,2),"maximum exhausted");
  }
  private static void workload(WaterContactBudget budget,long start,int ticks) {
    long total=0;
    for(int tick=0;tick<ticks;tick++) {
      budget.beginTick(start+tick);
      for(int n=0;n<2048;n++) {
        if(budget.tryVisit() && budget.firstContact(n,n+1))total++;
      }
    }
    sink=total;
  }
  private static void allocation() {
    var bean=(com.sun.management.ThreadMXBean)ManagementFactory.getThreadMXBean();
    if(!bean.isThreadAllocatedMemorySupported())throw new AssertionError("allocation counter unavailable");
    bean.setThreadAllocatedMemoryEnabled(true);
    var budget=new WaterContactBudget(2048);
    workload(budget,0,2000);
    long id=Thread.currentThread().threadId();
    long before=bean.getThreadAllocatedBytes(id);
    workload(budget,2000,2000);
    long bytes=bean.getThreadAllocatedBytes(id)-before;
    check(sink==4096000,"measured work not eliminated");
    check(bytes==0,"helper allocation after warmup: "+bytes);
    System.out.println("HELPER_ALLOCATION_PASS measured_visits=4096000 bytes="+bytes+" constructor_excluded=true adapter_excluded=true latency_not_measured=true");
  }
  public static void main(String[] args) throws Exception {
    lifecycle();differential();collisionAndRollover();allocation();
    System.out.println("CONTACT_BUDGET_PASS assertions="+assertions+" exact_identity=true fixed_arrays=true");
  }
}
