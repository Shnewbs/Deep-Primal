"""Inspect public GPU interfaces in local official client JARs; does not redistribute game code."""
import argparse
import hashlib
import json
from pathlib import Path
import subprocess
from zipfile import ZipFile


def inspect(path, javap):
    path = Path(path)
    with ZipFile(path) as jar:
        names = set(jar.namelist())
    candidates = ['com.mojang.blaze3d.systems.GpuDeviceBackend', 'com.mojang.renderpearl.api.device.GpuDevice']
    interface = next(name for name in candidates if name.replace('.', '/') + '.class' in names)
    result = subprocess.run([javap, '-cp', str(path), interface], check=True, capture_output=True, text=True)
    methods = sorted(line.strip() for line in result.stdout.splitlines() if line.strip().startswith('public abstract'))
    vulkan_classes = sum('/vulkan/' in name and name.endswith('.class') for name in names)
    return {'jar': path.name, 'sha256': hashlib.sha256(path.read_bytes()).hexdigest(),
            'device_interface': interface, 'vulkan_class_count': vulkan_classes, 'public_methods': methods}


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('baseline')
    parser.add_argument('candidates', nargs='+')
    parser.add_argument('--javap', default='javap')
    args = parser.parse_args()
    baseline = inspect(args.baseline, args.javap)
    comparisons = []
    for path in args.candidates:
        candidate = inspect(path, args.javap)
        candidate['added_methods'] = sorted(set(candidate['public_methods']) - set(baseline['public_methods']))
        candidate['removed_methods'] = sorted(set(baseline['public_methods']) - set(candidate['public_methods']))
        comparisons.append(candidate)
    print(json.dumps({'baseline': baseline, 'candidates': comparisons}, indent=2))
