from pathlib import Path
import json,hashlib,gzip,struct,sys
from audit_boundary_batches import packet,POW
from audit_console import audit as console
def sha(b):return hashlib.sha256(b).hexdigest()
def desired(row,kind):
 raw=bytearray(row['bytes'])
 for q in range(row['quads']):
  for v in range(4):
   p=q*144+v*36;struct.pack_into('<fffI',raw,p,q+(v&1)*.5,q%17+(v>>1)*.25,-q,0x1a234567);struct.pack_into('<H',raw,p+24,1234);struct.pack_into('<H',raw,p+28,176);struct.pack_into('<H',raw,p+30,240);raw[p+32]=127;raw[p+34]=q%16;raw[p+35]=91;packet(raw,p,72*sum(POW),[0]*4)
 if kind=='initial':assert sha(raw)==row['initialSha256'];return raw
 dx,dz=[(1,0),(0,1),(-1,0),(0,-1),(1,1),(-1,1),(1,-1),(-1,-1)][row['axis']]
 for entry in row['boundaryEntries']:
  ns=[n for n in range(4)if dx*16<=(entry&15)+(n&1)<=dx*16+16 and dz*16<=((entry>>4)&15)+(n>>1)<=dz*16+16]
  if not ns:continue
  pair=288 if kind=='supersede'else144 if kind=='expired'else140
  age=100 if kind in ['supersede','expired']else25
  now=row['initialClock']+(200 if kind=='expired'else0)
  pairs=sum((pair if n in ns else72)*POW[n]for n in range(4));starts=[(now-age)&65535 if n in ns else0 for n in range(4)]
  for v in range(4):packet(raw,(entry>>12)*144+v*36,pairs,starts)
 return raw
def mutate(b):
 for p in range(0,len(b),36):b[p+12]=19;b[p+24:p+26]=b'\0\0';b[p+32]=63
 return b
def audit(case,baseline=False):
 report=json.loads((case/'water-boundary-retry.json').read_text());assert report['pass']and report['required']==(not baseline);assert len(report['cases'])==40 and len(report['readbacks'])==120
 c=console(case);txt=gzip.decompress((case/'console-final.gz').read_bytes()).decode()
 for marker in ['WATER_BOUNDARY_RETRY_PASS cases=40 readbacks=120','CLIENT_SAVED_DISCONNECT_PASS','Saving players','Saving worlds','All dimensions are saved']:assert marker in txt,marker
 for marker in ['AssertionError','Exception in thread','VUID-','Failed to save']:assert marker not in txt,marker
 assert not json.loads((case/'crash-delta.json').read_text())['new_or_changed'];assert all(x['completed']for x in json.loads((case/'action-history.json').read_text()))
 if 'BACKEND_SELECTION_PASS backend=Vulkan'in txt:
  for marker in ['validationErrors=0','VULKAN_RESOURCES_RELEASED buffers=0 images=0']:assert marker in txt
  assert 'VULKAN_SHUTDOWN_CONTINUATION_PASS'in(case/'shutdown.txt').read_text()
 expected={};frames=[];automatic=terminal=cancel=0;maxbytes=maxpending=0;times=[]
 for r in report['cases']:
  assert r['recovering']==(not baseline)and r['initialThrew']==baseline
  initial=desired(r,'initial');normal=desired(r,'normal');superseded=desired(r,'supersede');expired=mutate(desired(r,'expired'))
  after=bytearray(initial)
  for w in r['writes']:
   assert w['offset']%144==w['bytes']%144==0 and 0<=w['offset']<w['offset']+w['bytes']<=len(after)
   if w['phase']==0 and not w['rejected']:after[w['offset']:w['offset']+w['bytes']]=normal[w['offset']:w['offset']+w['bytes']]
  expected[(r['id'],'after-call')]=bytes(after)
  if r['scenario']==1:mutate(after)
  for w in r['writes']:
   if w['phase']==2 and not w['rejected']:after[w['offset']:w['offset']+w['bytes']]=superseded[w['offset']:w['offset']+w['bytes']]
  first=None;previous=-1
  for turn in r['turns']:
   frame=turn['frame'];assert frame>previous;previous=frame;frames.append(frame);times.append(turn['nanos']/1e6)
   writes=[w for w in r['writes']if w['phase']==1 and w['frame']==frame];assert len(writes)==turn['writes']<=1 and sum(w['bytes']for w in writes)==turn['bytes']<=65520
   assert 0<=turn['pending']<=16 and 0<=turn['pendingBytes']<=1048576
   maxbytes=max(maxbytes,turn['bytes']);maxpending=max(maxpending,turn['pendingBytes'])
   for w in writes:
    if not w['rejected']:
     goal=expired if r['scenario']==1 else normal;after[w['offset']:w['offset']+w['bytes']]=goal[w['offset']:w['offset']+w['bytes']]
   if first is None:first=bytes(after)
  expected[(r['id'],'after-first-turn')]=first;expected[(r['id'],'after-recovery')]=bytes(after)
  if not baseline:
   if r['scenario']in [0,1]:assert after==(expired if r['scenario']==1 else normal);automatic+=1
   if r['scenario']==2:assert after==superseded;cancel+=1
   if r['scenario']==3:assert r['terminal']and len(r['turns'])==3 and all(w['rejected']for w in r['writes']if w['phase']==1);terminal+=1
   else:assert not r['terminal']
   if r['scenario']==4:assert after==expected[(r['id'],'after-call')]and r['turns'][-1]['pending']==0;cancel+=1
   if r['id']==0:assert r['storageBoundBytes']==1048320 and r['storageBoundRanges']==4
  else:assert not r['terminal']and not any(w['phase']==1 for w in r['writes'])
 assert len(frames)==len(set(frames))
 data=(case/'water-boundary-retry.bin').read_bytes();end=0
 for r in report['readbacks']:
  assert r['offset']==end;n=r['bytes'];cpu=data[end:end+n];gpu=data[end+n:end+n*2];end+=n*2
  assert cpu==gpu==expected[(r['id'],r['phase'])]and sha(cpu)==r['sha256'],(r['id'],r['phase'])
 assert end==len(data)
 return {'pass':True,'case':case.name,'baseline':baseline,'cases':40,'paired_reads':120,'initial_unhandled_rejections':40 if baseline else0,'automatic_recoveries':automatic,'supersession_or_stale_cancellations':cancel,'bounded_terminal_cases':terminal,'distinct_retry_frames':len(frames),'max_bytes_per_retry_frame':maxbytes,'max_observed_pending_bytes':maxpending,'retry_cpu_max_ms':max(times),'raw_readback_sha256':sha(data),'fixture_class_sha256':report['fixtureClassSha256'],'normal_save_shutdown':True,'new_crashes':0,'scope':'Private readable GPU pages, actual distinct rendered-frame turns and controlled synchronous pre-backend failures; no async-loss/whole-cohort/real-stream/full-scene latency acceptance'}
if __name__=='__main__':print(json.dumps(audit(Path(sys.argv[1]),'--baseline'in sys.argv),indent=2))
