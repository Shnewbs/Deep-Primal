package com.shnewbs.deepprimal.core.lod;
import com.shnewbs.deepprimal.core.HorizonMesh;
import java.util.*;
import java.lang.management.ManagementFactory;
public final class ByteAccountingChecks {
  static volatile long sink;
  static long oracle(List<HorizonMesh> list) { long n=0; for(var m:list)n+=m.colors().length*20L+m.indices().length*2L; return n; }
  static class Sequential extends LinkedList<HorizonMesh> {
    Sequential(List<HorizonMesh> x){super(x);}
    public HorizonMesh get(int i){throw new AssertionError("Indexed sequential list");}
  }
  public static void main(String[] args) throws Exception {
    var bean=(com.sun.management.ThreadMXBean)ManagementFactory.getThreadMXBean();
    if(!bean.isThreadAllocatedMemorySupported())throw new AssertionError("No allocation counter");
    bean.setThreadAllocatedMemoryEnabled(true);
    long tid=Thread.currentThread().threadId();
    for(int k=0;k<5;k++){
      var input=LodReductionChecks.scene(k,-64);
      var c=LodMesher.build(input); var b=BaselineLodMesher.build(input);
      if(!LodReductionChecks.signature(List.of(c.fine(),c.medium(),c.coarse())).equals(
          LodReductionChecks.signature(List.of(b.fine(),b.medium(),b.coarse()))))throw new AssertionError("Geometry");
      long expected=oracle(c.fine())+oracle(c.medium())+oracle(c.coarse());
      for(int mode=0;mode<4;mode++){
        List<HorizonMesh> f=switch(mode){case 0->c.fine();case 1->new ArrayList<>(c.fine());case 2->new Sequential(c.fine());default->List.of();};
        var x=new LodMesher.Levels(f,c.medium(),c.coarse());
        if(x.bytes()!=oracle(f)+oracle(c.medium())+oracle(c.coarse()))throw new AssertionError("Accounting");
        if(mode==1){f.clear();if(x.bytes()!=oracle(c.medium())+oracle(c.coarse()))throw new AssertionError("Stale cache");}
      }
      for(int i=0;i<10000;i++){sink=b.bytes();sink=c.bytes();}
      long beforeBytes=0,afterBytes=0;
      for(int pair=0;pair<5;pair++){
        long start=bean.getThreadAllocatedBytes(tid), sum=0;
        for(int i=0;i<20000;i++)sum+=b.bytes();
        beforeBytes+=bean.getThreadAllocatedBytes(tid)-start;sink=sum;
        start=bean.getThreadAllocatedBytes(tid);sum=0;
        for(int i=0;i<20000;i++)sum+=c.bytes();
        afterBytes+=bean.getThreadAllocatedBytes(tid)-start;sink=sum;
      }
      if(c.bytes()!=expected||b.bytes()!=expected)throw new AssertionError("Sum");
      System.out.println("ALLOCATION fixture="+k+" queries=100000 baselineBytes="+beforeBytes+" candidateBytes="+afterBytes+" meshBytes="+expected);
    }
    if(new LodMesher.Levels(List.of(),List.of(),List.of()).bytes()!=0)throw new AssertionError("Empty");
    LodScratchChecks.main(new String[0]);
    System.out.println("BYTE_ACCOUNTING_PASS realMesherFixtures=5 listKinds=4 mutationObserved=true noGeometryChanges=true");
  }
}
