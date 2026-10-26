"""Offline regression checks for private validation-tool recovery and cleanup."""
import hashlib, io, tarfile, tempfile, unittest, zipfile
from pathlib import Path
from unittest.mock import patch
import restore_validation_toolchain as r

class RestoreTests(unittest.TestCase):

    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.root = Path(self.tmp.name)
        self.cache = self.root / 'cache'
        self.cache.mkdir()
        self.out = self.root / 'out'
        self.out.mkdir()

    def asset(self, name='tool.zip', member='tool/bin/java'):
        p = self.cache / name
        with zipfile.ZipFile(p, 'w') as z:
            z.writestr(member, b'payload')
        return (name, 'https://invalid.example/no-network', p.stat().st_size, hashlib.sha256(p.read_bytes()).hexdigest(), 'tool')

    def test_cache_copy_without_network(self):
        a = self.asset()
        with zipfile.ZipFile(self.cache / a[0], 'a') as z:
            z.writestr('tool/bin/gradle', b'tool')
        p = self.cache / a[0]
        a = (a[0], a[1], p.stat().st_size, hashlib.sha256(p.read_bytes()).hexdigest(), a[4])
        with patch.object(r.urllib.request, 'urlopen', side_effect=AssertionError('network')):
            result = r.restore(self.out, a, False, self.cache, True)
        self.assertTrue(result['verified'])
        self.assertTrue(result['archive_removed'])
        self.assertFalse((self.out / a[0]).exists())
        self.assertEqual(b'payload', (self.out / 'tool/bin/java').read_bytes())
        self.assertEqual(a[3], hashlib.sha256(p.read_bytes()).hexdigest())

    def test_corrupt_cache_rejected_without_copy(self):
        a = self.asset()
        (self.cache / a[0]).write_bytes(b'wrong')
        self.assertFalse(r.restore(self.out, a, False, self.cache, True)['verified'])
        self.assertFalse((self.out / a[0]).exists())

    def test_existing_output_not_overwritten(self):
        a = self.asset()
        p = self.out / a[0]
        p.write_bytes(b'keep')
        self.assertFalse(r.restore(self.out, a, False, self.cache, True)['verified'])
        self.assertEqual(b'keep', p.read_bytes())

    def test_traversal_retained_but_not_extracted(self):
        a = self.asset(member='../escape')
        self.assertFalse(r.restore(self.out, a, False, self.cache, True)['verified'])
        self.assertTrue((self.out / a[0]).exists())
        self.assertFalse((self.root / 'escape').exists())

    def test_dependency_jar_not_removed(self):
        p = self.cache / 'agent.jar'
        p.write_bytes(b'agent')
        a = ('agent.jar', 'unused', 5, hashlib.sha256(b'agent').hexdigest(), None)
        result = r.restore(self.out, a, False, self.cache, True)
        self.assertTrue(result['verified'])
        self.assertFalse(result['archive_removed'])
        self.assertEqual(b'agent', (self.out / 'agent.jar').read_bytes())

    def test_missing_cache_no_network(self):
        with patch.object(r.urllib.request, 'urlopen', side_effect=AssertionError('network')):
            self.assertFalse(r.restore(self.out, ('absent', 'unused', 1, '0', None), False, self.cache, True)['verified'])

    def test_growing_source_copy_bounded(self):
        target = io.BytesIO()
        with self.assertRaises(ValueError):
            r.copy_bounded(io.BytesIO(b'oversize'), target, 2, r.time.monotonic())
        self.assertEqual(b'', target.getvalue())

    def test_copy_deadline(self):
        target = io.BytesIO()
        with self.assertRaises(ValueError):
            r.copy_bounded(io.BytesIO(b'x'), target, 1, r.time.monotonic() - 301)
        self.assertEqual(b'', target.getvalue())

    def test_tar_cache_cleanup(self):
        p = self.cache / 'tool.tar.gz'
        with tarfile.open(p, 'w:gz') as archive:
            member = tarfile.TarInfo('tool/bin/java')
            member.size = 7
            archive.addfile(member, io.BytesIO(b'payload'))
        a = (p.name, 'unused', p.stat().st_size, hashlib.sha256(p.read_bytes()).hexdigest(), 'tool')
        result = r.restore(self.out, a, False, self.cache, True)
        self.assertTrue(result['verified'])
        self.assertTrue(result['archive_removed'])
        self.assertEqual(b'payload', (self.out / 'tool/bin/java').read_bytes())
        self.assertTrue(p.exists())
    def test_same_size_corruption_rejected(self):
        a = self.asset()
        p = self.cache / a[0]
        p.write_bytes(b'x' * a[2])
        result = r.restore(self.out, a, False, self.cache, True)
        self.assertFalse(result['verified'])
        self.assertIn('SHA-256', result['error'])
        self.assertFalse((self.out / a[0]).exists())

    def test_copied_archive_reverified_before_extraction(self):
        a = self.asset()
        def corrupt_copy(source, target, size, started):
            target.write(b'x' * size)
        with patch.object(r, 'copy_bounded', side_effect=corrupt_copy):
            result = r.restore(self.out, a, False, self.cache, True)
        self.assertFalse(result['verified'])
        self.assertFalse((self.out / 'tool').exists())
        self.assertTrue((self.out / a[0]).exists())

if __name__ == '__main__':
    unittest.main()
