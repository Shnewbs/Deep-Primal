"""Local-only dedicated-server smoke/benchmark harness. Requires a prepared dev runtime.

Run: python3 tools/server_smoke.py /path/to/gradle-executable --accept-eula
The caller must have accepted the Minecraft EULA. Uses isolated disposable test worlds.
"""
import argparse
import json
import os
from pathlib import Path
import socket
import struct
import subprocess
import time

ROOT = Path(__file__).resolve().parents[1]

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

def run(launcher, preset, suffix, port):
    work = ROOT / ('run-smoke-' + suffix)
    work.mkdir(exist_ok=True)
    # Only invoke this script after accepting the Minecraft EULA.
    (work / 'eula.txt').write_text('eula=true\n')
    (work / 'server.properties').write_text(
        f'level-type=deepprimal:{preset}\nlevel-seed=20261018\nlevel-name=world\n'
        'gamemode=creative\nforce-gamemode=true\nspawn-protection=0\n'
        'online-mode=false\nserver-ip=127.0.0.1\n'
        f'server-port={port}\nenable-rcon=true\nrcon.port={port + 1}\n'
        'rcon.password=deepprimal-local-smoke-only\nview-distance=3\nsimulation-distance=3\n'
        'max-players=2\nsync-chunk-writes=true\n')
    results = []
    expected = None
    for iteration in range(2):
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
                assert 'seed=20261018' in info, info
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
                    for repetition in range(2):
                        before = log.read_text(errors='replace').count('BENCH COMPLETE')
                        rcon.command('deepprimal bench 200 200 1')
                        deadline = time.monotonic() + 180
                        while log.read_text(errors='replace').count('BENCH COMPLETE') <= before:
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
                results.append({'iteration': iteration, 'info': info, 'samples': samples})
                rcon.command('save-all flush')
                try:
                    rcon.command('stop')
                except (OSError, RuntimeError):
                    pass
                rcon.sock.close()
                process.wait(timeout=90)
                assert process.returncode == 0, process.returncode
            finally:
                if process.poll() is None:
                    process.terminate()
                    try:
                        process.wait(timeout=20)
                    except subprocess.TimeoutExpired:
                        process.kill()
        (work / 'smoke-results.json').write_text(json.dumps(results, indent=2) + '\n')
    return work

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('launcher', nargs='+')
    parser.add_argument('--preset', default='prototype', choices=['prototype', 'prototype_deep'])
    parser.add_argument('--port', type=int, default=25567)
    parser.add_argument('--accept-eula', action='store_true')
    args = parser.parse_args()
    if not args.accept_eula:
        parser.error('Read the Minecraft EULA and pass --accept-eula before starting the server.')
    run(args.launcher, args.preset, args.preset, args.port)
