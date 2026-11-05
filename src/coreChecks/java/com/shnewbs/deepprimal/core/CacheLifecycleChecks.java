package com.shnewbs.deepprimal.core;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public final class CacheLifecycleChecks {
  private static int checks;
  private static void require(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
  public static void main(String[] args)throws Exception {
    var cache=new ChunkCache(1,2);
    var entered=new CountDownLatch(1);var release=new CountDownLatch(1);
    var pool=Executors.newFixedThreadPool(3);
    try {
      var load=pool.submit(()->cache.getOrCompute("slow",k->{entered.countDown();try{release.await();}catch(InterruptedException e){throw new RuntimeException(e);}return "old";},4));
      require(entered.await(5,TimeUnit.SECONDS),"loader started");
      var cleared=pool.submit(cache::clear);
      try{cleared.get(2,TimeUnit.SECONDS);}catch(TimeoutException e){throw new AssertionError("clear blocked behind caller computation",e);}
      require(!load.isDone(),"clear must finish while loader remains blocked");
      cache.put("new","kept",4);release.countDown();require(load.get(5,TimeUnit.SECONDS).equals("old"),"caller retains computed value");
      require(cache.get("slow")==null,"pre-clear result must not repopulate cache");
      require(cache.get("new").equals("kept"),"post-clear entry preserved");
      AtomicInteger calls=new AtomicInteger();
      try{cache.getOrCompute("bad",k->{calls.incrementAndGet();return "bad";},-1);throw new AssertionError("negative accepted");}catch(IllegalArgumentException expected){}
      require(calls.get()==0,"invalid weight ran loader");
      try{cache.getOrCompute("fail",k->{throw new IllegalStateException("fixture");},4);throw new AssertionError("failure swallowed");}catch(IllegalStateException expected){}
      require(cache.get("fail")==null,"failed load admitted");
      cache.getOrCompute("new",k->{throw new AssertionError("hit computed");},4);
      var both=new CountDownLatch(2);var go=new CountDownLatch(1);
      java.util.function.Function<Object,String> loader=k->{both.countDown();try{go.await();}catch(InterruptedException e){throw new RuntimeException(e);}return Thread.currentThread().getName();};
      var a=pool.submit(()->cache.getOrCompute("same",loader,8));var b=pool.submit(()->cache.getOrCompute("same",loader,8));
      try{require(both.await(5,TimeUnit.SECONDS),"independent loads execute outside monitor");}finally{go.countDown();}
      require(a.get(5,TimeUnit.SECONDS).equals(b.get(5,TimeUnit.SECONDS)),"duplicate completion returns admitted winner");
      require(cache.size()==2&&cache.getMemoryUsage()==12,"single weighted admission");
      cache.put("third","v",16);require(cache.size()==2&&cache.getMemoryUsage()<=24,"bounded eviction");
      cache.clear();require(cache.size()==0&&cache.getMemoryUsage()==0,"final clear");
      cache.close();cache.clear();
      try{cache.put("late","value",4);throw new AssertionError("closed put accepted");}catch(IllegalStateException expected){}
      try{cache.getOrCompute("late",k->{calls.incrementAndGet();return "value";},4);throw new AssertionError("closed load accepted");}catch(IllegalStateException expected){}
      require(calls.get()==0&&cache.size()==0&&cache.getMemoryUsage()==0,"terminal close rejects late admission and callback");

    }finally{release.countDown();pool.shutdownNow();require(pool.awaitTermination(5,TimeUnit.SECONDS),"workers terminated");}
    System.out.println("CACHE_LIFECYCLE_PASS checks="+checks);
  }
}
