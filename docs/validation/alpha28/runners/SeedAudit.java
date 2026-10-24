import com.shnewbs.deepprimal.core.*;
import java.util.*;
public class SeedAudit {
 public static void main(String[] args) {
  int revision=args.length>0?Integer.parseInt(args[0]):6;
  System.out.println("seed,revision,originRaw,originRain,originTemp,originBiome,land,plains,forestFamily,treeCandidates,treeEligible,widthP10,widthP50,widthP90,maxWidth,digest");
  for(long seed:new long[]{0,1,2,3,73,20261020,8675309,-987654321}) {
   var t=new TerrainEngine(seed,revision);long digest=1;int land=0,plain=0,forest=0,trees=0,eligible=0;
   for(int z=-512;z<=512;z+=32)for(int x=-512;x<=512;x+=32){var s=t.sample(x,z);int b=t.biome(x,z);digest=31*digest+Double.doubleToLongBits(s.ground());if(!s.wet()){land++;if(b==0||b==21)plain++;if(Set.of(1,4,10,11,12,13,14,15,16,22,24,28).contains(b))forest++;}}
   for(int gz=-32;gz<=32;gz++)for(int gx=-32;gx<=32;gx++){
    int x=gx*8+1+(int)(t.noise.hash(gx,gz,311)*6),z=gz*8+1+(int)(t.noise.hash(gx,gz,312)*6);var s=t.sample(x,z);
    if(s.wet()||s.ground()<101||s.slope()>(revision>=7?1.5:.8)||s.temperature()<.1||s.rain()<.38||t.noise.hash(gx,gz,313)>(revision>=7?Vegetation.treeChance(t.biome(x,z),t.forest(x,z)):Math.max(0,t.forest(x,z)-.2)*.66))continue;
    trees++;int radius=revision>=7?1:3+(int)(t.noise.hash(gx,gz,315)*2);boolean supported=true;
    for(int dz=-radius;dz<=radius&&supported;dz++)for(int dx=-radius;dx<=radius;dx++){var n=t.column(x+dx,z+dz);if(n.sediment()>.85||n.wet()||Math.abs(n.ground()-s.ground())>2){supported=false;break;}}
    if(supported)eligible++;
   }
   var widths=t.waterways(0,0).centerline().stream().mapToDouble(p->p.radius()*2).sorted().toArray();var o=t.sample(0,0);
   System.out.printf(Locale.ROOT,"%d,%d,%.5f,%.5f,%.5f,%d,%d,%d,%d,%d,%d,%.3f,%.3f,%.3f,%.3f,%s%n",seed,revision,t.raw(0,0),o.rain(),o.temperature(),t.biome(0,0),land,plain,forest,trees,eligible,widths[widths.length/10],widths[widths.length/2],widths[widths.length*9/10],widths[widths.length-1],Long.toHexString(digest));
  }
 }
}
