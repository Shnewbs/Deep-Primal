#!/usr/bin/env python3
"""Non-shipping LWJGL 3.4.1 microprobe; Linux x86-64, Java 25, prebuilt engine-core."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import platform
import subprocess
import urllib.request

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--java-home', default=os.environ.get('JAVA_HOME'))
parser.add_argument('--output', type=Path)
args = parser.parse_args()
if platform.system() != 'Linux' or platform.machine().lower() not in ('x86_64', 'amd64'):
    parser.error('This standalone probe currently supports Linux x86-64 only.')
if not args.java_home:
    parser.error('Supply --java-home pointing to a Java 25 JDK.')
repo = Path(__file__).resolve().parents[2]
java_bin = Path(args.java_home) / 'bin'
version = next(line.split('=', 1)[1].strip() for line in (repo / 'gradle.properties').read_text().splitlines() if line.startswith('mod_version='))
core = repo / 'engine-core/build/libs' / f'DeepPrimal-Core-{version}.jar'
if not core.exists():
    parser.error('Build engine-core first: ./gradlew :engine-core:jar')
work = repo / 'build/lod-probe'
deps = work / 'deps'
deps.mkdir(parents=True, exist_ok=True)
jars = []
checksums = {}
for artifact in ('lwjgl', 'lwjgl-meshoptimizer'):
    for suffix in ('', '-natives-linux'):
        name = f'{artifact}-3.4.1{suffix}.jar'
        destination = deps / name
        if not destination.exists():
            url = f'https://repo.maven.apache.org/maven2/org/lwjgl/{artifact}/3.4.1/{name}'
            with urllib.request.urlopen(url, timeout=30) as response:
                data = response.read()
            pending = destination.with_suffix('.pending')
            pending.write_bytes(data)
            pending.replace(destination)
        checksums[name] = hashlib.sha256(destination.read_bytes()).hexdigest()
        jars.append(destination)
classes = work / 'classes'
classes.mkdir(exist_ok=True)
classpath = os.pathsep.join(map(str, [core, *jars]))
subprocess.run([str(java_bin / 'javac'), '-cp', classpath, '-d', str(classes), str(repo / 'tools/lod/MeshOptimizerProbe.java')], check=True)
result = subprocess.run([str(java_bin / 'java'), '--enable-native-access=ALL-UNNAMED', '-cp', classpath + os.pathsep + str(classes), 'MeshOptimizerProbe'], check=True, capture_output=True, text=True)
data = json.loads(result.stdout)
data['dependencySha256'] = checksums
data['scope'] = 'Synthetic proxy meshes; CPU-only microprobe, not Minecraft FPS or detailed voxel LOD certification.'
output = args.output or work / 'result.json'
output.parent.mkdir(parents=True, exist_ok=True)
output.write_text(json.dumps(data, indent=2) + '\n')
print(json.dumps(data, indent=2))
