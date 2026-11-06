package com.shnewbs.deepprimal.core;

import java.nio.file.*;

/** Exact generated geometry export for inspection, rather than an illustrative mockup. */
public final class OrganicDrainageCapture {
  public static void main(String[] args) throws Exception {
    Path out=Path.of(args[0]); Files.createDirectories(out);
    for (long seed:new long[] {0,20261020}) for (int revision:new int[] {9,10}) {
      var engine=new TerrainEngine(seed,7,TerrainOptions.DEFAULT,revision);
      var region=engine.waterways(0,0);
      var center=region.centerline();
      // Focus on the deepest planned lake to inspect its actual surrounding catchment.
      var lake=region.lakeProbes().stream().max(java.util.Comparator.comparingDouble(WatershedRegion.LakeProbe::area));
      double cx=lake.map(WatershedRegion.LakeProbe::x).orElse(0.),cz=lake.map(WatershedRegion.LakeProbe::z).orElse(0.);
      // Use the same revision-9 lake center in both captures.
      var previous=new TerrainEngine(seed,7,TerrainOptions.DEFAULT,9).waterways(0,0);
      var anchor=previous.lakeProbes().stream().max(java.util.Comparator.comparingDouble(WatershedRegion.LakeProbe::area));
      if (anchor.isPresent()) { cx=anchor.get().x();cz=anchor.get().z(); }
      StringBuilder grid=new StringBuilder("x,z,raw,bed,water,speed\n");
      for (int dz=-768;dz<=768;dz+=4) for (int dx=-768;dx<=768;dx+=4) {
        double x=cx+dx,z=cz+dz,raw=engine.raw(x,z);
        var s=engine.column(x,z);
        grid.append(x).append(',').append(z).append(',').append(raw).append(',').append(s.ground())
            .append(',').append(s.water()).append(',').append(s.speed()).append('\n');
      }
      Files.writeString(out.resolve(seed+"-r"+revision+"-surface.csv"),grid);
      StringBuilder paths=new StringBuilder("ax,az,bx,bz,sa,sb\n");
      for (int n=0;n+1<center.size();n+=2) {
        var a=center.get(n); var b=center.get(n+1);
        paths.append(a.x()).append(',').append(a.z()).append(',').append(b.x()).append(',').append(b.z())
            .append(',').append(a.stage()).append(',').append(b.stage()).append('\n');
      }
      Files.writeString(out.resolve(seed+"-r"+revision+"-paths.csv"),paths);
      System.out.println("CAPTURE seed="+seed+" revision="+revision+" center="+cx+","+cz+" "+region.statistics());
    }
  }
}
