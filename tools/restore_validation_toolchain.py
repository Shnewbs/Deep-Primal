#!/usr/bin/env python3
"""Restore pinned private Linux x64 validation tools into a fresh directory.

Requires Python 3.12+. Optional development tooling, never a runtime dependency or mod payload.
Use the ordinary Gradle wrapper and installed JDK in a normal build environment.
"""
import argparse
from concurrent.futures import ThreadPoolExecutor
import hashlib
import json
from pathlib import Path
import tarfile
import time
import urllib.request
import zipfile

ASSETS = (
    ('jdk25.tar.gz', 'https://github.com/adoptium/temurin25-binaries/releases/download/jdk-25.0.4.1%2B1/OpenJDK25U-jdk_x64_linux_hotspot_25.0.4.1_1.tar.gz', 141329719, 'dbb698396d478e7fa2b1e50f4103324b2a99b90569ee27c33f2261f9215cf41e', 'jdk-25.0.4.1+1'),
    ('jdk21.tar.gz', 'https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.12.1%2B1/OpenJDK21U-jdk_x64_linux_hotspot_21.0.12.1_1.tar.gz', 207473347, 'ce79869e1307ed8ee1e2baa86a412b1eb5b75d10a01006d788a6f968bcfaee94', 'jdk-21.0.12.1+1'),
    ('gradle.zip', 'https://services.gradle.org/distributions/gradle-9.2.1-bin.zip', 135535598, '72f44c9f8ebcb1af43838f45ee5c4aa9c5444898b3468ab3f4af7b6076c5bc3f', 'gradle-9.2.1'),
    ('checked-agent/neoform-runtime-2.0.31-all.jar', 'https://maven.neoforged.net/releases/net/neoforged/neoform-runtime/2.0.31/neoform-runtime-2.0.31-all.jar', 4483539, '6f5b0aae7e423efeea21cb848c5096e76fc4eec40256cbefdcfaa1583b197c07', None),
    ('checked-agent/asm-9.9.1.jar', 'https://repo.maven.apache.org/maven2/org/ow2/asm/asm/9.9.1/asm-9.9.1.jar', 126252, '6f3828a215c920059a5efa2fb55c233d6c54ec5cadca99ce1b1bdd10077c7ddd', None),
)


def verified(path, size, digest):
    if path.stat().st_size != size:
        raise ValueError('Pinned size mismatch: ' + path.name)
    hashed = hashlib.sha256()
    with path.open('rb') as stream:
        while chunk := stream.read(1024 * 1024):
            hashed.update(chunk)
    if hashed.hexdigest() != digest:
        raise ValueError('Pinned SHA-256 mismatch: ' + path.name)
    return hashed.hexdigest()


def restore(output, asset, verify_only):
    name, url, size, digest, top = asset
    path = output / name
    started = time.monotonic()
    try:
        if not verify_only:
            path.parent.mkdir(parents=True, exist_ok=True)
            count = 0
            request = urllib.request.Request(url, headers={'User-Agent': 'DeepPrimal-validation-tools'})
            with urllib.request.urlopen(request, timeout=30) as response, path.open('xb') as target:
                while chunk := response.read(1024 * 1024):
                    count += len(chunk)
                    if count > size or time.monotonic() - started > 300:
                        raise ValueError('Download exceeds its pinned size or time budget')
                    target.write(chunk)
        verified(path, size, digest)
        if not verify_only and top:
            if name.endswith('.tar.gz'):
                with tarfile.open(path) as archive:
                    if any(member.name.split('/')[0] != top for member in archive.getmembers()):
                        raise ValueError('Unexpected archive root')
                    archive.extractall(output, filter='data')
            else:
                with zipfile.ZipFile(path) as archive:
                    for member in archive.namelist():
                        p = Path(member)
                        if p.is_absolute() or '..' in p.parts or p.parts[0] != top:
                            raise ValueError('Unexpected archive path')
                    if archive.testzip() is not None:
                        raise ValueError('ZIP CRC failure')
                    archive.extractall(output)
                (output / top / 'bin/gradle').chmod(0o755)
        return {'asset': name, 'bytes': size, 'sha256': digest, 'verified': True,
                'elapsed_seconds': round(time.monotonic() - started, 2)}
    except Exception as error:
        return {'asset': name, 'verified': False, 'error': str(error)}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output', type=Path, required=True)
    parser.add_argument('--verify-only', action='store_true', help='Check pinned archives already in --output; do not download or extract')
    args = parser.parse_args()
    output = args.output.resolve()
    if args.verify_only:
        if not output.is_dir():
            parser.error('--verify-only requires an existing tool directory')
    else:
        if output.exists():
            parser.error('--output already exists; choose a fresh directory or use --verify-only')
        output.mkdir(parents=True, exist_ok=False)  # Never overwrite another checkout/tool installation.
    with ThreadPoolExecutor(max_workers=2) as pool:
        records = list(pool.map(lambda asset: restore(output, asset, args.verify_only), ASSETS))
    result = {'scope': 'private-validation-tools', 'verify_only': args.verify_only,
              'all_verified': all(record['verified'] for record in records), 'assets': records}
    if not args.verify_only:
        (output / 'restoration.json').write_text(json.dumps(result, indent=2) + '\n')
    print(json.dumps(result, indent=2))
    return 0 if result['all_verified'] else 1


if __name__ == '__main__':
    raise SystemExit(main())
