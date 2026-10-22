"""Read saved closed-tube fixture blocks without Minecraft or loading any chunks."""
import argparse
import hashlib
import json
from pathlib import Path
import struct
import zlib


class Nbt:
    def __init__(self, data):
        self.data, self.i = data, 0

    def read(self, n):
        if n < 0 or self.i + n > len(self.data):
            raise ValueError('Truncated NBT')
        value = self.data[self.i:self.i+n]
        self.i += n
        return value

    def number(self, fmt):
        return struct.unpack('>' + fmt, self.read(struct.calcsize(fmt)))[0]

    def string(self):
        return self.read(self.number('H')).decode('utf-8')

    def tag(self, kind, depth=0):
        if depth > 64:
            raise ValueError('NBT depth')
        if kind in range(1, 7):
            return self.number({1:'b',2:'h',3:'i',4:'q',5:'f',6:'d'}[kind])
        if kind == 7:
            return self.read(self.number('i'))
        if kind == 8:
            return self.string()
        if kind == 9:
            element, size = self.number('B'), self.number('i')
            if not 0 <= size <= 2000000:
                raise ValueError('NBT list size')
            return [self.tag(element, depth+1) for _ in range(size)]
        if kind == 10:
            value = {}
            while (child := self.number('B')):
                name = self.string()
                value[name] = self.tag(child, depth+1)
            return value
        if kind in (11, 12):
            size = self.number('i')
            if not 0 <= size <= 2000000:
                raise ValueError('NBT array size')
            return [self.number('i' if kind == 11 else 'q') for _ in range(size)]
        raise ValueError('Unknown NBT tag ' + str(kind))


class Save:
    def __init__(self, world):
        self.region = world/'dimensions/minecraft/the_end/region'
        self.chunks, self.hashes = {}, {}

    def chunk(self, cx, cz):
        key = (cx, cz)
        if key in self.chunks:
            return self.chunks[key]
        path = self.region/f'r.{cx>>5}.{cz>>5}.mca'
        raw = path.read_bytes()
        self.hashes[path.name] = hashlib.sha256(raw).hexdigest()
        i = 4 * ((cx & 31) + (cz & 31) * 32)
        offset = int.from_bytes(raw[i:i+3], 'big') * 4096
        if not offset:
            raise ValueError(f'Absent saved chunk {key}')
        size = int.from_bytes(raw[offset:offset+4], 'big')
        compressed = raw[offset+5:offset+4+size]
        compression = raw[offset+4]
        if compression != 2:
            raise ValueError(f'Unsupported region compression {compression}')
        nbt = Nbt(zlib.decompress(compressed))
        kind = nbt.number('B')
        nbt.string()
        value = nbt.tag(kind)
        self.chunks[key] = value
        return value

    def block(self, x, y, z):
        chunk = self.chunk(x >> 4, z >> 4)
        section = next(s for s in chunk['sections'] if s['Y'] == y >> 4)
        states = section['block_states']
        palette = states['palette']
        if len(palette) == 1:
            return palette[0]
        bits = max(4, (len(palette)-1).bit_length())
        per_word = 64 // bits
        i = (y & 15) * 256 + (z & 15) * 16 + (x & 15)
        word = states['data'][i // per_word] & ((1 << 64)-1)
        return palette[(word >> ((i % per_word) * bits)) & ((1 << bits)-1)]


def inspect(world):
    save = Save(world)
    scenes = []
    for offset, length in [(4096, 257), (8192, 16)]:
        cells = [(offset+x,200,8) for x in range(length+1)]
        cells += [(offset+length,199,8), (offset+length,198,8)]
        water = []
        for p in cells:
            state = save.block(*p)
            if state['Name'] == 'deepprimal:engine_water':
                water.append({'position': p, 'properties': state.get('Properties', {})})
            elif 'deepprimal' in state['Name']:
                raise ValueError('Unexpected mod block ' + str(state))
        scenes.append({'offset': offset, 'water': water})
    return {'world':str(world), 'saved_state_only':True, 'scenes':scenes, 'region_sha256':save.hashes}


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('world', type=Path)
    args = parser.parse_args()
    print(json.dumps(inspect(args.world), indent=2))
