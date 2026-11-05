package com.shnewbs.deepprimal.engine;
import com.shnewbs.deepprimal.core.biome.EcotoneSurfaceField;
public final class V8EcotoneColumnPlan {
  private final Column[] columns;
  public record Column(EcotoneSurfaceField.Sample cover,int soilDepth,int topY) {}
  public V8EcotoneColumnPlan(Column[] columns){this.columns=columns.clone();}
  public Column column(int x,int z){return columns[z*16+x];}
}
