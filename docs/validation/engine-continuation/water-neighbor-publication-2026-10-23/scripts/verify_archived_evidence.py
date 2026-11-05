from pathlib import Path
import json,gzip,hashlib,zipfile,subprocess,sys,tempfile
E=Path(sys.argv[1])if len(sys.argv)>1 else Path(__file__).resolve().parents[1]
def sha(b):return hashlib.sha256(b).hexdigest()
rows=json.loads((E/'compressed-data-manifest.json').read_text())
for r in rows:
 z=(E/r['gzip_path']).read_bytes();b=gzip.decompress(z);assert len(z)==r['gzip_bytes']and sha(z)==r['gzip_sha256'];assert len(b)==r['bytes']and sha(b)==r['sha256']
archives=json.loads((E/'compact-pixel-archives.json').read_text());names=0
for r in archives:
 p=E/(r['case']+'-pixels.zip');assert sha(p.read_bytes())==r['sha256']
 with zipfile.ZipFile(p)as z:
  assert z.testzip()is None;index=json.loads(z.read('manifest.json'));assert len(index)==r['names']
  for n,h in index.items():assert sha(z.read('pixels/'+h+'.png'))==h;names+=1
cases=['vulkan-corners-neighbor-baseline','vulkan-corners-neighbor-candidate','vulkan-corners-neighbor-candidate-packaged','vulkan-reuse-gl-corners-neighbor-candidate']
with tempfile.TemporaryFile()as f:
 subprocess.run([sys.executable,str(E/'scripts/audit_neighbors.py')]+[str(E/c)for c in cases],stdout=f,check=True);f.seek(0);assert json.load(f)['pass']
assert json.loads((E/'performance-gate.json').read_text())['accepted']is False
print(json.dumps({'pass':True,'compressed_files':len(rows),'png_names_verified':names,'archives':len(archives),'baseline_and_all_candidate_native_replayed':True,'performance_rejection_retained':True}))
