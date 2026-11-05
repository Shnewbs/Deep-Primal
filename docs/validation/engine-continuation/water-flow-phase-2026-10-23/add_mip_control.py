from pathlib import Path
P=Path(__file__).resolve().parent/'project';p=P/'src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterFlowPhaseChecks.java';s=p.read_text()
s=s.replace('boolean reference)throws Exception {\n    int count=', 'boolean reference,boolean mip)throws Exception {\n    int count=')
s=s.replace('b.putFloat(.125f+u*.5f).putFloat(.25f+v*.5f);b.putShort((short)0).putShort((short)0);b.putShort((short)1024)', 'b.putFloat(mip?u:.125f+u*.5f).putFloat(mip?v:.25f+v*.5f);b.putShort((short)0).putShort((short)0);b.putShort((short)(mip&&reference?0:1024))')
s=s.replace('var av=device.createTextureView(atlas);var lv=device.createTextureView(light);','''var av=device.createTextureView(atlas);var lv=device.createTextureView(light);
    var mipAtlas=device.createTexture("Phase diagnostic mip chain",GpuTexture.USAGE_TEXTURE_BINDING|GpuTexture.USAGE_COPY_DST,TextureFormat.RGBA8,64,64,1,3);var mv=device.createTextureView(mipAtlas);
    for(int level=0;level<3;level++)try(var image=new com.mojang.blaze3d.platform.NativeImage(64>>level,64>>level,false)) {
      int size=64>>level;for(int y=0;y<size;y++)for(int x=0;x<size;x++)image.setPixelABGR(x,y,level==1?0xff00ff00:level==2?0xffff0000:0xff000000|((y*4)<<16)|(x*4));
      encoder.writeToTexture(mipAtlas,image,level,0,0,0,size,size,0,0);
    }''')
s=s.replace('var pixels=new byte[12][]','var pixels=new byte[24][]').replace('for(int i=0;i<12;i++)','for(int i=0;i<24;i++)')
s=s.replace('int which=i;int pair=i/2;boolean reference=(i&1)==0;','int which=i;int pair=(i%12)/2;boolean reference=(i&1)==0,mip=i>=12;')
s=s.replace('quad(DIRECTIONS[pair],ORIGINS[pair],reference)','quad(DIRECTIONS[pair],ORIGINS[pair],reference,mip)')
s=s.replace('.m01(.125f).m02(.5f).m03(.25f).m12(.5f)', '.m01(mip?0:.125f).m02(mip?1:.5f).m03(mip?0:.25f).m12(mip?1:.5f)')
s=s.replace('pass.bindTexture("Sampler0",av,sampler)','pass.bindTexture("Sampler0",mip?mv:av,mip?RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST,true):sampler)')
s=s.replace('complete.incrementAndGet()==12','complete.incrementAndGet()==24').replace('atlas.close();light.close();','atlas.close();light.close();mv.close();mipAtlas.close();')
s=s.replace('for(int p=0;p<6;p++)','for(int p=0;p<12;p++)').replace('"direction",DIRECTIONS[p],"originX",ORIGINS[p]','"direction",DIRECTIONS[p%6],"originX",ORIGINS[p%6],"mipControl",p>=6')
s=s.replace('pairs=6 renders=12','pairs=12 renders=24').replace('worldPhase=true exactPixels=true','worldPhase=true explicitGradientMipControl=true exactPixels=true')
p.write_text(s)
