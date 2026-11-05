from pathlib import Path
R=Path(__file__).resolve().parent;D=R/'project/src/visualValidation/java/com/shnewbs/deepprimal/validation';p=D/'WaterCornerFieldChecks.java';s=p.read_text();(R/'pre-uniform-WaterCornerFieldChecks.java').write_text(s)
s=s.replace('private record Pixels(Input in,GpuBuffer read,','private record Pixels(Input in,int kind,int endpoint,int width,int height,GpuBuffer read,')
s=s.replace('var pixels=new ArrayList<Pixels>();var rawVertices','var pixels=new ArrayList<Pixels>();var uniformPixels=new ArrayList<Pixels>();var rawVertices')
s=s.replace('pixels.add(new Pixels(input,read,color,depth,cv,dv,mesh));','pixels.add(new Pixels(input,0,input.side,8,32,read,color,depth,cv,dv,mesh));')
needle='''          var read=device.createBuffer(()->"Corner edge pixel readback",GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_MAP_READ,1024);encoder.copyTextureToBuffer(color,read,0,()->{},0);pixels.add(new Pixels(input,0,input.side,8,32,read,color,depth,cv,dv,mesh));'''
assert needle in s
extra=r'''
          if(input.a==input.b && input.side==0 && input.route<2312)for(int reference=0;reference<2;reference++) {
            var whole=MemoryUtil.memAlloc(144).order(ByteOrder.nativeOrder());WaterGpuMesh uniform;
            try {
              for(int n=0;n<4;n++) {
                byte[] vertex=new byte[36];b.get(first+n*36,vertex);int at=whole.position();whole.put(vertex);
                if(reference==1) {
                  float x=b.getFloat(first+n*36)-input.cellX,z=b.getFloat(first+n*36+8)-input.cellZ,u=x,v=z;
                  if(input.a<16) {
                    float fx=(float)Math.cos(input.a*Math.PI/8),fz=(float)Math.sin(input.a*Math.PI/8);
                    int wx=(int)input.texture.m30()+input.cellX,wz=(int)input.texture.m31()+input.cellZ;
                    u=.5f+.5f*(fz*(x-.5f)-fx*(z-.5f))+WaterPatchMesher.flowUOffset(input.a,wx,wz);
                    v=.5f+.5f*(fx*(x-.5f)+fz*(z-.5f))+WaterPatchMesher.flowVOffset(input.a,wx,wz);
                  }
                  var sprite=input.a<16?flow:still;
                  whole.put(at+15,(byte)255);whole.putFloat(at+16,sprite.getU(u));whole.putFloat(at+20,sprite.getV(v));
                  whole.putShort(at+26,(short)WaterCornerPacking.heightOffset(b,first+n*36));whole.putShort(at+28,(short)((b.getShort(first+n*36+28)&255)|(input.a<16?1024:0)));whole.putShort(at+30,(short)(b.getShort(first+n*36+30)&255));
                }
              }
              whole.flip();uniform=new WaterGpuMesh(whole,4);
            }finally{MemoryUtil.memFree(whole);}
            var wholeView=new Matrix4f().zero().m00(2).m21(2).m30(-input.cellX*2-1).m31(-input.cellZ*2-1).m32(.25f).m33(1);
            var wholeTransform=RenderSystem.getDynamicUniforms().writeTransform(wholeView,new Vector4f(1),new Vector3f(),input.texture);
            var wholeColor=device.createTexture("Corner uniform color",GpuTexture.USAGE_RENDER_ATTACHMENT|GpuTexture.USAGE_COPY_SRC,TextureFormat.RGBA8,32,32,1,1);var wholeDepth=device.createTexture("Corner uniform depth",GpuTexture.USAGE_RENDER_ATTACHMENT,TextureFormat.DEPTH32,32,32,1,1);var wcv=device.createTextureView(wholeColor);var wdv=device.createTextureView(wholeDepth);
            try(var pass=encoder.createRenderPass(()->"Actual corner uniform-flow fidelity",wcv,OptionalInt.of(0),wdv,OptionalDouble.of(1))){pass.setPipeline(pipeline);RenderSystem.bindDefaultUniforms(pass);pass.setUniform("Projection",projection);pass.setUniform("DynamicTransforms",wholeTransform);pass.bindTexture("Sampler0",av,sampler);pass.bindTexture("Sampler2",lv,sampler);uniform.draw(pass);}
            var wr=device.createBuffer(()->"Corner uniform pixel readback",GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_MAP_READ,4096);encoder.copyTextureToBuffer(wholeColor,wr,0,()->{},0);uniformPixels.add(new Pixels(input,1,reference,32,32,wr,wholeColor,wholeDepth,wcv,wdv,uniform));
          }
'''
s=s.replace(needle,needle+extra)
s=s.replace('''        Files.write(out.resolve("water-corner-upload.bin"),''','''        pixels.addAll(uniformPixels);
        Files.write(out.resolve("water-corner-upload.bin"),''')
s=s.replace('byte[] previous=null;int bad=0,coverage=0,max=0;','byte[] previous=null;int previousOffset=0,bad=0,coverage=0,max=0,uniformRoutes=0;')
s=s.replace('byte[] bytes=new byte[1024];mapped.data().get(0,bytes);raw.writeBytes(bytes);','byte[] bytes=new byte[p.width*p.height*4];mapped.data().get(0,bytes);int pixelOffset=raw.size();raw.writeBytes(bytes);')
s=s.replace('if(p.in.side==0){previous=bytes;continue;}','if(p.endpoint==0){previous=bytes;previousOffset=pixelOffset;continue;}if(p.kind==1)uniformRoutes++;')
needle='''            rows.add(Map.of("route",p.in.route,"a",p.in.a,"b",p.in.b,"axis",p.in.axis,"origin",p.in.origin,"cell",p.in.cellX,"time",p.in.time,"maxChannelError",maximum,"channelsBeyondTolerance",errors));'''
assert needle in s
s=s.replace(needle,'''            var row=new LinkedHashMap<String,Object>();row.put("route",p.in.route);row.put("kind",p.kind);row.put("a",p.in.a);row.put("b",p.in.b);row.put("axis",p.in.axis);row.put("origin",p.in.origin);row.put("cellX",p.in.cellX);row.put("cellZ",p.in.cellZ);row.put("time",p.in.time);row.put("width",p.width);row.put("height",p.height);row.put("leftOffset",previousOffset);row.put("rightOffset",pixelOffset);row.put("maxChannelError",maximum);row.put("channelsBeyondTolerance",errors);rows.add(row);''')
s=s.replace('if(p.in.route==4||p.in.route==68||p.in.route==routes-3)','if((p.kind==0&&(p.in.route==4||p.in.route==68||p.in.route==routes-3))||(p.kind==1&&p.in.a==4&&p.in.origin==0&&p.in.cellX==3&&p.in.axis==0))')
s=s.replace('NativeImage(8,32,false)){var b=ByteBuffer.wrap(side==0?previous:bytes).order(ByteOrder.nativeOrder());for(int y=0;y<32;y++)for(int x=0;x<8;x++)image.setPixelABGR(x,y,b.getInt((y*8+x)*4));image.writeToFile(out.resolve("corner-"+p.in.route+"-"+side+".png"));','NativeImage(p.width,p.height,false)){var b=ByteBuffer.wrap(side==0?previous:bytes).order(ByteOrder.nativeOrder());for(int y=0;y<p.height;y++)for(int x=0;x<p.width;x++)image.setPixelABGR(x,y,b.getInt((y*p.width+x)*4));image.writeToFile(out.resolve("corner-"+p.kind+"-"+p.in.route+"-"+side+".png"));')
s=s.replace('result.put("routes",routes);','result.put("routes",routes);result.put("uniform_routes",uniformRoutes);');p.write_text(s)
print('added whole-cell legacy-reference uniform fidelity controls')
