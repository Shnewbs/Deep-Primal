from pathlib import Path
import json,subprocess,hashlib
R=Path(__file__).resolve().parent;P=R/'project';D=R/'core-reproduction';D.mkdir(exist_ok=True)
f=P/'engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterFlowTransition.java';source=f.read_text();(D/'WaterFlowTransition.java').write_text(source)
test='''import com.shnewbs.deepprimal.core.WaterFlowTransition;
public class MaterialCoreRepro { public static void main(String[] args) {
 int bad=0;for(int d=0;d<16;d++)for(int flip=0;flip<2;flip++){
 int a=flip==0?16:d,b=flip==0?d:16;var t=new WaterFlowTransition(a,0);t.target(b,25,1);
 if(t.source()!=a||t.target()!=b||!t.active(25)||t.age(25)!=0)bad++;
 }
 System.out.println("MATERIAL_CORE_RESULT routes=32 bad="+bad);
 if(bad!=0)throw new AssertionError("Still/flow endpoints must crossfade");
}}'''
(D/'MaterialCoreRepro.java').write_text(test);jdk=R/'toolchain/jdk-25.0.4.1+1/bin'
subprocess.run([str(jdk/'javac'),'-d',str(D),str(D/'WaterFlowTransition.java'),str(D/'MaterialCoreRepro.java')],check=True)
r=subprocess.run([str(jdk/'java'),'-cp',str(D),'MaterialCoreRepro'],text=True,capture_output=True)
row={'source_sha256':hashlib.sha256(source.encode()).hexdigest(),'exit_code':r.returncode,'stdout':r.stdout,'stderr':r.stderr,'test_source':test}
name='core-before.json' if r.returncode else 'core-after.json';(R/name).write_text(json.dumps(row,indent=2)+'\n');print(json.dumps(row))
