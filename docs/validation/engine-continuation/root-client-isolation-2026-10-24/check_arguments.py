from pathlib import Path
import importlib.util, unittest.mock as mock, sys, io, contextlib, json, hashlib
r=Path(__file__).resolve().parent;p=r/'project/tools/build_with_current_proxy.py'
spec=importlib.util.spec_from_file_location('proxy_helper',p);m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
compile(p.read_text(),str(p),'exec')
records=[]
for options,tail,expected in [(['--root-game-task','runClient'],['--dry-run'],['--dry-run',':runClient']),(['--root-game-task','runServer'],['--dry-run'],['--dry-run',':runServer']),([],[':gui-themes:runClient'],[':gui-themes:runClient']),([],['runClient'],['runClient'])]:
 with mock.patch.object(sys,'argv',[str(p),'--java-home','/tmp/jdk','--gradle','/tmp/gradle',*options,'--',*tail]),mock.patch.object(m.subprocess,'run',return_value=mock.Mock(returncode=0)) as run,mock.patch.dict(m.os.environ,{},clear=True):
  assert m.main()==0;assert run.call_args.args[0]==['/tmp/gradle',*expected]
  records.append({'options':options,'input':tail,'output':expected,'pass':True})
for task in ['runClient',':runClient',':gui-themes:runClient',':runServer']:
 with mock.patch.object(sys,'argv',[str(p),'--java-home','/tmp/jdk','--gradle','/tmp/gradle','--root-game-task','runClient','--',task]),mock.patch.object(m.subprocess,'run') as run,mock.patch.dict(m.os.environ,{},clear=True),contextlib.redirect_stderr(io.StringIO()) as error:
  try:m.main();raise AssertionError('conflicting task accepted')
  except SystemExit as e:assert e.code==2
  assert not run.called and 'cannot be combined' in error.getvalue()
  records.append({'conflicting_selector':task,'rejected_before_gradle':True,'pass':True})
result={'pass':True,'helper_sha256':hashlib.sha256(p.read_bytes()).hexdigest(),'argument_controls':records,'new_gradle_or_native_jobs':0}
(r/'argument-checks.json').write_text(json.dumps(result,indent=2)+'\n');print(json.dumps(result))
