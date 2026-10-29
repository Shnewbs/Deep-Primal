#!/usr/bin/env python3
"""Regression tests for mutation-runner process failures; no JDK required."""
import contextlib
import importlib.util
import io
import itertools
from pathlib import Path
import subprocess
import tempfile
import unittest
from unittest.mock import patch

TOOLS = Path(__file__).resolve().parent
RUNNERS = (
    ("test_watershed_interior_ports", "WatershedInteriorPortRoutesChecks",
     "V8_INTERIOR_ROUTES_PASS"),
    ("test_watershed_region_port_bridge", "WatershedRegionPortBridgeChecks",
     "V8_REGION_PORT_BRIDGE_PASS"),
)


def load_runner(name):
    spec = importlib.util.spec_from_file_location(name, TOOLS / (name + ".py"))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def process(code=0, out="", err=""):
    return subprocess.CompletedProcess([], code, out, err)


class RunnerFailureChecks(unittest.TestCase):
    def test_compile_failure_aborts_before_launch(self):
        for name, _, _ in RUNNERS:
            with self.subTest(runner=name), tempfile.TemporaryDirectory() as tmp:
                runner = load_runner(name)
                with patch.object(runner.subprocess, "run", return_value=process(
                        1, err="synthetic javac failure")) as run:
                    with self.assertRaisesRegex(RuntimeError, "compilation failed"):
                        runner.run(Path("Source.java"), Path(tmp) / "classes")
                    self.assertEqual(run.call_count, 1)

    def test_only_executed_assertion_counts_as_rejected(self):
        for name, checks, token in RUNNERS:
            assertion = ('Exception in thread "main" java.lang.AssertionError: regression\n'
                         f'\tat {checks}.main({checks}.java:1)\n')
            cases = (
                ("positive", process(0, token), True),
                ("assertion", process(1, err=assertion), False),
                ("missing_marker", process(0, "no checks executed"), None),
                ("launcher", process(1, err="Error: Could not find or load main class"), None),
                ("vm_failure", process(-1, err=assertion), None),
                ("exception", process(1, err='Exception in thread "main" '
                 'java.lang.IllegalArgumentException: wrong failure'), None),
                ("misleading_stdout", process(1, out=assertion), None),
                ("missing_fixture", process(1, err=assertion.splitlines()[0]), None),
                ("wrong_exception", process(1, err=assertion.replace(
                    "java.lang.AssertionError", "java.lang.NoClassDefFoundError")), None),
            )
            for label, result, expected in cases:
                with self.subTest(runner=name, case=label), tempfile.TemporaryDirectory() as tmp:
                    runner = load_runner(name)
                    with patch.object(runner.subprocess, "run", side_effect=[process(), result]):
                        if expected is None:
                            with self.assertRaises(RuntimeError):
                                runner.run(Path("Source.java"), Path(tmp) / "classes")
                        else:
                            okay, _ = runner.run(Path("Source.java"), Path(tmp) / "classes")
                            self.assertIs(okay, expected)

    def test_mutant_infrastructure_failure_never_prints_pass(self):
        for name, _, token in RUNNERS:
            # Let both positive fixtures pass, then fail the first mutant's
            # compiler or JVM. This reproduces the original false PASS in main.
            for stage in ("compile", "launch"):
                runner = load_runner(name)
                responses = [process(), process(0, token)] * 2
                if stage == "launch":
                    responses.append(process())
                responses.append(process(1, err="synthetic tool failure"))
                stdout = io.StringIO()
                with self.subTest(runner=name, stage=stage), contextlib.redirect_stdout(stdout):
                    with patch("sys.argv", [name]), patch.object(
                            runner.subprocess, "run", side_effect=itertools.chain(
                                responses, itertools.repeat(responses[-1]))):
                        with self.assertRaises(RuntimeError):
                            runner.main()
                    self.assertNotIn("RUNNER_PASS", stdout.getvalue())


if __name__ == "__main__":
    unittest.main()
