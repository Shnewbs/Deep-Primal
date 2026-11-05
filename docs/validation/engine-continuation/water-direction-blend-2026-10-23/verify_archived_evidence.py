"""CPU-only replay of compact evidence, without a game, GPU, toolchain or original checkout."""
from pathlib import Path
import tempfile,shutil,subprocess,json,hashlib,sys,datetime
E=Path(__file__).resolve().parent
commands=[['rehydrate_evidence.py'],['audit_water_side.py','vulkan-order-blend-cell-final','vulkan-order-blend-cell-final-packaged'],['audit_opengl_side.py']]
rows=[]
with tempfile.TemporaryDirectory(prefix='deepprimal-evidence-replay-') as tmp:
 d=Path(tmp)/'evidence';shutil.copytree(E,d)
 for command in commands:
  p=subprocess.run([sys.executable]+command,cwd=d,text=True,capture_output=True,timeout=120)
  rows.append({'command':command,'exit_code':p.returncode,'stdout_sha256':hashlib.sha256(p.stdout.encode()).hexdigest(),'stderr':p.stderr})
  assert p.returncode==0,rows[-1]
 for name in ['strict-water-blend-native-audit.json','strict-opengl-blend-audit.json']:
  original=json.loads((E/name).read_text());replayed=json.loads((d/name).read_text())
  if name.startswith('strict-water'):
   for a,b in zip(original['sessions'],replayed['sessions']):
    aa=sorted(a['screenshots'],key=lambda x:x['path']);bb=sorted(b['screenshots'],key=lambda x:x['path']);assert len(aa)==len(bb)
    for x,y in zip(aa,bb):
     assert x['path']==y['path']
     if Path(x['path']).name.startswith('order-live-frame-'):
      case=Path(x['path']).parts[0]
      frame=next(v for v in json.loads((E/case/'frame-video-provenance.json').read_text())['frames'] if case+'/'+v['png']==x['path'])
      assert x['sha256']==frame['png_sha256']
      image=__import__('PIL.Image',fromlist=['Image']).open(d/y['path']).convert('RGB')
      assert hashlib.sha256(image.tobytes()).hexdigest()==frame['rgb_sha256']
      assert y['sha256']==hashlib.sha256((d/y['path']).read_bytes()).hexdigest()
      for value in [x,y]:value.pop('bytes');value.pop('sha256')
    a['screenshots']=aa;b['screenshots']=bb
  assert original==replayed,name
record={'pass':True,'at':datetime.datetime.now(datetime.timezone.utc).isoformat(),'commands':rows,'strict_reports_match_except_reencoded_png_compression':True,'all27_restored_RGB_frames_bit_exact':True,'scope':'CPU-only lossless archive/log replay;not another native acceptance session'}
if '--record' in sys.argv:(E/'offline-audit-replay.json').write_text(json.dumps(record,indent=2)+'\n')
print(json.dumps(record))
