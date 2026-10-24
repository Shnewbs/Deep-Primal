from pathlib import Path
import re, subprocess, json, hashlib, shutil, difflib
r=Path(__file__).resolve().parent
jdk=Path('/workspace/scratch/a22609fabda0/water-frame-1732/toolchain/jdk-25.0.4.1+1/bin')
records=[]
for role in ['baseline','candidate']:
    s=(r/role/'HydrologyRuntime.java').read_text()
    region=re.search(r'  private static final class RegionWork \{.*?\n  \}',s,re.S).group()
    constant=re.search(r'  private static final int REGIONAL_DISCHARGE_UNITS = 32;',s)
    reset=re.search(r'regional.tick = l.getGameTime\(\); regional.discharged = 0;',s).group().replace('l.getGameTime()','tick')
    guard=re.search(r'else if \((regional.discharged < [A-Z_0-9]+)\)',s).group(1)
    remaining=re.search(r'Math.min\(HydrologyConfig.SOURCE_RATE.get\(\), ([A-Z_0-9]+ - regional.discharged)\)',s).group(1)
    assert 'else if (w.regions.size() > w.chunks.size()) w.regions.clear();' in s
    out=r/role/'extracted';out.mkdir(exist_ok=True)
    for n in ['Aquifer','WaterAccount']:shutil.copyfile(r/'baseline'/f'{n}.java',out/f'{n}.java')
    code='''package com.shnewbs.deepprimal.core;
public final class RegionalBudgetChecks {
CONSTANT
REGION
  static int emit(Aquifer aquifer, RegionWork regional, long tick) {
    if (aquifer.step(tick)) { RESET }
    if (!(GUARD)) return 0;
    int accepted = Math.min(4, REMAINING);
    long before = aquifer.ground.water();
    aquifer.ground.take(accepted * WaterAccount.SCALE);
    regional.discharged += (int)((before - aquifer.ground.water()) / WaterAccount.SCALE);
    return accepted;
  }
  public static void main(String[] args) {
    int violations=0, maximum=0, cases=0;
    // Enumerate cache reconstruction schedules around fixed-step boundaries.
    for (int period=1; period<=41; period++) for(int phase=0; phase<period; phase++) {
      var aquifer=new Aquifer(new WaterAccount.Parcel(Aquifer.CAPACITY,0,0),WaterAccount.Parcel.EMPTY,30,true,0);
      var regional=new RegionWork(100); int window=0, total=0; long last=0;
      for(long tick=1; tick<=200; tick++) {
        if (tick%period==phase) regional=new RegionWork(100);
        long previousStep=aquifer.lastTick();
        int amount=emit(aquifer,regional,tick);
        if (aquifer.lastTick()!=previousStep) { window=0; last=aquifer.lastTick(); }
        window+=amount; total+=amount; maximum=Math.max(maximum,window);
        if(window>32) violations++;
        if(aquifer.ground.water()+total*256L!=Aquifer.CAPACITY) throw new AssertionError("volume conservation");
        cases++;
      }
    }
    // Without cache loss every completed interval still delivers the full allowance.
    var a=new Aquifer(new WaterAccount.Parcel(Aquifer.CAPACITY,0,0),WaterAccount.Parcel.EMPTY,30,true,0);
    var region=new RegionWork(100);int delivered=0;
    for(long tick=20;tick<40;tick++)delivered+=emit(a,region,tick);
    if(delivered!=32)throw new AssertionError("ordinary interval underdelivery");
    // Long offline gaps authorize one step and one allowance, never catch-up.
    var offline=new RegionWork(100);int once=0;
    for(int i=0;i<20;i++)once+=emit(a,offline,1000000);
    if(once!=32)throw new AssertionError("offline catch-up");
    System.out.println("REGIONAL_BUDGET_RESULT cases="+cases+" violations="+violations+" maximum="+maximum+" ordinary="+delivered+" offline="+once);
    if(args.length>0 && violations!=0)throw new AssertionError("cache churn exceeded shared allowance");
  }
}
'''.replace('CONSTANT',constant.group() if constant else '').replace('\nREGION\n','\n'+region+'\n').replace('RESET',reset).replace('GUARD',guard).replace('REMAINING',remaining)
    (out/'RegionalBudgetChecks.java').write_text(code)
    compiled=subprocess.run([str(jdk/'javac'),'-d',str(out/'classes'),*[str(p) for p in out.glob('*.java')]],capture_output=True)
    (r/(role+'-compile.txt')).write_bytes(compiled.stdout+compiled.stderr);assert compiled.returncode==0
    p=subprocess.run([str(jdk/'java'),'-cp',str(out/'classes'),'com.shnewbs.deepprimal.core.RegionalBudgetChecks','enforce'],capture_output=True)
    raw=p.stdout+p.stderr;(r/(role+'-result.txt')).write_bytes(raw)
    rec={'role':role,'exit_code':p.returncode,'source_sha256':hashlib.sha256(s.encode()).hexdigest(),'output':raw.decode(),'sha256':hashlib.sha256(raw).hexdigest()};records.append(rec)
assert records[0]['exit_code']!=0 and records[1]['exit_code']==0
(r/'results.json').write_text(json.dumps({'pass':True,'scope':'source-extracted RegionWork/reset/guard/remaining plus unmodified actual Aquifer/WaterAccount; not Minecraft native execution','records':records},indent=2)+'\n')
(r/'change.patch').write_text(''.join(difflib.unified_diff((r/'baseline/HydrologyRuntime.java').read_text().splitlines(True),(r/'candidate/HydrologyRuntime.java').read_text().splitlines(True),fromfile='a/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java',tofile='b/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java')))
print(json.dumps(records,indent=2))
