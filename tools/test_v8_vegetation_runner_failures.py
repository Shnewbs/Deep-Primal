#!/usr/bin/env python3
"""Mutation-runner process regressions; no JDK or native Minecraft required."""
import contextlib
import importlib.util
import io
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch


def load_runner():
    path = Path(__file__).with_name('test_v8_vegetation_columns.py')
    spec = importlib.util.spec_from_file_location('vegetation_runner', path)
    runner = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(runner)
    return runner


CLASS = 'com.shnewbs.deepprimal.engine.V8VegetationColumnPlanChecks'
ASSERTION = ('Exception in thread "main" java.lang.AssertionError: snow protected\n'
             '\tat ' + CLASS + '.require(V8VegetationColumnPlanChecks.java:26)\n')


class VegetationRunnerFailureChecks(unittest.TestCase):
    def test_negative_requires_fixture_assertion(self):
        cases = (
            (1, ASSERTION, True),
            (0, ASSERTION, False),
            (-9, ASSERTION, False),
            (2, ASSERTION, False),
            (1, 'Error: Could not find or load main class AssertionError', False),
            (1, 'Exception in thread "main" java.lang.NoClassDefFoundError: '
             'java/lang/AssertionError\n', False),
            (1, ASSERTION.replace(CLASS, 'OtherChecks'), False),
            (1, ASSERTION.splitlines()[0], False),
            (1, 'diagnostic mentions AssertionError', False),
        )
        for code, stderr, expected in cases:
            with self.subTest(code=code, stderr=stderr):
                runner = load_runner()
                self.assertIs(runner.rejected_by_fixture(
                    {'exit': code, 'stdout': '', 'stderr': stderr}), expected)

    def test_compile_failure_never_launches(self):
        runner = load_runner()
        with patch.object(runner, 'run', return_value={
                'exit': 1, 'stdout': '', 'stderr': 'synthetic javac failure'}) as run:
            with self.assertRaisesRegex(RuntimeError, 'compiled variant failed'):
                runner.compile_run('class Unused {}', 'broken')
        self.assertEqual(run.call_count, 1)

    def test_main_rejects_launcher_failure_mentioning_assertion(self):
        runner = load_runner()
        for stderr in ('Error: Could not find or load main class AssertionError',
                       ASSERTION.replace(CLASS, 'OtherChecks')):
            with self.subTest(stderr=stderr), tempfile.TemporaryDirectory() as tmp:
                output = io.StringIO()
                positive = {'exit': 0, 'stdout': 'V8_VEGETATION_PLAN_PASS', 'stderr': ''}
                bad = {'exit': 1, 'stdout': '', 'stderr': stderr}
                with patch.object(runner, 'ROOT', Path(tmp)), patch.object(
                        runner, 'run', return_value={'stderr': 'synthetic java version'}), patch.object(
                        runner, 'compile_run', side_effect=[positive, positive] + [bad] * 4), contextlib.redirect_stdout(output):
                    with self.assertRaisesRegex(RuntimeError, 'negative not rejected via assertion'):
                        runner.main()
                self.assertFalse((Path(tmp) / 'results.json').exists())
                self.assertNotIn('broken_variants_rejected', output.getvalue())


if __name__ == '__main__':
    unittest.main()
