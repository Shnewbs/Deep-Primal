#!/usr/bin/env python3
"""Extract and compile exact V8 materializer coordinate guard; no native API claim."""
from __future__ import annotations
import hashlib
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'src/main/java/com/shnewbs/deepprimal/engine/DensityChunkMaterializer.java'
TEST = ROOT / 'tools/tests/V8MaterializerCoordinateChecks.java'
ANCHOR = 'private static void requireRepresentableChunk(ChunkAccess chunk) {'
CALL = 'requireRepresentableChunk(chunk);'


def extract(source: str) -> str:
    if source.count(ANCHOR) != 1:
        raise RuntimeError('missing/duplicate production coordinate guard')
    start = source.index(ANCHOR)
    brace = source.index('{', start)
    depth = 0
    for index in range(brace, len(source)):
        if source[index] == '{': depth += 1
        elif source[index] == '}':
            depth -= 1
            if depth == 0:
                return source[start:index + 1]
    raise RuntimeError('unterminated production coordinate guard')


def static_guards(source: str) -> None:
    # Every direct V8 native write path must guard the actual ChunkPos before
    # reading its potentially overflowed getMinBlockX/getMinBlockZ coordinates.
    segments = (
        ('public static DensityChunk.Planned fillPlannedSurface(', 'var origin = chunk.getPos();'),
        ('private static DensityChunk.Planned fillPlannedRaw(', 'var origin = chunk.getPos();'),
        ('private static MaterializedEcotone fillSavedEcotoneInternal(', 'int minY = chunk.getMinY();'),
    )
    for signature, endpoint in segments:
        if source.count(signature) != 1:
            raise RuntimeError('ambiguous V8 native entry point: ' + signature)
        body = source[source.index(signature):]
        if endpoint not in body or CALL not in body[:body.index(endpoint)]:
            raise RuntimeError('missing pre-origin coordinate guard: ' + signature)
        if body.index(CALL) > body.index(endpoint):
            raise RuntimeError('late coordinate guard: ' + signature)


def run(method: str) -> tuple[int, str]:
    with tempfile.TemporaryDirectory(prefix='v8-materializer-coords-') as temp:
        folder = Path(temp)
        synthetic = folder / 'DensityChunkMaterializer.java'
        synthetic.write_text('package com.shnewbs.deepprimal.engine;\n'
            'import java.util.Objects;\n'
            'final class DensityChunkMaterializer {\n'
            '  static void check(ChunkAccess chunk) { requireRepresentableChunk(chunk); }\n'
            + method + '\n}\n', encoding='utf-8')
        (folder / 'ChunkAccess.java').write_text('package com.shnewbs.deepprimal.engine;\n'
            'final class ChunkAccess { private final ChunkPos pos; '
            'ChunkAccess(int x, int z) { pos = new ChunkPos(x,z); } '
            'ChunkPos getPos() { return pos; } }\n', encoding='utf-8')
        (folder / 'ChunkPos.java').write_text('package com.shnewbs.deepprimal.engine;\n'
            'final class ChunkPos { final int x,z; '
            'ChunkPos(int x,int z) { this.x=x;this.z=z; } }\n', encoding='utf-8')
        (folder / 'DensityChunk.java').write_text('package com.shnewbs.deepprimal.engine;\n'
            'final class DensityChunk { static final int WIDTH=16; }\n', encoding='utf-8')
        out = folder / 'classes'
        out.mkdir()
        compiled = subprocess.run(['javac', '--release', '21', '-Xlint:all', '-Werror',
            '-d', str(out), str(synthetic), str(folder / 'ChunkAccess.java'),
            str(folder / 'ChunkPos.java'), str(folder / 'DensityChunk.java'), str(TEST)],
            capture_output=True, text=True, timeout=90)
        if compiled.returncode:
            return compiled.returncode, compiled.stdout + compiled.stderr
        tested = subprocess.run(['java', '-ea', '-Xverify:all', '-cp', str(out),
            'com.shnewbs.deepprimal.engine.V8MaterializerCoordinateChecks'],
            capture_output=True, text=True, timeout=90)
        return tested.returncode, tested.stdout + tested.stderr


def main() -> None:
    source = SOURCE.read_text(encoding='utf-8')
    static_guards(source)
    method = extract(source)
    expected = 'V8_MATERIALIZER_COORDINATES_PASS assertions=130121'
    first, output = run(method)
    if first or expected not in output:
        raise RuntimeError('production failed: ' + output[-1500:])
    second, repeated = run(method)
    if second or repeated != output:
        raise RuntimeError('production replay failed: ' + repeated[-1500:])
    for label, expression in [('x', 'Math.multiplyExact(chunkX, DensityChunk.WIDTH)'),
                              ('z', 'Math.multiplyExact(chunkZ, DensityChunk.WIDTH)')]:
        if method.count(expression) != 1:
            raise RuntimeError('missing exact multiply: ' + label)
        mutant = method.replace(expression, 'chunk' + label.upper() + ' * DensityChunk.WIDTH')
        status, failure = run(mutant)
        if status == 0 or 'AssertionError' not in failure:
            raise RuntimeError('compiled ' + label + ' overflow mutant survived: ' + failure[-1000:])
    print('V8_MATERIALIZER_COORDINATES_PASS 2x130121 assertions; '
          '2/2 compiled overflow mutants rejected; Java21 extracted production method; '
          'source_sha256=' + hashlib.sha256(source.encode()).hexdigest())

if __name__ == '__main__':
    main()
