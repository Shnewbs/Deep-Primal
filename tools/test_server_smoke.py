"""Dependency-free regression tests; no game process or network is started."""
import contextlib
import io
import json
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

import server_smoke as smoke

GOOD = '''[Server thread/INFO] RCON running on 127.0.0.1:25568
TERRAIN_AUDIT_NATIVE_PASS blocks=10
[Server thread/INFO] Stopping server
[Server thread/INFO] ThreadedAnvilChunkStorage: All dimensions are saved
[main/INFO] Closing FML Loader abc
BUILD SUCCESSFUL
'''


def simulate(module, logs, saved='Saved the game', codes=None):
    """Exercise the real run() receipt path with a controlled completed launcher."""
    codes = codes or [0] * len(logs)
    sessions = iter(zip(logs, codes))

    class Process:
        def __init__(self, *args, stdout, **kwargs):
            self.output = stdout
            self.tail, self.code = next(sessions)
            self.returncode = None
            stdout.write('RCON running on 127.0.0.1\n')
            stdout.flush()

        def poll(self):
            return self.returncode

        def wait(self, timeout):
            self.output.write(self.tail)
            self.output.flush()
            self.returncode = self.code
            return self.code

        def terminate(self):
            self.returncode = -15

    class Rcon:
        def __init__(self, port):
            self.sock = self

        def close(self):
            pass

        def request(self, kind, text):
            return 'preliminary surface=64 ' + text

        def command(self, text):
            if text == 'deepprimal info':
                return 'seed=20261018'
            if text.startswith('deepprimal sample'):
                return self.request(2, text)
            if text.startswith('execute if block'):
                return 'Test passed'
            if text == 'save-all flush':
                return saved
            return 'ok'

    with tempfile.TemporaryDirectory() as tmp:
        work = Path(tmp) / 'run-smoke-test'
        work.mkdir()
        receipt = work / 'smoke-results.json'
        receipt.write_text('[{"old": true}]')
        with patch.object(module, 'ROOT', Path(tmp)), patch.object(module, 'Rcon', Rcon), \
                patch.object(module.subprocess, 'Popen', Process), \
                patch.object(module.time, 'sleep'), contextlib.redirect_stdout(io.StringIO()):
            try:
                module.run(['unused'], 'prototype', 'test', 25567, batch_centers=(), restarts=2)
            except (RuntimeError, AssertionError) as error:
                return str(error), json.loads(receipt.read_text()) if receipt.exists() else None
        return None, json.loads(receipt.read_text())


class ShutdownChecks(unittest.TestCase):
    def test_clean_completed_session(self):
        smoke.verify_server_shutdown(GOOD, 0, ['TERRAIN_AUDIT_NATIVE_PASS'])

    def test_crash_wins_over_success_and_cleanup(self):
        for failure in ('[Server thread/ERROR] Encountered an unexpected exception',
                        '[Server thread/FATAL] failed', '[main/ERROR] failed',
                        'Exception in thread "Server thread" java.lang.AssertionError',
                        'Preparing crash report with UUID abc',
                        'This crash report has been saved to: crash.txt',
                        'Failed to save chunk', 'Error stopping the server'):
            for content in (failure + '\n' + GOOD, GOOD + '\n' + failure):
                with self.subTest(failure=failure, after=content.startswith(GOOD)):
                    with self.assertRaisesRegex(RuntimeError, 'Native server failure'):
                        smoke.verify_server_shutdown(content, 0)

    def test_nonzero_and_unfinished_process(self):
        for code in (1, -9, None):
            with self.subTest(code=code), self.assertRaises(RuntimeError):
                smoke.verify_server_shutdown(GOOD, code)

    def test_each_lifecycle_phase_required(self):
        for marker in ('RCON running on', 'Stopping server',
                       'ThreadedAnvilChunkStorage: All dimensions are saved', 'Closing FML Loader'):
            with self.subTest(marker=marker), self.assertRaises(RuntimeError):
                smoke.verify_server_shutdown(GOOD.replace(marker, 'omitted'), 0)

    def test_flush_before_stop_does_not_prove_shutdown_save(self):
        with self.assertRaises(RuntimeError):
            smoke.verify_server_shutdown('RCON running on\n'
                'ThreadedAnvilChunkStorage: All dimensions are saved\n'
                'Stopping server\nClosing FML Loader\nBUILD SUCCESSFUL', 0)

    def test_required_fixture_token_is_not_a_prefix(self):
        for suffix in ('_PENDING', 'ING', ''):
            with self.subTest(suffix=suffix), self.assertRaises(RuntimeError):
                smoke.verify_server_shutdown(GOOD.replace('TERRAIN_AUDIT_NATIVE_PASS',
                    'TERRAIN_AUDIT_NATIVE_PASS' + suffix if suffix else 'missing'),
                    0, ['TERRAIN_AUDIT_NATIVE_PASS'])

    def test_nonfatal_remote_key_failure_is_not_server_failure(self):
        smoke.verify_server_shutdown('[Yggdrasil Key Fetcher/ERROR] Failed to request yggdrasil public key\n' + GOOD, 0)

    def test_clean_fresh_reopen_receipt(self):
        error, receipt = simulate(smoke, [GOOD, GOOD])
        self.assertIsNone(error)
        self.assertEqual(2, len(receipt))
        self.assertTrue(all(x['native_shutdown_verified'] for x in receipt))

    def test_crash_never_leaves_old_or_partial_success_receipt(self):
        bad = GOOD + '[Server thread/FATAL] Preparing crash report\n'
        for logs in ([bad, GOOD], [GOOD, bad]):
            with self.subTest(logs=logs):
                error, receipt = simulate(smoke, logs)
                self.assertIn('Native server failure', error)
                self.assertIsNone(receipt)

    def test_unacknowledged_flush_rejects_receipt(self):
        error, receipt = simulate(smoke, [GOOD, GOOD], saved='Failed to save the game')
        self.assertIn('Save flush did not acknowledge', error)
        self.assertIsNone(receipt)


if __name__ == '__main__':
    unittest.main()
