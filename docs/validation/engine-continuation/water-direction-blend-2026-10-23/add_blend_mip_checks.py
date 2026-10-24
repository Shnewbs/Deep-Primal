from pathlib import Path
R=Path(__file__).resolve().parent;P=R/'project'
p=P/'graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders/core/animated_water.fsh';s=p.read_text();(R/'pre-mip-animated_water.fsh').write_text(s)
s=s.replace('    if (previousWeight > 0.0) {','    vec2 oldDx = dFdx(previousTexCoord), oldDy = dFdy(previousTexCoord);\n    if (previousWeight > 0.0) {').replace('textureGrad(Sampler0, oldUV, dFdx(previousTexCoord), dFdy(previousTexCoord))','textureGrad(Sampler0, oldUV, oldDx, oldDy)');p.write_text(s)
p=P/'src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterDirectionBlendChecks.java';s=p.read_text();(R/'pre-mip-WaterDirectionBlendChecks.java').write_text(s)
s=s.replace('boolean active,int originX,int originZ','boolean active,int originX,int originZ,boolean mip')
s=s.replace('b.putFloat(.125f+u*.5f).putFloat(.25f+v*.5f);','b.putFloat(mip?u:.125f+u*.5f).putFloat(mip?v:.25f+v*.5f);')
s=s.replace('var av=device.createTextureView(atlas);var lv=device.createTextureView(light);','''var av=device.createTextureView(atlas);var lv=device.createTextureView(light);
    var mipAtlas=device.createTexture("Blend mip diagnostic",GpuTexture.USAGE_TEXTURE_BINDING|GpuTexture.USAGE_COPY_DST,TextureFormat.RGBA8,128,128,1,3);var mv=device.createTextureView(mipAtlas);
    for(int level=0;level<3;level++)try(var image=new com.mojang.blaze3d.platform.NativeImage(128>>level,128>>level,false)) {
      int size=128>>level;for(int y=0;y<size;y++)for(int x=0;x<size;x++)image.setPixelABGR(x,y,level==1?0xff00ff00:level==2?0xffff0000:0xff000000|((y*2)<<16)|(x*2));
      encoder.writeToTexture(mipAtlas,image,level,0,0,0,size,size,0,0);
    }''')
s=s.replace('new byte[224][]','new byte[336][]').replace('i<224','i<336')
s=s.replace('originZ=-originX;var mesh=quad(', 'originZ=-originX;boolean mip=group>=32;var mesh=quad(')
s=s.replace('slot>=2,originX,originZ)','slot>=2,originX,originZ,mip)')
s=s.replace('.m01(.125f).m02(.5f).m03(.25f).m12(.5f)','.m01(mip?0:.125f).m02(mip?1:.5f).m03(mip?0:.25f).m12(mip?1:.5f)')
s=s.replace('pass.bindTexture("Sampler0",av,sampler)','pass.bindTexture("Sampler0",mip?mv:av,mip?RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST,true):sampler)')
s=s.replace('complete.incrementAndGet()==224','complete.incrementAndGet()==336').replace('atlas.close();light.close();','atlas.close();light.close();mv.close();mipAtlas.close();')
s=s.replace('sampleGroup<32','sampleGroup<48').replace('"millis",sample*25,','"millis",sample*25,"mipControl",sampleGroup>=32,')
s=s.replace('samples=160 renders=224','samples=240 renders=336').replace('directions=16 origins=2','directions=16 origins=2 mipControls=true')
p.write_text(s)
for f in ['audit_water_side.py','audit_opengl_side.py']:
 p=R/f;s=p.read_text().replace('samples=160 renders=224 directions=16 origins=2','samples=240 renders=336 directions=16 origins=2 mipControls=true').replace('assert len(j)==160','assert len(j)==240').replace('for group in range(32):','for group in range(48):')
 s=s.replace('224 shader renders','336 shader renders').replace('vulkan-order-blend-checked-final','vulkan-order-blend-mip-final')
 p.write_text(s)
print('Unconditional old-sample derivatives and diagnostic mip groups added')
