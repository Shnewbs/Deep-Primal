from pathlib import Path
R=Path(__file__).resolve().parent;P=R/'project';p=P/'src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterSideMaterialChecks.java';s=p.read_text();(R/'pre-height-WaterSideMaterialChecks.java').write_text(s)
s=s.replace('snapshot(int direction,int cell)', 'snapshot(int direction,int cell,int amount)').replace('u[key]=16;','u[key]=(byte)amount;').replace('int cell,origin;long revision;','int cell,origin,amount;long revision;')
s=s.replace('void start(int origin,int cell,int direction)throws Exception {','''void start(int origin,int cell,int direction)throws Exception {start(origin,cell,direction,16);}
    void start(int origin,int cell,int direction,int amount)throws Exception {
      this.amount=amount;''').replace('snapshot(direction,cell);','snapshot(direction,cell,amount);')
a=s.index('          for(int profile=0;profile<2;profile++) {');b=s.index('\n        }\n      }\n      session.start(0,3,16);',a);draw=s[a:b]
s=s[:b]+s[b:].replace('      session.start(0,3,16);','''      for(int extra=0;extra<16;extra++) {
        int route=320+extra,source=extra%2==0?16:0,target=source==16?0:16,origin=extra%4<2?0:29999984,cell=extra%4<2?3:15;
        session.start(origin,cell,source,4);
        for(int slot=1;slot<6;slot++) {
          long time=100+(slot-1)*25L;session.amount=16;session.put(target,time,++session.revision);
          samples.add(session.read(route,slot,source,target,(slot-1)*25,slot<5,time));
''' +draw+'''
        }
      }
      session.start(0,3,16);''',1)
s=s.replace('session.read(320,n,','session.read(336,n,').replace('s.route==320','s.route==336').replace('new byte[2][1920][]','new byte[2][2016][]').replace('view(route/64+1,cell)','view(route<320?route/64+1:(route-320)/4+1,cell)')
a=s.index('          Files.write(out.resolve("water-side-material-upload.bin")')
s=s[:a]+'''          for(int profile=0;profile<2;profile++)for(int extra=0;extra<16;extra++)for(int phase=0;phase<5;phase++) {
            int route=320+extra,base=(extra/4)*64+(extra%4<2?0:32)+(extra%2==0?0:16),size=profile==0?32:8;
            var a=rgba[profile][base*6];var b=rgba[profile][base*6+5];var c=rgba[profile][route*6+phase+1];float t=phase*.25f;int violations=0,maximum=0;
            // Bottom quarter is covered at every displayed height, including the
            // initial4/16 column. Compare the same physical pixels, not rescaled quads.
            for(int y=size*3/4;y<size;y++)for(int x=0;x<size;x++)for(int ch=0;ch<4;ch++) {
              int i=(y*size+x)*4+ch,error=Math.abs((c[i]&255)-Math.round((a[i]&255)*(1-t)+(b[i]&255)*t));maximum=Math.max(maximum,error);if(error>1)violations++;
            }
            imageRows.add(Map.of("profile",profile,"route",route,"millis",phase*25,"maxChannelError",maximum,"channelsBeyondTolerance",violations,"matchedGeometryCrop",true));channels+=violations;max=Math.max(max,maximum);
          }
''' +s[a:]
s=s.replace('routes=320 renders=3840 samples=3200 readbacks=1929 readbackBytes=1666656','routes=336 renders=4000 samples=3360 readbacks=2009 readbackBytes=1735776').replace('verticalSidesAndUnderside=true','verticalSidesAndUnderside=true simultaneousHeightMaterial=true')
p.write_text(s)
print('added16 simultaneous4-to16 height/material routes,160 draws/160 lower-quarter RGBA controls,80 readbacks')
