#!/usr/bin/env python3
"""Java21 isolated API-double tests: not actual WatershedRegion compilation or Minecraft."""
import argparse
import json
import os
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
BRIDGE = ROOT / "engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedRegionPortBridge.java"
TOPOLOGY = ROOT / "engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedOutletTopology.java"
CHECKS = ROOT / "tools/tests/WatershedRegionPortBridgeChecks.java"
STUBS = ROOT / "tools/tests/port_bridge_stubs/com/shnewbs/deepprimal/core"

def run(source, path):
    path.mkdir(parents=True, exist_ok=True)
    cmd=[os.environ.get("JAVAC","javac"),"--release","21","-Xlint:all","-Werror",
         "-d",str(path),str(source),str(TOPOLOGY),str(CHECKS),
         *(str(p) for p in sorted(STUBS.glob("*.java")))]
    c=subprocess.run(cmd, capture_output=True, text=True, check=False)
    if c.returncode: return False,c.stdout+c.stderr
    r=subprocess.run([os.environ.get("JAVA","java"),"-ea","-Xverify:all",
                      "-cp",str(path),"WatershedRegionPortBridgeChecks"],
                     capture_output=True,text=True,check=False)
    return r.returncode==0 and "V8_REGION_PORT_BRIDGE_PASS" in r.stdout,r.stdout+r.stderr

def main():
    parser=argparse.ArgumentParser()
    parser.add_argument("--output", type=Path)
    opts=parser.parse_args()
    results=[]
    baseline=BRIDGE.read_text()
    changes=[
      ("lost_seam_height_guards",[
         ("Double.doubleToLongBits(floor) != Double.doubleToLongBits(n.ground())","false"),
         ("previous != null && Double.doubleToLongBits(previous) != Double.doubleToLongBits(sill)","false")]),
      ("lost_frontier_unknown",[("s.copies != expectedCopies","false")]),
      ("invented_runoff",[("s.x, s.z, s.floor, 0L,","s.x, s.z, s.floor, 1024L,")])
    ]
    with tempfile.TemporaryDirectory(prefix="dp-v8-port-bridge-") as tmp:
        tmp=Path(tmp)
        for i in range(2):
            okay,detail=run(BRIDGE,tmp/("positive"+str(i)))
            if not okay: raise SystemExit("positive test failed: "+detail)
            results.append({"name":"positive"+str(i),"passed":True,"details":detail.strip()})
        for name,replacements in changes:
            broken=baseline
            for old,new in replacements:
                if broken.count(old)!=1: raise SystemExit("mutation target not unique: "+name)
                broken=broken.replace(old,new)
            target=tmp/name/"WatershedRegionPortBridge.java"
            target.parent.mkdir(parents=True)
            target.write_text(broken)
            okay,detail=run(target,tmp/(name+"-classes"))
            if okay: raise SystemExit("broken implementation passed: "+name)
            results.append({"name":name,"rejected":True,"error_excerpt":detail[-500:]})
    out=opts.output
    if out:
        out.mkdir(parents=True,exist_ok=True)
        (out/"port-bridge-results.json").write_text(json.dumps(results,indent=2)+"\n")
    print("V8_REGION_PORT_BRIDGE_RUNNER_PASS positive=2 broken_rejected=3")

if __name__=="__main__": main()
