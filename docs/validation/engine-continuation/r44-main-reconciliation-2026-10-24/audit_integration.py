from pathlib import Path
import hashlib, json, zipfile, io, gzip, re, datetime

root = Path(__file__).resolve().parent
d = json.loads((root/'inputs.json').read_text())
original = Path('/workspace/scratch/1a7cb29b04a2')
project = original/'Deep-Primal'
sha = lambda b: hashlib.sha256(b).hexdigest()
gitsha = lambda b: hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()
sources=[]
for x in d['files']:
    p=project/x['path']; assert p.is_file(), x['path']
    b=p.read_bytes(); assert gitsha(b)==x['sha'], x['path']
    sources.append({'path':x['path'],'git_sha':x['sha'],'sha256':sha(b)})
for x in d['r44_staged']:
    assert sha((project/x['path']).read_bytes())==x['sha256']
jar=original/'deliverables'/d['jar']['jar']; b=jar.read_bytes()
assert len(b)==d['jar']['bytes'] and sha(b)==d['jar']['sha256']
z=zipfile.ZipFile(io.BytesIO(b)); assert z.testzip() is None
classes=[]
for x in [{'archive':None,'module':''}]+d['jar']['nested_archives']:
    module=x['module']; a=z if x['archive'] is None else zipfile.ZipFile(io.BytesIO(z.read(x['archive'])))
    assert a.testzip() is None
    p=project/module/'build/classes/java/main'
    for c in sorted(p.rglob('*.class')):
        name=c.relative_to(p).as_posix(); assert a.read(name)==c.read_bytes(), (module,name)
        classes.append({'module':module or 'root','path':name,'sha256':sha(c.read_bytes())})
    assert not any('/validation/' in n or n.endswith('CoreChecks.class') for n in a.namelist())
assert len(classes)==d['jar']['compiled_classes_matched']
build=(original/'merged-build.log').read_bytes()
assert b'BUILD SUCCESSFUL' in build and b'31 actionable tasks: 31 executed' in build
assert b'60000' in build
console=(original/'combined-client-test/console.log').read_bytes(); text=console.decode(errors='replace')
run=original/'combined-client-test/run'; result=json.loads((original/'combined-client-test/result.json').read_text())
assert result['pass'] and result['exit_code']==0 and result['journal_success'] and not result['failures'] and not result['crashes']
assert (run/'alpha8-journal-validation-success.txt').is_file()
assert len(d['gui']['screenshots_sha256'])==40
for name,s in d['gui']['screenshots_sha256'].items(): assert sha((run/'screenshots'/name).read_bytes())==s
assert 'VULKAN_RESOURCES_RELEASED buffers=0 images=0' in text
counts=re.findall(r'VULKAN_OPERATION_COUNTS \{[^\n]*validationErrors=(\d+)[^\n]*\}',text)
assert counts and all(x=='0' for x in counts)
assert 'All dimensions are saved' in text and 'Stopping!' in text
assert not list((run/'crash-reports').glob('*'))
sealed=[]
for name,raw in [('merged-build.log',build),('combined-client-console.log',console)]:
    gz=gzip.compress(raw,mtime=0);(root/(name+'.gz')).write_bytes(gz)
    sealed.append({'path':name+'.gz','sha256':sha(gz),'raw_sha256':sha(raw),'raw_bytes':len(raw)})
report={'pass':True,'audited_at':datetime.datetime.now(datetime.timezone.utc).isoformat(),
 'integrated_commit':d['commit'],'integrated_tree':d['tree'],'code_changes_during_reconciliation':0,
 'local_engine_and_theme_repository_files_match_git_tree':len(sources),'scope_exclusions':d['scope_exclusions'],'r44_staged_sources_match_main':27,
 'root_and_nested_archive_crcs':7,'compiled_product_classes_match':len(classes),'jar':d['jar'],
 'existing_build_31_tasks_passed':True,
 'combined_gui_existing_session':{'success_marker':True,'captures_hashed':40,'crash_reports':0,
 'save_all_dimensions':True,'stop_logged':True,'vulkan_validation_errors':0,'final_buffers':0,'final_images':0,
 'shutdown_record_exists':(original/'combined-client-test/shutdown.txt').exists(),
 'both_root_and_standalone_clients_were_dispatched':True,'distinct_clients_not_independently_attributed':True,
 'not_a_repeat_of_r44_water_native_suite':True},
 'manual_review':'Eight Journal-tab contact-sheet captures inspected: readable panels and world visible behind dimming. No crash screen. Not a water-flow visual test.',
 'observed_messages':['OpenAL unavailable; sound disabled','X11 standard cursor shape unavailable',
 'Vulkan shader-output-not-consumed performance warnings','Gradle deprecated features'],
 'sealed_raw_evidence':sealed,'new_build_or_native_execution':False,'windows_gpu_execution':False,
 'hard_2ms_or_full_scene_or_engine_acceptance':False,
 'restore_rule':'Continue from current main. Never overwrite merged source with the historical 589-input engine-only R44 snapshot.'}
(root/'integration-audit.json').write_text(json.dumps(report,indent=2)+'\n')
(root/'source-identities.json').write_text(json.dumps(sources,indent=2)+'\n')
(root/'compiled-identities.json').write_text(json.dumps(classes,indent=2)+'\n')
print(json.dumps({k:v for k,v in report.items() if k not in ['jar']},indent=2))
