#!/usr/bin/env python3
"""Compile the actual V8 water-domain admission class against small Java API doubles.

This is a Java21 component/source regression, NOT a real Mojang/NeoForge/V8
routing, GPU, persistence, water-volume, or native Minecraft test.
"""
import argparse
import hashlib
import json
import re
import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'src/main/java/com/shnewbs/deepprimal/engine/V8WaterDomainAdmission.java'
STUBS = {
    'com/shnewbs/deepprimal/core/biome/EcotoneBlend.java': '''package com.shnewbs.deepprimal.core.biome;
public final class EcotoneBlend {
  public enum Domain { LAND, RIVER, OCEAN }
  public record Climate(int altitude, double temp) {}
}
''',
    'com/shnewbs/deepprimal/core/density/DensityGraph.java': '''package com.shnewbs.deepprimal.core.density;
public final class DensityGraph { public record Definition(String name) {} }
''',
    'com/shnewbs/deepprimal/engine/V8ClimateBinding.java': '''package com.shnewbs.deepprimal.engine;
public record V8ClimateBinding(String name) {}
''',
    'com/shnewbs/deepprimal/engine/V8SurfacePolicy.java': '''package com.shnewbs.deepprimal.engine;
public record V8SurfacePolicy(String value) {
  public static final V8SurfacePolicy FROZEN_R1=new V8SurfacePolicy("r1");
}
''',
    'com/shnewbs/deepprimal/engine/V8HydrologyBinding.java': '''package com.shnewbs.deepprimal.engine;
public record V8HydrologyBinding(int version, int maskInterfaceRevision, String sourceFingerprint) {}
''',
    'com/shnewbs/deepprimal/engine/V8GeneratorSnapshot.java': '''package com.shnewbs.deepprimal.engine;
import com.shnewbs.deepprimal.core.density.DensityGraph;
public record V8GeneratorSnapshot(int version, long seed, DensityGraph.Definition density,
   V8SurfacePolicy surfacePolicy, V8ClimateBinding climateBinding,
   V8HydrologyBinding hydrologyBinding) {
  public V8GeneratorSnapshot(int version, long seed, DensityGraph.Definition density,
      V8SurfacePolicy surfacePolicy, V8ClimateBinding climateBinding) {
    this(version, seed, density, surfacePolicy, climateBinding, null);
  }
  public static final int CLIMATE_VERSION=4;
  public static final int HYDROLOGY_VERSION=5;
  public static final int VEGETATION_VERSION=6;
  public record Prepared(V8GeneratorSnapshot snapshot) {}
  public Prepared prepare(){return new Prepared(this);}
}
''',
    'com/shnewbs/deepprimal/engine/V8EcotoneColumnPlan.java': '''package com.shnewbs.deepprimal.engine;
import com.shnewbs.deepprimal.core.biome.EcotoneBlend;
public final class V8EcotoneColumnPlan {
  public interface ClimateSource {ClimateDatum sample(int x,int topY,int z);}
  public record ClimateDatum(EcotoneBlend.Climate climate, EcotoneBlend.Domain domain) {}
}
''',
    'WaterDomainSourceCheck.java': '''import com.shnewbs.deepprimal.engine.*;
import com.shnewbs.deepprimal.core.biome.EcotoneBlend;
import com.shnewbs.deepprimal.core.density.DensityGraph;
public final class WaterDomainSourceCheck {
  static long assertions, masked, dry;
  static void need(boolean ok,String what){assertions++;if(!ok)throw new AssertionError(what);}
  static void deny(Runnable call,String name){boolean threw=false;
    try{call.run();}catch(IllegalArgumentException|IllegalStateException|NullPointerException ex){threw=true;}
    need(threw,name);
  }
  public static void main(String[] args) {
    var graph=new DensityGraph.Definition("terrain-r4");var climate=new V8ClimateBinding("saved");
    var snapshot=new V8GeneratorSnapshot(4,-29,graph,V8SurfacePolicy.FROZEN_R1,climate);
    V8EcotoneColumnPlan.ClimateSource reference=(x,y,z)->new V8EcotoneColumnPlan.ClimateDatum(
      new EcotoneBlend.Climate(y,.4),Math.floorMod(z,7)==0?EcotoneBlend.Domain.OCEAN:
        Math.floorMod(x,5)==0?EcotoneBlend.Domain.RIVER:EcotoneBlend.Domain.LAND);
    V8WaterDomainAdmission.WaterSource observer=(x,z)->
      Math.floorMod(x,17)==0?V8WaterDomainAdmission.WaterDatum.ocean():
      Math.floorMod(z,13)==0?new V8WaterDomainAdmission.WaterDatum(V8WaterDomainAdmission.WaterKind.LAKE,99):
      Math.floorMod(x,11)==0?new V8WaterDomainAdmission.WaterDatum(V8WaterDomainAdmission.WaterKind.RIVER,98):
      V8WaterDomainAdmission.WaterDatum.dry();
    var authority=new V8WaterDomainAdmission.Authority(snapshot.seed(),graph,climate,1,observer);
    var admission=V8WaterDomainAdmission.bind(snapshot.prepare(),reference,authority);
    String sourceFingerprint="a".repeat(64);
    var hydrated=new V8GeneratorSnapshot(5,-29,graph,V8SurfacePolicy.FROZEN_R1,climate,
        new V8HydrologyBinding(1,1,sourceFingerprint));
    var routedAuthority=new V8WaterDomainAdmission.Authority(-29,graph,climate,1,sourceFingerprint,observer);
    var routed=V8WaterDomainAdmission.bind(hydrated.prepare(),reference,routedAuthority);
    var vegetated=new V8GeneratorSnapshot(6,-29,graph,V8SurfacePolicy.FROZEN_R1,climate,
        new V8HydrologyBinding(1,1,sourceFingerprint));
    var routed6=V8WaterDomainAdmission.bind(vegetated.prepare(),reference,routedAuthority);
    deny(()->V8WaterDomainAdmission.bind(hydrated.prepare(),reference,authority),"missing routed identity");
    deny(()->V8WaterDomainAdmission.bind(snapshot.prepare(),reference,routedAuthority),"false revision4 identity");
    deny(()->V8WaterDomainAdmission.bind(hydrated.prepare(),reference,
      new V8WaterDomainAdmission.Authority(-29,graph,climate,1,"b".repeat(64),observer)),"mismatched routed identity");
    deny(()->V8WaterDomainAdmission.bind(snapshot.prepare(),reference,
      new V8WaterDomainAdmission.Authority(snapshot.seed()+1,graph,climate,1,observer)),"wrong seed");
    deny(()->V8WaterDomainAdmission.bind(snapshot.prepare(),reference,
      new V8WaterDomainAdmission.Authority(snapshot.seed(),new DensityGraph.Definition("other"),climate,1,observer)),"wrong graph");
    deny(()->V8WaterDomainAdmission.bind(snapshot.prepare(),reference,
      new V8WaterDomainAdmission.Authority(snapshot.seed(),graph,new V8ClimateBinding("other"),1,observer)),"wrong climate");
    deny(()->V8WaterDomainAdmission.bind(new V8GeneratorSnapshot(3,-29,graph,V8SurfacePolicy.FROZEN_R1,
      climate).prepare(),reference,authority),"old profile");
    deny(()->new V8WaterDomainAdmission.Authority(-29,graph,climate,2,observer),"unsaved authority version");
    deny(()->new V8WaterDomainAdmission.WaterDatum(V8WaterDomainAdmission.WaterKind.OCEAN,95),"ocean stage");
    deny(()->new V8WaterDomainAdmission.WaterDatum(V8WaterDomainAdmission.WaterKind.LAKE,Double.NaN),"missing lake stage");
    deny(()->new V8WaterDomainAdmission.WaterDatum(V8WaterDomainAdmission.WaterKind.DRY,99),"dry stage");
    long checksum=0;
    for(int top=92;top<=101;top++)for(int z=-48;z<=47;z++)for(int x=-48;x<=47;x++){
      var actual=admission.sample(x,top,z).domain();
      need(routed.sample(x,top,z).domain()==actual,"r5 saved observer parity");
      need(routed6.sample(x,top,z).domain()==actual,"r6 saved observer parity");
      boolean shouldBeDry=top>=96&&Math.floorMod(z,7)!=0&&Math.floorMod(x,5)!=0
        && Math.floorMod(x,17)!=0&&Math.floorMod(z,13)!=0&&Math.floorMod(x,11)!=0;
      need((actual==EcotoneBlend.Domain.LAND)==shouldBeDry,"protected water/sea side "+x+","+z+","+top);
      checksum=checksum*0x100000001b3L+actual.ordinal();
      if(shouldBeDry)dry++;else masked++;
    }
    need(dry>0&&masked>0,"must exercise both sides");
    System.out.println("V8_WATER_DOMAIN_SOURCE_PASS assertions="+assertions+" masked="+masked+
      " dry="+dry+" checksum="+Long.toUnsignedString(checksum,16)+" apiDoubles=true native=false");
  }
}
''',
}


def run(case_dir, source):
    files = []
    for path, body in STUBS.items():
        target = case_dir / path
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(body, encoding='utf-8')
        files.append(str(target))
    actual = case_dir / 'com/shnewbs/deepprimal/engine/V8WaterDomainAdmission.java'
    actual.parent.mkdir(parents=True, exist_ok=True)
    actual.write_text(source, encoding='utf-8')
    files.append(str(actual))
    compiled = case_dir / 'classes'
    compiled.mkdir()
    compile_result = subprocess.run(['javac', '--release', '21', '-Xlint:all', '-Werror',
                                     '-d', str(compiled), *files], capture_output=True,
                                    text=True, timeout=45)
    if compile_result.returncode:
        raise RuntimeError('source/API-double compilation failed: ' + compile_result.stderr[:1300])
    outcome = subprocess.run(['java', '-ea', '-Xverify:all', '-cp', str(compiled),
                              'WaterDomainSourceCheck'], capture_output=True,
                             text=True, timeout=45)
    return {'compile_exit': 0, 'test_exit': outcome.returncode,
            'stdout': outcome.stdout.strip(), 'stderr': outcome.stderr.strip()[:1200]}


def rejected_by_fixture(outcome):
    """A broken variant must compile and fail in this fixture, not the launcher."""
    return (outcome['compile_exit'] == 0 and outcome['test_exit'] == 1
            and re.search(r'^(?:Exception in thread "[^\"]+" |Caused by: )java\.lang\.AssertionError(?::|$)',
                          outcome['stderr'], re.MULTILINE) is not None
            and '\tat WaterDomainSourceCheck.' in outcome['stderr'])


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--source', type=Path, default=SOURCE)
    parser.add_argument('--output', type=Path)
    args = parser.parse_args()
    source = args.source.read_text(encoding='utf-8')
    mutants = {
        'ignore_new_ocean': (' || datum.kind() == WaterKind.OCEAN', ''),
        'ignore_undersea': ('else if (topY < SEA_PLANE)', 'else if (false)'),
        'ignore_graph_identity': ('|| !authority.savedDensity().equals(saved.density())', ''),
    }
    for name, (needle, _) in mutants.items():
        if source.count(needle) != 1:
            raise RuntimeError('mutation anchor unavailable/ambiguous: ' + name)
    with tempfile.TemporaryDirectory(prefix='dp-v8-water-admission-') as temp:
        work = Path(temp)
        positives = [run(work / ('positive-' + str(i)), source) for i in range(2)]
        negatives = {name: run(work / name, source.replace(needle, replacement))
                     for name, (needle, replacement) in mutants.items()}
    if any(p['test_exit'] for p in positives) or positives[0]['stdout'] != positives[1]['stdout']:
        raise RuntimeError('positive source checks failed or fingerprint drifted')
    if not all(rejected_by_fixture(n) for n in negatives.values()):
        raise RuntimeError('negative not rejected by intended fixture: ' + repr(negatives))
    results = {'toolchain': subprocess.run(['java', '-version'], capture_output=True,
                                          text=True).stderr.splitlines()[0],
               'backend': 'exact source class / Java21 isolated API doubles, not native Minecraft',
               'source_sha256': hashlib.sha256(source.encode()).hexdigest(),
               'positive': positives, 'compiled_negative_controls': negatives}
    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(json.dumps(results, indent=2) + '\n', encoding='utf-8')
    print(positives[0]['stdout'])
    print('V8_WATER_DOMAIN_MUTANTS_REJECTED compiled=3')


if __name__ == '__main__':
    main()
