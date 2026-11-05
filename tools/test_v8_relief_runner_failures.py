"""Verify relief mutation controls reject infrastructure failures."""
import importlib.util
from pathlib import Path
import subprocess
import unittest
from unittest.mock import patch

SPEC = importlib.util.spec_from_file_location(
    'relief_runner', Path(__file__).with_name('test_v8_relief_v5.py'))
RUNNER = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(RUNNER)
ASSERTION = ('Exception in thread "main" java.lang.AssertionError: bound\n'
             '\tat ' + RUNNER.MAIN + '.main(V8ReliefJoinV5ComponentChecks.java:42)\n')


def result(code=1, out='', err=ASSERTION):
    return subprocess.CompletedProcess([], code, out, err)


class ReliefRunnerFailures(unittest.TestCase):
    def check_negative(self, done):
        with patch.object(RUNNER, 'compile_run', return_value=done):
            return RUNNER.once('source', 'broken-bound')

    def test_intended_assertion_is_accepted(self):
        self.assertEqual(self.check_negative(result()),
                         {'mutation': 'broken-bound', 'rejected': True})

    def test_launch_failure_is_rejected(self):
        cases = [result(err='launcher mentioned AssertionError'),
                 result(code=2), result(code=-9), result(code=0),
                 result(out=ASSERTION, err=''),
                 result(err=ASSERTION.replace(RUNNER.MAIN, 'other.Fixture')),
                 result(err=ASSERTION.replace('java.lang.AssertionError', 'java.lang.LinkageError')),
                 result(out='RELIEF5_JOIN_COMPONENT_PASS assertions=212735'),
                 result(err=ASSERTION.replace('Exception in thread "main" ', ''))]
        for done in cases:
            with self.subTest(done=done), self.assertRaises(RuntimeError):
                self.check_negative(done)

    def test_compile_failure_never_counts_as_rejection(self):
        with patch.object(RUNNER.subprocess, 'run', return_value=result(err='compiler failed')) as execute:
            with self.assertRaises(RuntimeError):
                RUNNER.once('source', 'broken-bound')
            self.assertEqual(execute.call_count, 1)

    def test_positive_requires_receipt_and_assertion_count(self):
        for done in [result(code=0, err='', out=''),
                     result(code=0, err='', out='RELIEF5_JOIN_COMPONENT_PASS assertions=2'),
                     result(code=1, err='', out='RELIEF5_JOIN_COMPONENT_PASS assertions=212735')]:
            with self.subTest(done=done), patch.object(RUNNER, 'compile_run', return_value=done):
                with self.assertRaises(RuntimeError):
                    RUNNER.once('source')


if __name__ == '__main__':
    unittest.main()
