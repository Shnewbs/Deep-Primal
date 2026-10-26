"""Run from the repository root; pass a JDK25 bin directory and an empty output directory."""
import argparse
from pathlib import Path
import subprocess

parser = argparse.ArgumentParser()
parser.add_argument('jdk_bin', type=Path)
parser.add_argument('output', type=Path)
args = parser.parse_args()
evidence = Path(__file__).resolve().parent
root = Path.cwd()
if args.output.exists():
    parser.error('Output must not already exist; preserve prior receipts')
args.output.mkdir(parents=True)
sources = []
for directory in ['engine-core/src/main/java', 'src/coreChecks/java']:
    for source in (root / directory).rglob('*.java'):
        staged = evidence / 'staged' / source.relative_to(root)
        sources.append(str(staged if staged.exists() else source))
sources.append(str(evidence / 'SplitAdmissionChecks.java'))
subprocess.run([str(args.jdk_bin / 'javac'), '-g', '-d', str(args.output), *sources], check=True)
for name in ['SplitAdmissionChecks', 'CoreChecks']:
    subprocess.run([str(args.jdk_bin / 'java'), '-Xmx768m', '-Xverify:all', '-cp', str(args.output),
                    'com.shnewbs.deepprimal.core.' + name], check=True, timeout=600)
