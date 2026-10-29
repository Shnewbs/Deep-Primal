#!/usr/bin/env python3
"""Replay the isolated V8 routing adapter suite from its recorded evidence ZIP.

This is NOT a full NeoForge/Java25/native watershed test. Source identities
are checked against the checkout before running the frozen Java21 API doubles.
"""
from __future__ import annotations
import argparse
from pathlib import Path
import subprocess
import sys
import tempfile
import zipfile

RELATIVE_SOURCES = {
    'src/main/java/com/shnewbs/deepprimal/engine/V8RoutedWaterSource.java':
        'source/com/shnewbs/deepprimal/engine/V8RoutedWaterSource.java',
    'tools/tests/V8RoutedWaterSourceChecks.java':
        'tests/V8RoutedWaterSourceChecks.java',
    'src/main/java/com/shnewbs/deepprimal/engine/V8WaterDomainAdmission.java':
        'source/com/shnewbs/deepprimal/engine/V8WaterDomainAdmission.java',
    'src/main/java/com/shnewbs/deepprimal/engine/V8GeneratorSnapshot.java':
        'source/com/shnewbs/deepprimal/engine/V8GeneratorSnapshot.java',
}


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--bundle', type=Path, required=True,
        help='Dev68 source-only evidence ZIP (not an executable or Minecraft JAR)')
    parser.add_argument('--repo', type=Path, default=Path(__file__).resolve().parents[1],
        help='Git checkout root to match against the frozen fixture')
    ns = parser.parse_args()
    with tempfile.TemporaryDirectory(prefix='v8-routed-source-') as tmp:
        root = Path(tmp)
        with zipfile.ZipFile(ns.bundle) as archive:
            bad = archive.testzip()
            if bad:
                raise ValueError(f'evidence archive CRC failed: {bad}')
            for entry in archive.infolist():
                dest = (root / entry.filename).resolve()
                if not dest.is_relative_to(root) or entry.filename.startswith('/'):
                    raise ValueError('unsafe archive entry')
            archive.extractall(root)
        for real, fixture in RELATIVE_SOURCES.items():
            real_file = ns.repo / real
            fixture_file = root / fixture
            if not real_file.is_file() or not fixture_file.is_file():
                raise FileNotFoundError(f'cannot verify source identity: {real}')
            if real_file.read_bytes() != fixture_file.read_bytes():
                raise ValueError(f'fixture does not match checked-out source: {real}')
        for script in ('run_focused.py', 'mutation_checks.py'):
            completed = subprocess.run([sys.executable, str(root / script)],
                cwd=root, check=False, timeout=300)
            if completed.returncode:
                return completed.returncode
    print('V8_ROUTED_EVIDENCE_REPLAY_PASS sourceIdentities=4 '
          'java21ApiDoubles=true realWatershed=false nativeMinecraft=false')
    return 0

if __name__ == '__main__':
    sys.exit(main())
