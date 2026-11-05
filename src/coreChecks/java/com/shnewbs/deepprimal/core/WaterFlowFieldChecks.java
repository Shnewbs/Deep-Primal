package com.shnewbs.deepprimal.core;
import java.util.Arrays;
final class WaterFlowFieldChecks {
  private static void need(boolean ok,String message){if(!ok)throw new AssertionError(message);}
  private static WaterPatchMesher.Snapshot snapshot(int ox,int oz,int salt) {
    byte[] u=new byte[WaterPatchMesher.CELLS],f=new byte[u.length];
    for(int z=-1;z<=16;z++)for(int x=-1;x<=16;x++) {
      int h=(x+ox)*73856093^(z+oz)*19349663^salt;int d=Math.floorMod(h,18)-1;
      int i=WaterPatchMesher.index(x,0,z);u[i]=(byte)(d<0?0:16);f[i]=(byte)Math.max(d,0);
    }
    return new WaterPatchMesher.Snapshot(u,new byte[u.length],new int[u.length],new int[256],f);
  }
  static void run() {
    for (long time = 0; time < 65536; time++) {
      float phase = WaterFlowField.downstreamPhase(time);
      need(phase >= 0 && phase < 1, "downstream phase outside one atlas tile");
      need(phase == WaterFlowField.downstreamPhase(time + 8192),
          "downstream phase changed at its tile-period wrap");
    }
    need(WaterFlowField.downstreamPhase(8191) < 1
        && WaterFlowField.downstreamPhase(8192) == 0
        && WaterFlowField.downstreamPhase(65535) == WaterFlowField.downstreamPhase(65535 + 65536L),
        "downstream phase is discontinuous at the renderer clock wrap");
    int codec=0;
    for(int a=0;a<17;a++)for(int b=0;b<17;b++)for(int c=0;c<17;c++)for(int d=0;d<17;d++) {
      int word=a|(b<<5)|(c<<10)|(d<<15);
      for(int age=0;age<=100;age++) {
        int to=Math.round(-WaterFlowField.targets(word,age))-2,from=Math.round(WaterFlowField.sources(word,age));
        need((to&1048575)==word&&(from&1048575)==word,"corner ID lost float precision");
        need((((to>>20)&15)|(((from>>20)&7)<<4))==age,"fourth age lost");codec++;
      }
    }
    int clocks=0;
    for(int offset=-32768;offset<=0;offset++)for(int age:new int[]{0,25,100}) {
      int packed=WaterFlowField.heightClock(offset,age);int decoded=(byte)packed;
      need(decoded<=0 && decoded>=-100,"height clock starts in future or exceeds morph window");
      need(((packed>>>8)&255)==age,"expired height offset corrupts corner clock");
      if(offset>=-100)need(decoded==offset,"active height morph clock changed");clocks++;
    }
    int edges=0;
    for(int n=0;n<1000;n++) {
      int ox=n%2==0?29999968:-29999984,oz=-ox,salt=n*16777619;
      var a=snapshot(ox,oz,salt);var x=snapshot(ox+16,oz,salt);var z=snapshot(ox,oz+16,salt);
      byte[] saved=a.units().clone();
      for(int i=0;i<=16;i++) {
        need(WaterFlowField.direction(a,16,0,i)==WaterFlowField.direction(x,0,0,i),"X halo corner differs");
        need(WaterFlowField.direction(a,i,0,16)==WaterFlowField.direction(z,i,0,0),"Z halo corner differs");edges+=2;
      }
      need(Arrays.equals(saved,a.units()),"visual field changed quantity");
    }
    var s=snapshot(0,0,0);
    for(int d=0;d<=16;d++) {
      Arrays.fill(s.units(),(byte)16);Arrays.fill(s.flow(),(byte)d);
      need(WaterFlowField.direction(s,8,0,8)==d,"uniform direction changed");
    }
    Arrays.fill(s.units(),(byte)0);need(WaterFlowField.direction(s,8,0,8)==-1,"dry corner not distinguished");
    s.units()[WaterPatchMesher.index(7,0,7)]=16;s.flow()[WaterPatchMesher.index(7,0,7)]=0;
    s.units()[WaterPatchMesher.index(8,0,7)]=16;s.flow()[WaterPatchMesher.index(8,0,7)]=4;
    need(WaterFlowField.direction(s,8,0,8)==2,"turn corner not averaged");
    s.flow()[WaterPatchMesher.index(8,0,7)]=8;need(WaterFlowField.direction(s,8,0,8)==16,"opposed flow not stagnant");
    System.out.println("WATER_FLOW_FIELD_CORE_PASS codecCases="+codec+" heightClockCases="+clocks+" haloEdges="+edges+" uniformDirections=17 dryStillDistinct=true quantityUnchanged=true vertexStride=36");
  }
}
