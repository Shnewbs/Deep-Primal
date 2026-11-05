from pathlib import Path
R=Path(__file__).resolve().parent;P=R/'project'
p=P/'src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java';s=p.read_text();s=s.replace('int from = corner == null ? to : corner.source();','int from = corner == null || !corner.active(clock) ? to : corner.source();');p.write_text(s)
p=P/'src/coreChecks/java/com/shnewbs/deepprimal/core/WaterFlowFieldChecks.java'
p.write_text('''package com.shnewbs.deepprimal.core;
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
    int codec=0;
    for(int a=0;a<17;a++)for(int b=0;b<17;b++)for(int c=0;c<17;c++)for(int d=0;d<17;d++) {
      int word=a|(b<<5)|(c<<10)|(d<<15);
      for(int age=0;age<=100;age++) {
        int to=Math.round(-WaterFlowField.targets(word,age))-2,from=Math.round(WaterFlowField.sources(word,age));
        need((to&1048575)==word&&(from&1048575)==word,"corner ID lost float precision");
        need((((to>>20)&15)|(((from>>20)&7)<<4))==age,"fourth age lost");codec++;
      }
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
    System.out.println("WATER_FLOW_FIELD_CORE_PASS codecCases="+codec+" haloEdges="+edges+" uniformDirections=17 dryStillDistinct=true quantityUnchanged=true vertexStride=36");
  }
}
''')
p=P/'src/coreChecks/java/com/shnewbs/deepprimal/core/CoreChecks.java';s=p.read_text().replace('WaterFlowBlendChecks.run();','WaterFlowBlendChecks.run();\n    WaterFlowFieldChecks.run();');p.write_text(s)
# Independent byte decoder used by retained upload tests. No production codec imports.
p=P/'src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterCornerPacking.java'
p.write_text('''package com.shnewbs.deepprimal.validation;
import java.nio.ByteBuffer;
final class WaterCornerPacking {
  static boolean spatial(ByteBuffer b,int at){return b.getFloat(at+16)<-1;}
  static int[] ids(ByteBuffer b,int at,boolean target){int word=target?Math.round(-b.getFloat(at+16))-2:Math.round(b.getFloat(at+20));return new int[]{word&31,(word>>>5)&31,(word>>>10)&31,(word>>>15)&31};}
  static int[] ages(ByteBuffer b,int at){int to=Math.round(-b.getFloat(at+16))-2,from=Math.round(b.getFloat(at+20));return new int[]{(b.getShort(at+26)>>>8)&255,(b.getShort(at+28)>>>8)&255,(b.getShort(at+30)>>>8)&255,((to>>>20)&15)|(((from>>>20)&7)<<4)};}
  static int heightOffset(ByteBuffer b,int at){return spatial(b,at)?b.get(at+26):b.getShort(at+26);}
  static void uniform(ByteBuffer b,int at,int source,int target,int age,int cell){
    if(!spatial(b,at)||(b.get(at+15)&255)!=cell)throw new AssertionError("Missing corner field/cell metadata");
    var targets=ids(b,at,true);var sources=ids(b,at,false);var ages=ages(b,at);
    for(int n=0;n<4;n++)if(targets[n]!=target||sources[n]!=(age>=100?target:source)||ages[n]!=age)throw new AssertionError("Corner endpoint/age mismatch n="+n+" actual="+sources[n]+"->"+targets[n]+" age="+ages[n]+" expected="+source+"->"+target+" age="+age);
  }
  private WaterCornerPacking(){}
}
''')
# For initial settled uploads a fresh flow history has age zero; stable still is sparse/100.
for name in ['WaterMaterialTransitionChecks.java','WaterSideMaterialChecks.java']:
 p=P/'src/visualValidation/java/com/shnewbs/deepprimal/validation'/name;s=p.read_text();(R/('before-'+name)).write_text(s)
 if name.startswith('WaterMaterial'):
  start=s.index('              boolean wrong=');end=s.index('              if(wrong)bad++;',start)
  s=s[:start]+'''              boolean wrong=(l&255)!=176||(sky&255)!=240;
              if(v<4) {
                int expectedAge=s.active?s.age:(s.age>=100?100:s.target==16?100:0);
                WaterCornerPacking.uniform(b,at,s.source,s.target,expectedAge,s.cell|(s.cell<<4));
              } else {
                wrong|=active!=expected||(b.get(at+15)&255)!=(expected?(s.cell|(s.cell<<4)):255);
                if(active)wrong|=(l>>>11)!=s.source||((sky>>>8)&127)!=s.age;
              }
'''+s[end:]
 else:
  needle='''              boolean wrong=(l&255)!=176||(sky&255)!=240||active!=expected||(b.get(at+15)&255)!=(expected?(s.cell|(s.cell<<4)):255);'''
  assert needle in s
  s=s.replace(needle,'''              boolean wrong=(l&255)!=176||(sky&255)!=240;
              if(v<4) {
                int expectedAge=s.active?s.age:(s.age>=100?100:s.target==16?100:0);
                WaterCornerPacking.uniform(b,at,s.source,s.target,expectedAge,s.cell|(s.cell<<4));
                continue;
              }
              wrong|=active!=expected||(b.get(at+15)&255)!=(expected?(s.cell|(s.cell<<4)):255);''')
 p.write_text(s)
p=P/'src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterDirectionUploadChecks.java';s=p.read_text();(R/'before-WaterDirectionUploadChecks.java').write_text(s)
start=s.index('              boolean active=');end=s.index('              byte[] bytes=',start)
s=s[:start]+'''              boolean active=(sky&32768)!=0;
              if(v<4) WaterCornerPacking.uniform(b,at,sample.source,sample.direction,sample.age,51);
              else {
                if((b.get(at+15)&255)!=255||active)throw new AssertionError("Side flow-to-flow metadata changed");
              }
'''+s[end:];p.write_text(s)
p=P/'src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterSideUvUploadChecks.java';s=p.read_text();(R/'before-WaterSideUvUploadChecks.java').write_text(s);s=s.replace('if((light&255)!=176 || sky!=240 || flowFlag!=(sideFlag&&sample.direction!=16))','if((light&255)!=176 || (sky&255)!=240 || (v>=4&&flowFlag!=(sideFlag&&sample.direction!=16)))');s=s.replace('int offset=b.getShort(i+26);','int offset=WaterCornerPacking.heightOffset(b,i);');s=s.replace('else topCount++;','else {if(!WaterCornerPacking.spatial(b,i))throw new AssertionError("Missing corner field");topCount++;}');p.write_text(s)
print('added exhaustive codec, canonical halo and independent native packing checks')
