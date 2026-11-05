from pathlib import Path
import zipfile,io,hashlib,json,shutil
R=Path(__file__).resolve().parent;P=R/'baseline';O=Path('/workspace/scratch/98befbcef095/deepprimal-preview')
win=P/'build/libs/DeepPrimal-26.1.2-0.0.1-alpha.25-dev-vulkan-windows.jar'
linux=R.parent/'water-boundary-retry-2230/project/build/libs/DeepPrimal-26.1.2-0.0.1-alpha.25-dev-vulkan-linux.jar'
assert json.loads((R/'windows-preview/packaging-result.json').read_text())['pass']
def sha(b):return hashlib.sha256(b).hexdigest()
def inspect(p):
 result={};archives=0;classes=0;natives=[]
 with zipfile.ZipFile(p)as z:
  assert z.testzip()is None;archives+=1
  for n in z.namelist():
   b=z.read(n)
   if n.endswith('.jar'):
    with zipfile.ZipFile(io.BytesIO(b))as m:
     assert m.testzip()is None;archives+=1
     for k in m.namelist():
      if k.endswith(('.dll','.so','.dylib')):natives.append(k)
      if k.endswith('.class'):result[n.split('/')[-1].split('-0.0.1-')[0]+'/'+k]=sha(m.read(k));classes+=1
   elif n.endswith('.class'):result['root/'+n]=sha(b);classes+=1
  assert not any('validation/'in n or 'coreChecks/'in n for n in z.namelist())
 return result,archives,classes,natives
w,wc,wn,dlls=inspect(win);l,lc,ln,solibs=inspect(linux)
assert w==l,(len(w),len(l),[x for x in w if w.get(x)!=l.get(x)][:10])
assert dlls and all(x.endswith('.dll')for x in dlls)and not any(x.endswith('.dll')for x in solibs)
# Top-level compiled product classes and every nested module/class match the tested R43 Linux archive.
out=O/'DeepPrimal-26.1.2-0.0.1-alpha.25-dev-R43-vulkan-windows.jar';shutil.copyfile(win,out)
record={'pass':True,'product_checkpoint':'136892bc3b060fb12d75f063d3e92d40a7f45bac','build':'experimental R43 Windows preview; internal mod version0.0.1-alpha.25-dev','r44_changes_included':False,'sha256':sha(out.read_bytes()),'bytes':out.stat().st_size,'crc_archives':wc,'same_compiled_classes_as_tested_R43_linux':wn,'all_compiled_class_bytes_equal':True,'windows_native_files':dlls,'linux_reference_sha256':sha(linux.read_bytes()),'windows_native_execution':False,'performance_promotion':False}
(R/'windows-preview/archive-audit.json').write_text(json.dumps(record,indent=2)+'\n');print(json.dumps(record,indent=2))
