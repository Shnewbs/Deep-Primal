#!/usr/bin/env python3
"""Water-domain runner failure-path regressions; no Java installation required."""
import contextlib
import importlib.util
import io
from pathlib import Path
import sys
import tempfile
import unittest
from unittest.mock import patch


def load_runner():
    path = Path(__file__).with_name('test_v8_water_domain_admission.py')
    spec = importlib.util.spec_from_file_location('water_domain_runner', path)
    runner = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(runner)
    return runner


ASSERTION = ('Exception in thread "main" java.lang.AssertionError: protected water\n'
             '\tat WaterDomainSourceCheck.need(WaterDomainSourceCheck.java:5)\n')


def outcome(code=1, stderr=ASSERTION, compile_exit=0):
    return dict(compile_exit=compile_exit, test_exit=code, stdout='', stderr=stderr)


class WaterDomainRunnerFailureChecks(unittest.TestCase):
    def test_negative_requires_fixture_assertion(self):
        runner = load_runner()
        cases = [
            (outcome(), True),
            (outcome(stderr=ASSERTION.replace('Exception in thread "main" ', 'Caused by: ')), True),
            (outcome(code=0), False),
            (outcome(code=-9), False),
            (outcome(code=2), False),
            (outcome(compile_exit=1), False),
            (outcome(stderr='Error: Could not find or load main class AssertionError'), False),
            (outcome(stderr='Exception in thread "main" java.lang.NoClassDefFoundError: java/lang/AssertionError\n'), False),
            (outcome(stderr=ASSERTION.replace('WaterDomainSourceCheck.', 'OtherChecks.')), False),
            (outcome(stderr=ASSERTION.splitlines()[0]), False),
            (outcome(stderr='diagnostic mentions AssertionError'), False),
        ]
        for value, expected in cases:
            with self.subTest(value=value):
                self.assertIs(runner.rejected_by_fixture(value), expected)

    def test_compile_failure_never_launches(self):
        runner = load_runner()
        with tempfile.TemporaryDirectory() as tmp, patch.object(
                runner.subprocess, 'run',
                return_value=type('Failure', (), dict(returncode=1, stderr='synthetic javac failure'))()) as run:
            with self.assertRaisesRegex(RuntimeError, 'compilation failed'):
                runner.run(Path(tmp), 'class Unused {}')
            self.assertEqual(run.call_count, 1)

    def test_main_rejects_bad_mutants_without_success_receipt(self):
        runner = load_runner()
        # Use exact anchors consumed by main, independent of production Java availability.
        source = '\n'.join([
            ' || datum.kind() == WaterKind.OCEAN',
            'else if (topY < SEA_PLANE)',
            '|| !authority.savedDensity().equals(saved.density())',
        ])
        bads = [outcome(stderr='Error: Could not find or load main class AssertionError'),
                outcome(stderr=ASSERTION.replace('WaterDomainSourceCheck.', 'OtherChecks.')),
                outcome(code=-9), outcome(code=2)]
        for bad in bads:
            with self.subTest(bad=bad), tempfile.TemporaryDirectory() as tmp:
                input_path = Path(tmp) / 'Source.java'
                input_path.write_text(source)
                output_path = Path(tmp) / 'receipt.json'
                positive = outcome(code=0, stderr='')
                positive['stdout'] = 'V8_WATER_DOMAIN_SOURCE_PASS assertions=1'
                captured = io.StringIO()
                with patch.object(sys, 'argv', ['runner', '--source', str(input_path),
                                                '--output', str(output_path)]), patch.object(
                        runner, 'run', side_effect=[positive, positive] + [bad] * 3), contextlib.redirect_stdout(captured):
                    with self.assertRaisesRegex(RuntimeError, 'negative not rejected by intended fixture'):
                        runner.main()
                self.assertFalse(output_path.exists())
                self.assertNotIn('MUTANTS_REJECTED', captured.getvalue())


if __name__ == '__main__':
    unittest.main()
