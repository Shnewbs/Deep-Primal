from pathlib import Path
import hashlib,json,zipfile,io
r=Path(__file__).resolve().parent;p=r/'project';out=r/'release';out.mkdir(exist_ok=True)
win=p/'build/libs/DeepPrimal-26.1.2-0.0.1-alpha.27-vulkan-windows.jar';linux=p/'build/libs/DeepPrimal-26.1.2-0.0.1-alpha.27-vulkan-linux.jar'
classes=0;checks=0;mods={}
with zipfile.ZipFile(win) as w,zipfile.ZipFile(linux) as l:
 assert w.testzip() is None and l.testzip() is None
 for n in w.namelist():
  assert '/validation/' not in n,n
  if n.endswith('.class'):assert w.read(n)==l.read(n),n;classes+=1
  if n.endswith('.jar'):
   raw=w.read(n)
   with zipfile.ZipFile(io.BytesIO(raw)) as m:
    assert m.testzip() is None;checks+=1
    natives=[x for x in m.namelist() if x.endswith(('.so','.dll','.dylib'))]
    if 'Vulkan' in n:
     assert any(x.endswith('.dll') for x in natives) and not any(x.endswith(('.so','.dylib')) for x in natives),natives
     ln=next(x for x in l.namelist() if x.endswith('.jar') and 'Vulkan' in x)
     with zipfile.ZipFile(io.BytesIO(l.read(ln))) as lm:
      for name in m.namelist():
       if name.endswith('.class'):assert m.read(name)==lm.read(name),name;classes+=1
    else:assert raw==l.read(n),n
    mods[n]={'sha256':hashlib.sha256(raw).hexdigest(),'bytes':len(raw),'native_files':natives}
 dest=out/win.name;dest.write_bytes(win.read_bytes())
 with zipfile.ZipFile(dest) as final:assert final.testzip() is None
record={'filename':dest.name,'bytes':dest.stat().st_size,'sha256':hashlib.sha256(dest.read_bytes()).hexdigest(),'outer_crc':True,'nested_crc_archives':checks,'linux_windows_identical_root_and_vulkan_classes':classes,'all_other_modules_byte_identical':True,'modules':mods,'windows_runtime_tested':False}
(out/'artifact.json').write_text(json.dumps(record,indent=2)+'\n');print({k:v for k,v in record.items() if k!='modules'})
