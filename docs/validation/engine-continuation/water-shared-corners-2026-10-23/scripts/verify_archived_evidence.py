from pathlib import Path
import json,gzip,hashlib,zipfile,subprocess,sys,tempfile
E=Path(sys.argv[1]) if len(sys.argv)>1 else Path(__file__).resolve().parents[1]
def sha(b):return hashlib.sha256(b).hexdigest()
rows=json.loads((E/'compressed-data-manifest.json').read_text())
for r in rows:
 z=(E/r['gzip_path']).read_bytes();b=gzip.decompress(z);assert len(z)==r['gzip_bytes'] and sha(z)==r['gzip_sha256'];assert len(b)==r['bytes'] and sha(b)==r['sha256'];assert not (E/r['path']).exists(),'Expanded raw unexpectedly staged'
archives=json.loads((E/'compact-pixel-archives.json').read_text());pngs=0
for r in archives:
 p=E/(r['case']+'-pixels.zip');assert sha(p.read_bytes())==r['sha256']
 with zipfile.ZipFile(p) as z:
  assert z.testzip() is None;index=json.loads(z.read('manifest.json'));assert len(index)==r['source_files']
  for n,h in index.items():assert sha(z.read('pixels/'+h+'.png'))==h;pngs+=1
cases=['exact-baseline','vulkan-corners-clock-final','vulkan-corners-clock-final-packaged','vulkan-reuse-gl-corners-clock-final']
with tempfile.NamedTemporaryFile() as f:
 p=subprocess.run([sys.executable,str(E/'scripts/audit_corners.py')]+[str(E/c) for c in cases],stdout=f,check=True);f.seek(0);a=json.load(f);assert a['pass']
print(json.dumps({'pass':True,'compressed_files':len(rows),'png_names_verified':pngs,'archives':len(archives),'strict_raw_baseline_and_final_sessions_replayed':True,'expanded_raw_staged':False}))
