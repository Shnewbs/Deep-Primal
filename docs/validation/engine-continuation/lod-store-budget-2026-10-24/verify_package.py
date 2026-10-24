from pathlib import Path
import zipfile,subprocess,hashlib,json
r=Path(__file__).resolve().parent;j=r/'project/build/libs/DeepPrimal-26.1.2-0.0.1-alpha.27-vulkan-linux.jar';core=r/'packaged-core.jar';java=r/'toolchain/jdk-25.0.4.1+1/bin';out=r/'classes-candidate';e=r/'evidence'
with zipfile.ZipFile(j) as z:
 name,=[x for x in z.namelist() if '.DeepPrimal-Core-' in x and x.endswith('.jar')];core.write_bytes(z.read(name))
expected=json.loads((r/'independent-compile.json').read_text())['candidate']['com/shnewbs/deepprimal/core/lod/LodStore.class']
p=subprocess.run([str(java/'javac'),'-g','-cp',str(out),'-d',str(out),str(e/'PackagedLodBudget.java')],capture_output=True,text=True);assert p.returncode==0,p.stderr
before=hashlib.sha256(j.read_bytes()).hexdigest();cmd=[str(java/'java'),'-Xmx256m','-cp',str(core)+':'+str(out),'com.shnewbs.deepprimal.core.lod.PackagedLodBudget',expected]
p=subprocess.run(cmd,capture_output=True,text=True);(e/'packaged-result.txt').write_text(p.stdout+p.stderr);assert p.returncode==0,p.stdout+p.stderr
assert before==hashlib.sha256(j.read_bytes()).hexdigest()
(e/'packaged-check.json').write_text(json.dumps({'pass':True,'command':cmd,'root_sha256':before,'embedded_core_sha256':hashlib.sha256(core.read_bytes()).hexdigest(),'expected_loaded_class_sha256':expected,'exit':p.returncode,'raw_result_sha256':hashlib.sha256((e/'packaged-result.txt').read_bytes()).hexdigest()},indent=2)+'\n');print(p.stdout)
