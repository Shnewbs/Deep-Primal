"""Recheck complete native acceptance, matching record files and ordered final shutdown."""
from pathlib import Path
import hashlib,json
ROOT=Path(__file__).resolve().parent
accepted=json.loads((ROOT/'fifo-safety.json').read_text())
names=['basin-limited-a','limited-transfer','limited-loaded-idle','basin-limited-b',
       'limited-packaged-filling','limited-packaged-transfer','limited-packaged-reload']
accepted += [json.loads((ROOT/(name+'.json')).read_text()) for name in names]
strict=[]
for record in accepted:
    name=record['case']; log=(ROOT/(name+'.log')).read_text()
    file=(ROOT/(name+'.txt')).read_text().strip()
    assert record['explicit_pass'] and not record['failures'] and not record['crash_reports'],name
    assert hashlib.sha256((ROOT/(name+'.log')).read_bytes()).hexdigest()==record['log_sha256'],name
    assert file and file in record['records'] and file in log,name
    passed=log.rfind(file); stop=log.find('Stopping server',passed)
    saved=log.find('All dimensions are saved',stop) if stop>=0 else -1
    built=log.find('BUILD SUCCESSFUL',saved) if saved>=0 else -1
    assert passed>=0 and stop>passed and saved>stop and built>saved,name
    strict.append({'case':name,'matching_file_and_stdout':True,'shutdown_and_save_after_pass':True,
                   'gradle_completion_after_save':True,'log_sha256':record['log_sha256']})
assert len(strict)==24
rejected=None
if (ROOT/'fifo-pressure-reopen.json').is_file():
    initial=json.loads((ROOT/'fifo-pressure-reopen.json').read_text())
    text=(ROOT/'fifo-pressure-reopen.log').read_text()
    rejected={'case':initial['case'],'status':'incomplete log capture; excluded from passes',
        'record_file_contains_pass':(ROOT/'fifo-pressure-reopen.txt').is_file(),
        'stdout_contains_pass':'LONG_PRESSURE_REOPEN_NATIVE_PASS' in text,
        'stdout_contains_final_stop':'Stopping server' in text,
        'stdout_contains_build_completion':'BUILD SUCCESSFUL' in text,
        'log_sha256':initial['log_sha256'],
        'cause':'Unproven. Record file alone and startup autosave do not establish accepted shutdown. Separate-process retry passes all stricter checks.'}
result={'accepted_current_candidate_cases':len(strict),'strict_checks':strict,'rejected_attempt':rejected}
(ROOT/'strict-native-audit.json').write_text(json.dumps(result,indent=2)+'\n')
(ROOT/'native-results.json').write_text(json.dumps(accepted,indent=2)+'\n')
print('STRICT_NATIVE_AUDIT_PASS cases='+str(len(strict)))
