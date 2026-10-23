package com.shnewbs.deepprimal.core;
final class WaterFlowBlendChecks {
  static void run() {
    var early=new WaterFlowTransition(0,0);early.target(4,25,1);
    if(early.source()!=0||early.target()!=4||!early.active(25)||early.age(25)!=0)throw new AssertionError("early idle retarget");
    int cases=0;
    for(int a=0;a<17;a++)for(int b=0;b<17;b++)if(a!=b) {
      var t=new WaterFlowTransition(a,0);t.target(b,100,1);
      if(t.source()!=a||t.target()!=b||!t.active(100))throw new AssertionError("initial blend");
      t.target(b,125,2);if(t.age(125)!=25)throw new AssertionError("reupload restarted blend");
      int c=(b+1)%17;t.target(c,150,3);
      if(t.target()!=b||!t.queued(150)||t.age(150)!=50)throw new AssertionError("rapid request disrupted blend");
      t.target(a,175,0);if(t.target()!=b||!t.queued(175))throw new AssertionError("stale revision admitted");
      t.target(c,210,4);if(t.source()!=b||t.target()!=c||t.age(210)!=0)throw new AssertionError("queued endpoint");
      t.target(16,250,5);
      if(!t.active(250)||t.target()!=c||t.queued(250)!=(c!=16))throw new AssertionError("material disrupted blend");
      t.target(16,310,6);
      if(t.target()!=16||t.active(310)!=(c!=16))throw new AssertionError("material queued endpoint");
      cases++;
    }
    System.out.println("WATER_FLOW_BLEND_CORE_PASS pairs="+cases+" earlyFirstRetarget=true repeatedUpload=true queuedLatest=true staleGuard=true finiteTwoEndpoints=true stillSwitchBlended=true");
  }
}
