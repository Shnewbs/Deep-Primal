package com.shnewbs.deepprimal.core;
import java.util.*;
/** Thin films on real solid stair beds, across layers and horizontal page boundaries. */
final class WaterBedChecks {
  static void run() {
    long vertices=0;int cases=0;
    for(int direction=0;direction<4;direction++)for(int high=1;high<=16;high++)for(int low=1;low<=16;low++)
      for(int y:new int[]{0,8,15})for(boolean smooth:new boolean[]{false,true}) {
        byte[] u=new byte[WaterPatchMesher.CELLS],s=new byte[u.length];
        int dx=direction==0?1:direction==1?-1:0,dz=direction==2?1:direction==3?-1:0;
        u[WaterPatchMesher.index(8,y,8)]=(byte)high;s[WaterPatchMesher.index(8,y-1,8)]=1;
        u[WaterPatchMesher.index(8+dx,y-1,8+dz)]=(byte)low;
        var input=new WaterPatchMesher.Snapshot(u,s,new int[u.length],new int[256]);
        byte[] before=u.clone();var mesh=WaterPatchMesher.build(input,input,smooth).vertices();
        for(int i=0;i<mesh.length;i+=WaterPatchMesher.STRIDE)if(WaterPatchMesher.face(mesh[i+10])==0&&(int)mesh[i+9]==(8|(8<<4)|(y<<8))) {
          if(mesh[i+3]<=0||mesh[i+4]<=0)throw new AssertionError("Supported film intersects solid bed");vertices++;
        }
        if(!Arrays.equals(before,u))throw new AssertionError("Renderer changes volume");cases++;
      }
    int seams=0;
    for(int amount=1;amount<=16;amount++) {
      var leftEdges=new HashMap<String,Float>();var rightEdges=new HashMap<String,Float>();
      for(int ox:new int[]{0,16}) {
        byte[] u=new byte[WaterPatchMesher.CELLS],s=new byte[u.length];
        for(int z=-1;z<=16;z++)for(int x=-1;x<=16;x++) {
          int high=(z&1)==0?8:7;
          u[WaterPatchMesher.index(x,high,z)]=(byte)amount;
          s[WaterPatchMesher.index(x,high-1,z)]=1;
        }
        var v=WaterPatchMesher.build(new WaterPatchMesher.Snapshot(u,s,new int[u.length],new int[256]),null,true).vertices();
        for(int i=0;i<v.length;i+=WaterPatchMesher.STRIDE)if(WaterPatchMesher.face(v[i+10])==0) {
          int cell=(int)v[i+9],cellX=cell&15,cellZ=(cell>>>4)&15,cellY=cell>>>8;
          float worldX=ox+cellX+v[i+5],worldZ=cellZ+v[i+6];
          if(worldX!=16)continue;
          String key=worldZ+":"+cellY;
          Float old=(ox==0?leftEdges:rightEdges).putIfAbsent(key,v[i+3]);
          if(old!=null&&old!=v[i+3])throw new AssertionError("Shared support differs within a page");
        }
      }
      for(var edge:leftEdges.entrySet()) {
        Float other=rightEdges.get(edge.getKey());
        if(other!=null) {
          if(other.floatValue()!=edge.getValue())
            throw new AssertionError("Shared support differs across pages");
          seams++;
        }
      }
    }
    if(vertices<24000||seams<100)throw new AssertionError("Missing coverage: vertices="+vertices+" seams="+seams);
    System.out.println("WATER_BED_PASS cases="+cases+" supportedVertices="+vertices+" sharedPageVertices="+seams+" volumeUnchanged=true");
  }
}
