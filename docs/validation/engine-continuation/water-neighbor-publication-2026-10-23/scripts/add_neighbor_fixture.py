from pathlib import Path
R=Path(__file__).resolve().parent
for base in ['project','baseline/project']:
 p=R/base/'src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterCornerFieldChecks.java';s=p.read_text();
 if base=='project':(R/'before-WaterCornerFieldChecks.java').write_text(s)
 needle='    public void close()throws Exception{'
 method='''    Input resident(Input old,int a,int b,long drawTime)throws Exception {
      long key=SectionPos.asLong(((int)old.texture.m30())>>4,0,((int)old.texture.m31())>>4);Object page=local.get(key);
      var gpu=(WaterGpuMesh)field(pt,"gpu").get(page);int count=field(WaterGpuMesh.class,"indices").getInt(gpu)/6*4;
      var read=RenderSystem.getDevice().createBuffer(()->"Unrepacked resident neighbor",GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_MAP_READ,count*36L);
      RenderSystem.getDevice().createCommandEncoder().copyToBuffer(((GpuBuffer)field(WaterGpuMesh.class,"vertices").get(gpu)).slice(0,count*36L),read.slice());
      long uploaded=field(pt,"uploaded").getLong(page);
      return new Input(old.route,old.side,a,b,old.axis,old.origin,old.cellX,old.cellZ,drawTime,uploaded,0,old.topVertex,count,new Matrix4f((Matrix4f)field(pt,"texture").get(page)).m00(drawTime-uploaded),read);
    }
'''
 s=s.replace(needle,method+needle)
 needle='      try {\n        var method=session.type.getDeclaredMethod("cornerFlow"'
 block='''      // New request arrives on only one page: read the resident neighbor without a capture/upload request.
      for(int origin:new int[]{0,29999968})for(int cell:new int[]{3,15})for(int axis=0;axis<2;axis++)for(int age:new int[]{0,25,50,75,100})for(int first=0;first<2;first++) {
        session.reset();var oldLeft=session.put(route,0,0,4,axis,origin,cell,0,0);var oldRight=session.put(route,1,0,4,axis,origin,cell,0,0);
        oldLeft.read.close();oldRight.read.close();
        long started=origin==0?100:20000;
        var changed=session.put(route,first,4,8,axis,origin,cell,started,started+age);
        var resident=session.resident(first==0?oldRight:oldLeft,4,8,started+age);
        if(first==0){inputs.add(changed);inputs.add(resident);}else{inputs.add(resident);inputs.add(changed);}route++;
      }
'''
 assert needle in s;s=s.replace(needle,block+needle);s=s.replace('no full scene/stream or partial neighbor-request atomicity','staggered new-request resident-neighbor publication included; no full scene/stream/cap-churn acceptance')
 p.write_text(s)
