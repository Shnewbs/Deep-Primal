from pathlib import Path
R=Path(__file__).resolve().parent;p=R/'project/src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterBoundaryBatchChecks.java';s=p.read_text()
s=s.replace('for(int axis=0;axis<2;axis++)','for(int axis=0;axis<8;axis++)')
s=s.replace('''          int sx=origin>>4,sz=-origin>>4;Object admitted=pc.newInstance(SectionPos.asLong(sx,0,sz));
          Object neighbor=pc.newInstance(SectionPos.asLong(sx-(axis==0?1:0),0,sz-(axis==1?1:0)));''','''          int[][] deltas={{1,0},{0,1},{-1,0},{0,-1},{1,1},{-1,1},{1,-1},{-1,-1}};
          int dx=deltas[axis][0],dz=deltas[axis][1];
          int sx=origin>>4,sz=-origin>>4;Object admitted=pc.newInstance(SectionPos.asLong(sx,0,sz));
          Object neighbor=pc.newInstance(SectionPos.asLong(sx-dx,0,sz-dz));''')
s=s.replace('''          for(int y=0;y<16;y++)for(int lane=0;lane<=16;lane++) {
            int x=axis==0?ax:ax+lane,z=axis==0?az+lane:az;var state=new WaterFlowTransition(4,0);state.target(8,100,1);state.target(12,125,2);directions.put(BlockPos.asLong(x,y,z),state);
          }
          for(int n=0;n<count;n++){int q=n+(group==256?0:n/group);int y=(n%256)/16,lane=n%16,cx=axis==0?15:lane,cz=axis==0?lane:15;entries[n]=(q<<12)|(y<<8)|(cz<<4)|cx;}''','''          for(int y=0;y<16;y++)for(int x=ax;x<=ax+16;x++)for(int z=az;z<=az+16;z++) {
            if(x!=ax&&x!=ax+16&&z!=az&&z!=az+16)continue;
            var state=new WaterFlowTransition(4,0);state.target(8,100,1);state.target(12,125,2);directions.put(BlockPos.asLong(x,y,z),state);
          }
          for(int n=0;n<count;n++) {
            int q=n+(group==256?0:n/group),y=(n%256)/16,lane=n%16;
            int cx=dx==0?lane:dx>0?15:0,cz=dz==0?lane:dz>0?15:0;
            // Half the entries face away from the admitted page. Keep them in GPU order.
            if((n/(group==256?128:8)&1)!=0){if(dx!=0)cx=15-cx;else cz=15-cz;}
            entries[n]=(q<<12)|(y<<8)|(cz<<4)|cx;
          }''')
s=s.replace('''          int expected=group==256?(count+1819)/1820:(count+7)/8;
          if(requireBatches&&fail==0&&firstAttempts!=expected)throw new AssertionError("Contiguous boundary batches were not combined");''','''          int expected=group==256?count/256:count/16;
          if(requireBatches&&fail==0&&firstAttempts!=expected)throw new AssertionError("Contiguous boundary batches were not combined");''')
s=s.replace('WATER_BOUNDARY_BATCH_PASS cases=44 readbacks=88','WATER_BOUNDARY_BATCH_PASS cases=176 readbacks=352')
s=s.replace('Private256/2048boundaryquad realGPU fixture;','Private256/2048boundaryquad realGPU fixture,all8neighbors,halfunrelated;')
p.write_text(s);(R/'baseline'/p.relative_to(R/'project')).write_text(s)
p=R/'audit_boundary_batches.py';s=p.read_text();a=s.index(' ns=[1,3]');b=s.index(' return initial,expected,touched',a)
s=s[:a]+''' dx,dz=[(1,0),(0,1),(-1,0),(0,-1),(1,1),(-1,1),(1,-1),(-1,-1)][row['axis']]
 touched=set()
 for entry in row['boundaryEntries']:
  q=entry>>12;cx=entry&15;cz=(entry>>4)&15;ns=[]
  for n in range(4):
   x=cx+(n&1);z=cz+(n>>1)
   if dx*16<=x<=dx*16+16 and dz*16<=z<=dz*16+16:ns.append(n)
  if not ns:continue
  touched.add(q);pairs=sum((140 if n in ns else 72)*POW[n]for n in range(4));starts=[100 if n in ns else 0 for n in range(4)]
  for v in range(4):packet(expected,q*144+v*36,pairs,starts)
'''+s[b:]
s=s.replace('cases=44 readbacks=88','cases=176 readbacks=352').replace("len(r['cases'])==44 and len(r['readbacks'])==88","len(r['cases'])==176 and len(r['readbacks'])==352")
s=s.replace("first==(row['count'] if baseline else((row['count']+1819)//1820 if row['group']==256 else (row['count']+7)//8))","first==(len(touched) if baseline else (row['count']//256 if row['group']==256 else row['count']//16))")
s=s.replace("sum(v['bytes']for v in ranges)==row['count']*144","sum(v['bytes']for v in ranges)==len(touched)*144").replace("'bytes':row['count']*144,'quads':row['count']","'bytes':len(touched)*144,'quads':len(touched)")
s=s.replace("'cases':44,'paired_cpu_gpu_reads':88","'cases':176,'paired_cpu_gpu_reads':352")
p.write_text(s)
print('Expanded same native fixture for fresh baseline and candidate:8directions/half unrelated entries/176cases')
