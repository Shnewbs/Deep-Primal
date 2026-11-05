from pathlib import Path
import subprocess,json
R=Path(__file__).resolve().parent;P=R/'project';out=R/'initial-core-repro';out.mkdir(exist_ok=True)
core=P/'engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterFlowTransition.java';before=core.read_text();(out/'WaterFlowTransition.java').write_text(before)
test='''import com.shnewbs.deepprimal.core.WaterFlowTransition;
public class EarlyRetarget { public static void main(String[] args) {
 var t=new WaterFlowTransition(0,0);t.target(4,25,1);
 if(t.source()!=0||t.target()!=4||!t.active(25)||t.age(25)!=0)throw new AssertionError("Idle first retarget delayed: target="+t.target()+" active="+t.active(25));
 System.out.println("EARLY_RETARGET_PASS millis=25 source=0 target=4 age=0");
}}'''
(out/'EarlyRetarget.java').write_text(test)
jdk=R/'toolchain/jdk-25.0.4.1+1/bin'
def run():
 c=subprocess.run([str(jdk/'javac'),'-d',str(out),str(out/'WaterFlowTransition.java'),str(out/'EarlyRetarget.java')],text=True,capture_output=True);assert c.returncode==0,c.stderr
 r=subprocess.run([str(jdk/'java'),'-cp',str(out),'EarlyRetarget'],text=True,capture_output=True)
 return {'exit_code':r.returncode,'stdout':r.stdout,'stderr':r.stderr}
a=run();assert a['exit_code']!=0 and 'Idle first retarget delayed' in a['stderr']
after=before.replace('} else if (now - started >= MILLIS && next != target) {','} else if ((from == target || now - started >= MILLIS) && next != target) {')
assert after!=before;core.write_text(after);(out/'WaterFlowTransition.java').write_text(after);b=run();assert b['exit_code']==0
(R/'initial-transition-reproduction.json').write_text(json.dumps({'before':a,'after':b,'before_source':before,'test_source':test,'scope':'Candidate-only edge;not parent production defect'},indent=2)+'\n')
p=P/'src/coreChecks/java/com/shnewbs/deepprimal/core/WaterFlowBlendChecks.java';s=p.read_text().replace('int cases=0;','var early=new WaterFlowTransition(0,0);early.target(4,25,1);\n    if(early.source()!=0||early.target()!=4||!early.active(25)||early.age(25)!=0)throw new AssertionError("early idle retarget");\n    int cases=0;').replace('pairs="+cases+" repeatedUpload', 'pairs="+cases+" earlyFirstRetarget=true repeatedUpload');p.write_text(s)
