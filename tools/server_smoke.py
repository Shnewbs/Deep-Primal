"""Local-only dedicated-server smoke/benchmark harness. Requires a prepared dev runtime.

Run: python3 tools/server_smoke.py /path/to/gradle-executable --accept-eula
The caller must have accepted the Minecraft EULA. Uses isolated disposable test worlds.
"""
import argparse
import json
import os
from pathlib import Path
import re
import socket
import struct
import subprocess
import time

ROOT = Path(__file__).resolve().parents[1]

def verify_server_shutdown(content, returncode, required_markers=()):
    """Verify the completed game session, not just its Gradle launcher exit code.

    Crash cleanup can save every dimension and still make Gradle exit zero. Failures
    therefore take precedence over both fixture PASS lines and shutdown messages.
    This is a log/lifecycle gate, not archive identity or gameplay acceptance.
    """
    if returncode != 0:
        raise RuntimeError(f'Server launcher exited {returncode}')
    failure = re.search(
        r'(?m)^.*(?:\[(?:Server thread|main)/(?:ERROR|FATAL)\]'
        r'|Exception in thread |Encountered an unexpected exception'
        r'|Preparing crash report|This crash report has been saved'
        r'|Failed to save|Error stopping the server).*$', content)
    if failure:
        raise RuntimeError('Native server failure: ' + failure.group(0).strip())
    cursor = -1
    for marker in ('RCON running on', 'Stopping server',
                   'ThreadedAnvilChunkStorage: All dimensions are saved', 'Closing FML Loader'):
        cursor = content.find(marker, cursor + 1)
        if cursor < 0:
            raise RuntimeError('Missing or out-of-order native lifecycle marker: ' + marker)
    for marker in required_markers:
        # Require a complete token; e.g. FOO_PASS must not accept FOO_PASS_PENDING.
        if not marker or not re.search(r'(?<![\w])' + re.escape(marker) + r'(?![\w])', content):
            raise RuntimeError('Missing required native fixture marker: ' + marker)

class Rcon:
    def __init__(self, port):
        self.sock = socket.create_connection(('127.0.0.1', port), timeout=20)
        self.seq = 0
        self.request(3, 'deepprimal-local-smoke-only')

    def read(self, count):
        data = b''
        while len(data) < count:
            part = self.sock.recv(count - len(data))
            if not part:
                raise RuntimeError('RCON disconnected')
            data += part
        return data

    def request(self, kind, text):
        self.seq += 1
        body = struct.pack('<ii', self.seq, kind) + text.encode() + b'\0\0'
        self.sock.sendall(struct.pack('<i', len(body)) + body)
        length = struct.unpack('<i', self.read(4))[0]
        response = self.read(length)
        request_id, _ = struct.unpack('<ii', response[:8])
        if request_id == -1:
            raise RuntimeError('RCON authentication failed')
        return response[8:-2].decode()

    def command(self, text):
        answer = self.request(2, text)
        print(text + ' => ' + answer, flush=True)
        return answer

def run(launcher, preset, suffix, port, radius=1, seed=20261018, batch_centers=((200, 200),), restarts=2, runtime_checks=False, required_markers=()):
    if restarts < 2:
        raise ValueError('At least two sessions are required to verify save/reopen')
    work = ROOT / ('run-smoke-' + suffix)
    work.mkdir(exist_ok=True)
    # An interrupted/failed rerun must not leave an earlier successful receipt behind.
    receipt = work / 'smoke-results.json'
    receipt.unlink(missing_ok=True)
    # Only invoke this script after accepting the Minecraft EULA.
    (work / 'eula.txt').write_text('eula=true\n')
    (work / 'server.properties').write_text(
        f'level-type=deepprimal:{preset}\nlevel-seed={seed}\nlevel-name=world\n'
        'gamemode=creative\nforce-gamemode=true\nspawn-protection=0\n'
        'online-mode=false\nserver-ip=127.0.0.1\n'
        f'server-port={port}\nenable-rcon=true\nrcon.port={port + 1}\n'
        'rcon.password=deepprimal-local-smoke-only\nview-distance=3\nsimulation-distance=3\n'
        'max-players=2\nsync-chunk-writes=true\n')
    config_dir = work / 'config'
    config_dir.mkdir(exist_ok=True)
    (config_dir / 'deepprimal-performance.toml').write_text('pauseAboveTickMilliseconds = 0\n')
    results = []
    expected = None
    for iteration in range(restarts):
        log = work / f'smoke-{iteration}.log'
        with log.open('w') as output:
            command = launcher + ['runServer', '--console=plain', '-PrunDir=' + str(work)]
            process = subprocess.Popen(command, cwd=ROOT, stdout=output, stderr=subprocess.STDOUT)
            try:
                deadline = time.monotonic() + 360
                while True:
                    content = log.read_text(errors='replace')
                    if 'RCON running on' in content:
                        break
                    if process.poll() is not None:
                        raise RuntimeError(f'Server exited {process.returncode}; inspect {log}')
                    if time.monotonic() > deadline:
                        raise TimeoutError(f'Server startup timed out; inspect {log}')
                    time.sleep(1)
                print(f'SERVER READY {suffix} iteration={iteration}', flush=True)
                rcon = Rcon(port + 1)
                info = rcon.command('deepprimal info')
                samples = [rcon.command(f'deepprimal sample {x} {z}')
                           for x, z in [(0, 0), (1024, 1024), (-1024, 2048), (8192, -4096)]]
                assert f'seed={seed}' in info, info
                assert all('preliminary surface=' in sample for sample in samples), samples
                if expected is not None:
                    assert samples == expected, 'Sampler changed after save/restart'
                expected = samples
                if iteration == 0:
                    survey = []
                    for z in range(-8192, 8193, 1024):
                        for x in range(-8192, 8193, 1024):
                            reply = rcon.request(2, f'deepprimal sample {x} {z}')
                            survey.append({'x': x, 'z': z, 'reply': reply})
                    (work / 'surface-survey.json').write_text(json.dumps(survey, indent=2) + '\n')
                    for repetition, (cx, cz) in enumerate([center for center in batch_centers for _ in range(2)]):
                        report_dir = work / 'deepprimal-benchmarks'
                        before = set(report_dir.glob('*.txt'))
                        rcon.command(f'deepprimal bench {cx} {cz} {radius}')
                        deadline = time.monotonic() + 180
                        while not (set(report_dir.glob('*.txt')) - before):
                            if 'Worldgen benchmark failed' in log.read_text(errors='replace'):
                                raise RuntimeError('Benchmark reported an engine error')
                            if process.poll() is not None or time.monotonic() > deadline:
                                raise RuntimeError('Benchmark did not complete')
                            time.sleep(1)
                        print(f'BENCHMARK FINISHED {suffix} repetition={repetition}', flush=True)
                    rcon.command('forceload add 3200 3200')
                    time.sleep(1)
                    floor = -384 if preset == 'prototype_deep' else -128
                    check = rcon.command(f'execute if block 3200 {floor} 3200 minecraft:bedrock')
                    assert 'Test passed' in check, check
                    rcon.command('forceload remove 3200 3200')
                if runtime_checks and iteration == 0:
                    # Verify bounded diagnostics and reload/cancel lifecycle through the live server.
                    before_perf = rcon.command('deepprimal perf')
                    assert 'benchmark=idle' in before_perf, before_perf
                    rcon.command('deepprimal sample 12345 -12345')
                    rcon.command('deepprimal sample 12345 -12345')
                    perf = rcon.command('deepprimal perf')
                    assert 'cacheHits=0 ' not in perf, perf
                    rcon.command('reload')
                    deadline = time.monotonic() + 30
                    while True:
                        perf = rcon.command('deepprimal perf')
                        if 'sampleCache=0/' in perf: break
                        if time.monotonic() > deadline: raise RuntimeError('Sampler cache did not clear on reload')
                        time.sleep(1)
                    rejected = rcon.command('deepprimal bench 400 400 5')
                    assert 'exceeds maxBenchmarkRadius' in rejected, rejected
                    rcon.command('deepprimal bench 400 400 4')
                    time.sleep(0.25)
                    rejected = rcon.command('deepprimal bench 500 500 0')
                    assert 'request is active' in rejected, rejected
                    rcon.command('deepprimal cancel')
                    perf = rcon.command('deepprimal perf')
                    assert 'benchmark=idle' in perf, perf
                    time.sleep(3)
                    assert 'No force loaded chunks were found' in rcon.command('forceload query')
                saved = rcon.command('save-all flush')
                if 'Saved the game' not in saved:
                    raise RuntimeError('Save flush did not acknowledge success: ' + saved)
                try:
                    rcon.command('stop')
                except (OSError, RuntimeError):
                    pass
                rcon.sock.close()
                process.wait(timeout=90)
                verify_server_shutdown(log.read_text(errors='replace'), process.returncode, required_markers)
                results.append({'iteration': iteration, 'info': info, 'samples': samples,
                                'native_shutdown_verified': True,
                                'required_markers': list(required_markers)})
            finally:
                if process.poll() is None:
                    process.terminate()
                    try:
                        process.wait(timeout=20)
                    except subprocess.TimeoutExpired:
                        process.kill()
    receipt.write_text(json.dumps(results, indent=2) + '\n')
    return work

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('launcher', nargs='+')
    parser.add_argument('--preset', default='prototype', choices=['prototype', 'prototype_deep'])
    parser.add_argument('--port', type=int, default=25567)
    parser.add_argument('--accept-eula', action='store_true')
    parser.add_argument('--radius', type=int, default=1)
    parser.add_argument('--seed', type=int, default=20261018)
    parser.add_argument('--suffix')
    parser.add_argument('--restarts', type=int, default=2)
    parser.add_argument('--comparison', action='store_true')
    parser.add_argument('--runtime-checks', action='store_true')
    parser.add_argument('--require-marker', action='append', default=[],
                        help='Native fixture PASS token required in every fresh/reopen session')
    args = parser.parse_args()
    if not args.accept_eula:
        parser.error('Read the Minecraft EULA and pass --accept-eula before starting the server.')
    centers = ((200, 200), (-240, 160), (320, -280)) if args.comparison else ((200, 200),)
    run(args.launcher, args.preset, args.suffix or args.preset, args.port, args.radius, args.seed, centers, args.restarts, args.runtime_checks, args.require_marker)
