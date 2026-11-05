#!/usr/bin/env python3
"""Run the staged renderer byte-accounting checks without sharing mutable outputs."""
import argparse, hashlib, json, pathlib, subprocess, tempfile
def sha(data): return hashlib.sha256(data).hexdigest()
def blob(data): return hashlib.sha1(b"blob "+str(len(data)).encode()+b"\0"+data).hexdigest()
def main():
 p=argparse.ArgumentParser()
 for name in ("source-root","candidate","check-source","manifest","java-home","output-root"):
  p.add_argument("--"+name,required=True,type=pathlib.Path)
 p.add_argument("--candidate-sha256",required=True);p.add_argument("--check-sha256",required=True)
 a=p.parse_args(); root=a.source_root.resolve(); manifest=json.loads(a.manifest.read_text())
 inputs=[];sources=[]
 for entry in manifest["tree"]:
  if entry["path"].startswith("src/main/java/") and entry["path"].endswith(".java"):
   path=(root/"engine-core"/entry["path"]).resolve()
   if not path.is_relative_to(root): raise ValueError("Escaped source root")
   data=path.read_bytes()
   if blob(data)!=entry["sha"]: raise ValueError("Source identity mismatch: "+str(path))
   inputs.append((path,sha(data)))
   if path.name!="LodMesher.java": sources.append(str(path))
 for entry in manifest["fixtures"]:
  path=(root/entry["path"]).resolve()
  if not path.is_relative_to(root): raise ValueError("Escaped fixture root")
  data=path.read_bytes()
  if blob(data)!=entry["sha"]:raise ValueError("Fixture identity mismatch: "+str(path))
  inputs.append((path,sha(data)));sources.append(str(path))
 candidate=a.candidate.read_bytes();check=a.check_source.read_bytes()
 if sha(candidate)!=a.candidate_sha256:raise ValueError("Candidate identity mismatch")
 if sha(check)!=a.check_sha256:raise ValueError("Private check identity mismatch")
 original=root/"engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/LodMesher.java"
 work=pathlib.Path(tempfile.mkdtemp(prefix="renderer-byte-check-",dir=a.output_root.resolve()))
 classes=work/"classes";classes.mkdir()
 staged=work/"LodMesher.java";staged.write_bytes(candidate)
 baseline=work/"BaselineLodMesher.java"
 baseline.write_text(original.read_text().replace("LodMesher","BaselineLodMesher"))
 private=work/"ByteAccountingChecks.java";private.write_bytes(check)
 sources += [str(staged),str(baseline),str(private)]
 commands=[[str(a.java_home/"bin/javac"),"-g","-sourcepath",str(work/"no-sourcepath"),"-d",str(classes)]+sources,
 [str(a.java_home/"bin/java"),"-Xmx512m","-Xverify:all","-cp",str(classes),
 "com.shnewbs.deepprimal.core.lod.ByteAccountingChecks"]]
 result={"pass":False,"work":str(work),"candidate_sha256":sha(candidate),
 "check_sha256":sha(check),"core_sources":len(inputs)-len(manifest["fixtures"]),
 "fixture_sources":len(manifest["fixtures"]),"commands":commands,
 "full_gradle_build":False,"packaged_artifact":False,"gpu_validation":False}
 try:
  for i,cmd in enumerate(commands):
   with (work/("compile.txt" if i==0 else "checks.txt")).open("w") as log:
    rc=subprocess.run(cmd,stdout=log,stderr=subprocess.STDOUT).returncode
   if rc:raise RuntimeError("Command "+str(i)+" exited "+str(rc))
  if "BYTE_ACCOUNTING_PASS" not in (work/"checks.txt").read_text():raise RuntimeError("Missing acceptance marker")
  for path,expected in inputs:
   if sha(path.read_bytes())!=expected:raise RuntimeError("Input changed during checks: "+str(path))
  if sha(a.candidate.read_bytes())!=sha(candidate) or sha(a.check_source.read_bytes())!=sha(check):
   raise RuntimeError("Candidate/check changed during checks")
  result["pass"]=True
 finally:
  (work/"result.json").write_text(json.dumps(result,indent=2))
  print(json.dumps(result))
if __name__=="__main__":main()
