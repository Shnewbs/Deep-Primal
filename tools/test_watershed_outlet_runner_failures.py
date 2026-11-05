#!/usr/bin/env python3
"""Exercise the topology runner's real control flow with simulated tool failures."""
import contextlib
import importlib.util
import io
from pathlib import Path
import subprocess
import tempfile
import unittest
from unittest.mock import patch

SPEC = importlib.util.spec_from_file_location(
    'outlet_runner', Path(__file__).with_name('test_watershed_outlet_topology.py'))
RUNNER = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(RUNNER)
CLASS = 'com.shnewbs.deepprimal.core.WatershedOutletTopologyChecks'
MARKER = 'WATERSHED_OUTLET_TOPOLOGY_PASS checks=123'
ASSERTION = ('Exception in thread "main" java.lang.AssertionError: regression\n'
             '\tat ' + CLASS + '.main(WatershedOutletTopologyChecks.java:1)\n')


def process(code=0, out='', err=''):
    return subprocess.CompletedProcess([], code, out, err)


class OutletRunnerFailures(unittest.TestCase):
    def exercise(self, negative, *, positive=None, compiler=None):
        launches = 0
        compilations = 0

        def simulated(command, **kwargs):
            nonlocal launches, compilations
            if '-version' in command:
                return process(err='simulated JDK')
            if '--release' in command:
                compilations += 1
                if compilations > 1 and compiler is not None:
                    return compiler
                dest = Path(command[command.index('-d') + 1])
                binary = dest / 'com/shnewbs/deepprimal/core/WatershedOutletTopology.class'
                binary.parent.mkdir(parents=True, exist_ok=True)
                binary.write_bytes(b'simulated compiled identity')
                return process()
            launches += 1
            return (positive if positive is not None else process(out=MARKER)) if launches == 1 else negative

        with tempfile.TemporaryDirectory() as temp:
            evidence = Path(temp) / 'receipt.json'
            with patch.object(RUNNER.subprocess, 'run', side_effect=simulated), \
                    patch('sys.argv', ['outlet', '--evidence', str(evidence)]), \
                    contextlib.redirect_stdout(io.StringIO()):
                RUNNER.main()
            self.assertTrue(evidence.exists())

    def test_actual_fixture_assertion_is_accepted(self):
        self.exercise(process(1, err=ASSERTION))

    def test_bad_mutant_launches_are_rejected(self):
        cases = [process(1, err='Error: Could not find or load main class'),
                 process(2, err=ASSERTION), process(-9, err=ASSERTION),
                 process(1, out=ASSERTION),
                 process(1, err=ASSERTION.replace(CLASS, 'other.Fixture')),
                 process(1, err=ASSERTION.replace('java.lang.AssertionError', 'java.lang.NoClassDefFoundError')),
                 process(1, err='launcher mentioned java.lang.AssertionError'),
                 process(0, out=MARKER)]
        for result in cases:
            with self.subTest(result=result), self.assertRaises(RuntimeError):
                self.exercise(result)

    def test_success_without_positive_marker_is_rejected(self):
        with self.assertRaises(RuntimeError):
            self.exercise(process(1, err=ASSERTION), positive=process(out='no fixture executed'))

    def test_mutant_compilation_failure_is_rejected(self):
        with self.assertRaisesRegex(RuntimeError, 'compilation failed'):
            self.exercise(process(1, err=ASSERTION), compiler=process(1, err='compiler failed'))


if __name__ == '__main__':
    unittest.main()
