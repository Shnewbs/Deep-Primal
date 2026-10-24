from pathlib import Path
R=Path(__file__).resolve().parent;p=R/'project/src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterCornerFieldChecks.java';s=p.read_text();(R/'pre-mip-WaterCornerFieldChecks.java').write_text(s)
needle='''var av=device.createTextureView(atlas);''';assert needle in s;s=s.replace(needle,needle+'''
    var mip=device.createTexture("Corner mip atlas",GpuTexture.USAGE_TEXTURE_BINDING|GpuTexture.USAGE_COPY_DST,TextureFormat.RGBA8,width,height,1,3);var mv=device.createTextureView(mip);''')
s=s.replace('encoder.writeToTexture(atlas,image);','encoder.writeToTexture(atlas,image);encoder.writeToTexture(mip,image);')
needle='''    var light=device.createTexture("Corner white light"''';assert needle in s;s=s.replace(needle,'''    for(int level=1;level<=2;level++)try(var image=new com.mojang.blaze3d.platform.NativeImage(width>>level,height>>level,false)){
      for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)image.setPixelABGR(x,y,level==1?0xff00ff00:0xffff0000);
      encoder.writeToTexture(mip,image,level,0,0,0,image.getWidth(),image.getHeight(),0,0);
    }
'''+needle)
s=s.replace('var uniformPixels=new ArrayList<Pixels>();','var uniformPixels=new ArrayList<Pixels>();var mipPixels=new ArrayList<Pixels>();')
s=s.replace('ByteOrder.nativeOrder());WaterGpuMesh mesh;','ByteOrder.nativeOrder());WaterGpuMesh mesh;byte[] probeBytes=new byte[144];')
s=s.replace('data.flip();mesh=new WaterGpuMesh(data,4);','data.flip();data.get(0,probeBytes);mesh=new WaterGpuMesh(data,4);')
needle='''          if(input.a==input.b && input.side==0 && input.route<2312)for(int reference=0;reference<2;reference++) {''';assert needle in s
extra='''          var mipData=MemoryUtil.memAlloc(144).order(ByteOrder.nativeOrder());WaterGpuMesh mipMesh;
          try{mipData.put(probeBytes).flip();mipMesh=new WaterGpuMesh(mipData,4);}finally{MemoryUtil.memFree(mipData);}
          var mipColor=device.createTexture("Corner mip edge color",GpuTexture.USAGE_RENDER_ATTACHMENT|GpuTexture.USAGE_COPY_SRC,TextureFormat.RGBA8,8,8,1,1);var mipDepth=device.createTexture("Corner mip edge depth",GpuTexture.USAGE_RENDER_ATTACHMENT,TextureFormat.DEPTH32,8,8,1,1);var mcv=device.createTextureView(mipColor);var mdv=device.createTextureView(mipDepth);
          try(var pass=encoder.createRenderPass(()->"Actual uploaded water mip edge",mcv,OptionalInt.of(0),mdv,OptionalDouble.of(1))){pass.setPipeline(pipeline);RenderSystem.bindDefaultUniforms(pass);pass.setUniform("Projection",projection);pass.setUniform("DynamicTransforms",transform);pass.bindTexture("Sampler0",mv,RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST,true));pass.bindTexture("Sampler2",lv,sampler);mipMesh.draw(pass);}
          var mr=device.createBuffer(()->"Corner mip edge readback",GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_MAP_READ,256);encoder.copyTextureToBuffer(mipColor,mr,0,()->{},0);mipPixels.add(new Pixels(input,2,input.side,8,8,mr,mipColor,mipDepth,mcv,mdv,mipMesh));
'''
s=s.replace(needle,extra+'''          if(input.a==input.b && input.side==0 && input.route<2312)for(int profile=0;profile<2;profile++)for(int reference=0;reference<2;reference++) {
            int wholeSize=profile==0?32:8;''')
s=s.replace('TextureFormat.RGBA8,32,32,1,1);var wholeDepth','TextureFormat.RGBA8,wholeSize,wholeSize,1,1);var wholeDepth')
s=s.replace('TextureFormat.DEPTH32,32,32,1,1);var wcv','TextureFormat.DEPTH32,wholeSize,wholeSize,1,1);var wcv')
s=s.replace('pass.setUniform("DynamicTransforms",wholeTransform);pass.bindTexture("Sampler0",av,sampler);','pass.setUniform("DynamicTransforms",wholeTransform);pass.bindTexture("Sampler0",profile==0?av:mv,profile==0?sampler:RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST,true));')
s=s.replace('GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_MAP_READ,4096);encoder.copyTextureToBuffer(wholeColor','GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_MAP_READ,wholeSize*wholeSize*4);encoder.copyTextureToBuffer(wholeColor')
s=s.replace('new Pixels(input,1,reference,32,32,wr','new Pixels(input,profile==0?1:3,reference,wholeSize,wholeSize,wr')
s=s.replace('pixels.addAll(uniformPixels);','pixels.addAll(mipPixels);pixels.addAll(uniformPixels);')
s=s.replace('max=0,uniformRoutes=0;','max=0,uniformRoutes=0,uniformMipRoutes=0;')
s=s.replace('if(p.kind==1)uniformRoutes++;','if(p.kind==1)uniformRoutes++;if(p.kind==3)uniformMipRoutes++;')
s=s.replace('result.put("uniform_routes",uniformRoutes);','result.put("uniform_routes",uniformRoutes);result.put("uniform_mip_routes",uniformMipRoutes);result.put("mip_edge_routes",routes);result.put("comparisons",rows.size());')
s=s.replace('projection.close();av.close();lv.close();atlas.close();light.close();','projection.close();av.close();mv.close();lv.close();atlas.close();mip.close();light.close();')
p.write_text(s);print('added mixed-edge and whole-cell mip controls')
