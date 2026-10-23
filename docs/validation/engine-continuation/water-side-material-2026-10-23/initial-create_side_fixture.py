from pathlib import Path
R=Path(__file__).resolve().parent;P=R/'project';src=P/'src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterMaterialTransitionChecks.java'
s=src.read_text().replace('WaterMaterialTransitionChecks','WaterSideMaterialChecks').replace('water-material','water-side-material').replace('WATER_MATERIAL','WATER_SIDE_MATERIAL').replace('material-','side-material-')
# The cell is deliberately unsupported in this isolated upload fixture so the real
# mesher supplies all six faces, including the otherwise hidden underside.
s=s.replace('for(int z=-1;z<=16;z++)for(int x=-1;x<=16;x++)s[WaterPatchMesher.index(x,-1,z)]=1;','')
s=s.replace('count!=20||mesh.bytes!=720','count!=24||mesh.bytes!=864').replace(',720)',',864)').replace('slice(0,720)','slice(0,864)').replace('new byte[720]','new byte[864]')
s=s.replace('route<64','route<320').replace('route==64','route==320').replace('route=0;route<64','route=0;route<320').replace('route%32<16','(route%64)%32<16').replace('route<32?0:29999984','route%64<32?0:29999984').replace('route<32?3:15','route%64<32?3:15').replace('session.read(64,','session.read(320,')
s=s.replace('new byte[2][384][]','new byte[2][1920][]').replace('v<20','v<24').replace('expected=v<4&&s.active','expected=s.active').replace('if(active&&v<4)','if(active)')
a=s.index('              if(v<4) {');b=s.index('              if(wrong)bad++;',a)
s=s[:a]+'''              int face=v/4;
              float x=b.getFloat(at)-s.cell,z=b.getFloat(at+8)-s.cell,y=b.getFloat(at+4),u,w;
              if(face==0) {
                u=x;w=z;
                if(s.target<16){float fx=(float)Math.cos(s.target*Math.PI/8),fz=(float)Math.sin(s.target*Math.PI/8);u=.5f+.5f*(fz*(x-.5f)-fx*(z-.5f))+WaterPatchMesher.flowUOffset(s.target,s.origin+s.cell,-s.origin+s.cell);w=.5f+.5f*(fx*(x-.5f)+fz*(z-.5f))+WaterPatchMesher.flowVOffset(s.target,s.origin+s.cell,-s.origin+s.cell);}
              } else {float scale=s.target<16?.5f:1;u=(face==2||face==4?z:x)*scale;w=(1-y)*scale;}
              var sprite=s.target==16?still:flowing;wrong|=Math.abs(b.getFloat(at+16)-sprite.getU(u))>1e-6||Math.abs(b.getFloat(at+20)-sprite.getV(w))>1e-6;
''' +s[b:]
s=s.replace('int size=profile==0?32:8;var view=new Matrix4f().zero().m00(2).m21(2).m30(-cell*2-1).m31(-cell*2-1).m32(.25f).m33(1);','int size=profile==0?32:8;var view=view(route/64+1,cell);')
s=s.replace('routes=64 renders=768 samples=640 readbacks=393 readbackBytes=282960','routes=320 renders=3840 samples=3200 readbacks=1929 readbackBytes=1666656').replace('nearFarCells=true','nearFarCells=true verticalSidesAndUnderside=true')
s=s.replace('scope=controlledActualProductionUploadsAndShaderNotFullStreamAcceptance','scope=controlledActualProductionSideUploadsAndShaderNotFullStreamAcceptance')
s=s.replace('  private WaterSideMaterialChecks() {}','''  private static Matrix4f view(int face,int cell) {
    var m=new Matrix4f().zero().m33(1);
    if(face==1||face==3) {
      float depth=face==1?.25f:-.25f;
      return m.m00(2).m11(2).m22(depth).m30(-2*cell-1).m31(-1).m32(.25f-depth*cell+(face==3?.25f:0));
    }
    if(face==2||face==4) {
      float depth=face==4?.25f:-.25f;
      return m.m20(2).m11(2).m02(depth).m30(-2*cell-1).m31(-1).m32(.25f-depth*cell+(face==2?.25f:0));
    }
    return m.m00(2).m21(2).m12(.25f).m30(-2*cell-1).m31(-2*cell-1).m32(.25f);
  }
  private WaterSideMaterialChecks() {}''')
# Do not duplicate the prefix when renaming both file roots and individual images.
s=s.replace('water-side-side-material','water-side-material')
(P/'src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterSideMaterialChecks.java').write_text(s)
p=P/'src/visualValidation/java/com/shnewbs/deepprimal/validation/ClientCapture.java';s=p.read_text();needle='if (action.equals("water-material-check"))';at=s.index(needle);s=s[:at]+'''if (action.equals("water-side-material-check")) {
        WaterSideMaterialChecks.run(mc, dir);
        return;
      }
      '''+s[at:];p.write_text(s)
