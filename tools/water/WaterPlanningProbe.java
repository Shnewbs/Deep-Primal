import com.shnewbs.deepprimal.core.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleBinaryOperator;

/** Real terrain inputs; counts expensive height queries, not FPS or a named-hardware benchmark. */
public final class WaterPlanningProbe {
  public static void main(String[] args) {
    long legacy=0,hydraulic=0;int reaches=0;
    for(long seed:new long[]{0,1,73,20261020}) {
      var terrain=new TerrainEngine(seed,7,TerrainOptions.DEFAULT,10);
      int expected=-1;
      for(boolean upgraded:new boolean[]{false,true}) {
        var calls=new AtomicLong();
        DoubleBinaryOperator height=(x,z)->{calls.incrementAndGet();return terrain.raw(x,z);};
        var region=new WatershedRegion(-4096,-4096,height,terrain::rainfall,true,true,
            terrain.noise,false,false,false,null,null,true,false,false,true,
            WaterTerrainConfig.DEFAULTS,upgraded);
        region.verify();
        if(!upgraded){legacy+=calls.get();expected=region.statistics().reaches();reaches+=expected;}
        else {
          hydraulic+=calls.get();
          if(region.statistics().reaches()!=expected)throw new AssertionError("Planning dropped drainage reaches");
        }
      }
    }
    if(hydraulic>=legacy)throw new AssertionError("No terrain-query reduction");
    System.out.printf(java.util.Locale.ROOT,
        "WATER_PLANNING_PASS seeds=4 reaches=%d revision10HeightCalls=%d revision11HeightCalls=%d reduction=%.2f%% fpsClaim=false%n",
        reaches,legacy,hydraulic,100.0*(legacy-hydraulic)/legacy);
  }
}
