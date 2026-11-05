import com.shnewbs.deepprimal.core.density.*;
public class CountProbe {
 public static void main(String[]args)throws Exception{
  var graph=TerrainShapeV8.definition(false).compile(20261024L);DensityField field=graph.newSampler();long checksum=0;
  for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=-128;y<640;y++)checksum=checksum*31+Double.doubleToRawLongBits(field.sample(74131+x,y,-71931+z));
  System.out.println("COUNT_PROBE noise3D="+graph.getClass().getField("counted3D").getLong(graph)+" checksum="+checksum);
 }
}
