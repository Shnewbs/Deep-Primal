package com.shnewbs.deepprimal.core;
import java.util.List;
public final class WatershedRegion {
  private WatershedRegion() {}
  public static final int STEP = 32, SIDE=TerrainEngine.CELL/STEP+1;
  public record BoundaryNode(int x,int z,double ground,double floodedElevation,
      double plannedStage,double catchmentArea,boolean ocean,boolean lake,Long downstreamKey) {}
  public record BoundarySnapshot(int originX,int originZ,List<BoundaryNode> nodes) {}
  public record RoutingSnapshot(BoundarySnapshot boundary,
      List<WatershedInteriorPortRoutes.Route> routes) {}

}
