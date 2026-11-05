import com.shnewbs.deepprimal.core.*;
import java.util.*;
public class WaterBedProbe {
 public static void main(String[] args){
  byte[] water=new byte[WaterPatchMesher.CELLS],solid=new byte[water.length];
  // Thin moving water on the higher stair, partial water below the next stair.
  water[WaterPatchMesher.index(8,8,8)]=1;
  solid[WaterPatchMesher.index(8,7,8)]=1;
  water[WaterPatchMesher.index(9,7,8)]=1;
  solid[WaterPatchMesher.index(9,6,8)]=1;
  var s=new WaterPatchMesher.Snapshot(water,solid,new int[water.length],new int[256]);
  var m=WaterPatchMesher.build(s,null,true);int clipped=0,total=0;float min=100;
  for(int i=0;i<m.vertices().length;i+=WaterPatchMesher.STRIDE){var v=m.vertices();if(WaterPatchMesher.face(v[i+10])==0&&(int)v[i+9]==(8|(8<<4)|(8<<8))){total++;min=Math.min(min,v[i+3]);if(v[i+3]<0)clipped++;}}
  System.out.println("supportedFilmVertices="+total+" belowSolidBed="+clipped+" minimumRelativeHeight="+min);
  if(args.length>0&&clipped>0)throw new AssertionError("Water top intersects the solid riverbed");
 }
}
