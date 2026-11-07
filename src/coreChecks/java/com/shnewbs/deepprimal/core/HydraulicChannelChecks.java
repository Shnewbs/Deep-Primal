package com.shnewbs.deepprimal.core;

import java.util.*;

public final class HydraulicChannelChecks {
  private static long checks;
  private static void need(boolean b, String m) { checks++; if (!b) throw new AssertionError(m); }
  public static void main(String[] args) {
    for (double grade : new double[] {0,.001,.02,.15,1,4}) {
      double lastRadius=0,lastDepth=0;
      for (int n=0;n<=500;n++) {
        var a=HydraulicChannel.section(18_000*Math.pow(1.02,n),grade,.6);
        need(a.radius()>=lastRadius && a.depth()>=lastDepth,"Catchment geometry regressed");
        need(Double.isFinite(a.speed()) && a.speed()>=.08 && a.speed()<=.85,"Velocity bounds");
        for(int i=-20;i<=20;i++) {
          double x=i/20.;
          double left=HydraulicChannel.bedDepth(a.depth(),x,.6);
          double right=HydraulicChannel.bedDepth(a.depth(),-x,-.6);
          need(Math.abs(left-right)<1e-9,"Mirrored bend section");
          need(left>=.12-1e-9 && left<=a.depth()+1e-9,"Bed escaped section");
        }
        lastRadius=a.radius();lastDepth=a.depth();
      }
    }
    need(HydraulicChannel.bedDepth(8,-.35,1)>HydraulicChannel.bedDepth(8,.35,1),"Outer bend pool missing");
    need(HydraulicChannel.section(1_000_000,.01,0).radius()>
        HydraulicChannel.section(1_000_000,.3,0).radius(),"Steep confinement missing");
    var tangent=HydraulicChannel.tangent(0,32,32,0);
    need(Math.abs(tangent.x()-tangent.z())<1e-12 && tangent.x()>0,"Shared bend bisector");
    var widths=new ArrayList<Double>();int wet=0,changed=0,movedControls=0;double maxDepth=0;
    for(long seed:new long[]{0,1,73,20261020}) {
      var engine=new TerrainEngine(seed,7,TerrainOptions.DEFAULT,11);
      var old=new TerrainEngine(seed,7,TerrainOptions.DEFAULT,10);
      var replay=new TerrainEngine(seed,7,TerrainOptions.DEFAULT,11);
      var r=engine.waterways(0,0);r.verify();
      var line=r.centerline();var before=old.waterways(0,0).centerline();
      need(line.size()==before.size(),"Hydraulics changed terrain receiver count");
      for(int n=0;n<line.size();n++) {
        var p=line.get(n);var b=before.get(n);
        need(p.stage()==b.stage(),"Hydraulics moved routed stage");
        if(n%16==0 || n%16==15)
          need(p.x()==b.x() && p.z()==b.z(),"Hydraulics moved shared drainage knot");
        else if(Math.hypot(p.x()-b.x(),p.z()-b.z())>1e-6)movedControls++;
        widths.add(p.radius()*2);
        if(n%16!=0)continue;
        var s=engine.column(p.x(),p.z());var previous=old.column(p.x(),p.z());
        need(s.wet() && Double.isFinite(s.ground()),"Dry/nonfinite channel center");
        need(s.ground()<=Math.ceil(s.water())-1.001+1e-8,"Lost wet voxel");
        maxDepth=Math.max(maxDepth,s.water()-s.ground());wet++;
        if(Math.abs(s.ground()-previous.ground())>.1)changed++;
        need(s.equals(replay.column(p.x(),p.z())),"Replay");
      }
    }
    Collections.sort(widths);
    need(wet>100 && changed>20,"Missing integrated hydraulic depth witnesses");
    need(movedControls>100,"Smooth node tangents not connected to production paths");
    need(widths.get(0)<5 && widths.get(widths.size()-1)>20 && maxDepth>4,"Missing waterway scale/depth range");
    System.out.println("HYDRAULIC_CHANNEL_PASS checks="+checks+" wet="+wet+" changedBeds="+changed
        +" minWidth="+widths.get(0)+" medianWidth="+widths.get(widths.size()/2)
        +" maxWidth="+widths.get(widths.size()-1)+" maxDepth="+maxDepth+" smoothedSamples="+movedControls);
  }
}
