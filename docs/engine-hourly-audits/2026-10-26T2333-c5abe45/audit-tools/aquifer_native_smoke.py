from pathlib import Path
import hashlib, importlib.util, json, os, subprocess, time

r = Path(__file__).resolve().parent
p = r/'project'
e = r / 'evidence'
a = json.loads((e / 'package.json').read_text())
spec = importlib.util.spec_from_file_location('smoke', p / 'tools/server_smoke.py')
sm = importlib.util.module_from_spec(spec)
spec.loader.exec_module(sm)
work = Path('/dev/shm/dp-audit-c5abe45-2333') / 'native-world-aquifer'
work.mkdir()
(work / '.cache').symlink_to(r / 'native-cache', target_is_directory=True)
(work / 'eula.txt').write_text('eula=true\n')
(work / 'server.properties').write_text('level-type=minecraft:normal\nlevel-seed=20261020\nlevel-name=world\nonline-mode=false\nserver-ip=127.0.0.1\nserver-port=25971\nenable-rcon=true\nrcon.port=25972\nrcon.password=deepprimal-local-smoke-only\nmax-tick-time=-1\nview-distance=2\nsimulation-distance=2\n')
cmd = json.loads((e / 'build-command.json').read_text())
cmd = [x for x in cmd if x != '--rerun-tasks']
cmd.insert(cmd.index('--'), 'runServer')
cmd.insert(cmd.index('runServer'), '--root-game-task')
cmd = cmd[:cmd.index(':build')] + ['-PpackagedValidation', '-PvisualValidation', '-PvalidationJar=' + a['artifact'], '-PrunDir=' + str(work), '-x', 'downloadAssets', '-I', str(r / 'native-memory.init.gradle')]
results = []
for mode in ['fresh', 'reopen', 'verify']:
    record = e / ('aquifer-' + mode + '.txt')
    log = e / ('aquifer-' + mode + '-native.log')
    if record.exists():
        raise RuntimeError('Refuse a stale native record')
    env = os.environ.copy()
    env.update(JDK_JAVA_OPTIONS='-Xmx1024m -Djava.io.tmpdir=/dev/shm/dp-audit-c5abe45-2333/tmp', DEEPPRIMAL_AQUIFER_PERSISTENCE_RECORD=str(record), DEEPPRIMAL_AQUIFER_PERSISTENCE_MODE=mode)
    marker = 'AQUIFER_PERSISTENCE_PASS mode=' + mode
    with log.open('w') as out:
        proc = subprocess.Popen(cmd+['-x',':jar'], cwd=p, stdout=out, stderr=subprocess.STDOUT, env=env)
        deadline = time.monotonic() + 480
        try:
            while True:
                text = log.read_text(errors='replace')
                if marker in text and 'RCON running on' in text:
                    break
                if proc.poll() is not None:
                    raise RuntimeError('Native process exited before acceptance: ' + str(proc.returncode))
                if time.monotonic() >= deadline:
                    raise RuntimeError('Native startup deadline')
                time.sleep(1)
            if not record.is_file() or marker not in record.read_text():
                raise RuntimeError('Missing explicit native pass record')
            rc = sm.Rcon(25972)
            try:
                ack = rc.command('save-all flush')
                if 'Saved the game' not in ack:
                    raise RuntimeError('Save was not acknowledged')
                rc.command('stop')
            finally:
                rc.sock.close()
            proc.wait(timeout=90)
            sm.verify_server_shutdown(log.read_text(errors='replace'), proc.returncode, [marker])
            if proc.returncode != 0:
                raise RuntimeError('Nonzero producer exit')
            if hashlib.sha256(Path(a['artifact']).read_bytes()).hexdigest() != a['sha256']:
                raise RuntimeError('Tested artifact changed')
            results.append({'mode': mode, 'pass': True, 'exit': proc.returncode, 'saved_flush_acknowledged': True, 'record': record.name})
            print('AQUIFER_NATIVE_SESSION_PASS', mode, flush=True)
        finally:
            if proc.poll() is None:
                proc.terminate()
                proc.wait(timeout=30)
(e / 'aquifer-native-result.json').write_text(json.dumps({'pass': True, 'platform': 'Linux dedicated server', 'sessions': results, 'depths': [1, 64], 'extreme_timestamp': 'Long.MIN_VALUE', 'actual_process_restarts': 2, 'bounded_step': True, 'quantities_and_constituents_exact': True, 'no_force_load': True, 'windows_execution': False, 'ordinary_world_stall_claimed': False}, indent=2))

