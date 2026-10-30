"""Regression tests for the V7 source-window validation runner."""
import contextlib
import hashlib
import importlib.util
import io
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

SPEC = importlib.util.spec_from_file_location(
    "relief_v7_runner", Path(__file__).with_name("test_v8_relief_v7_window.py"))
RUNNER = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(RUNNER)

V6 = """Node.bounded(\"relief6_plateau_entry\", Op.SMOOTHSTEP, \"continentalness\", .12, .48)
Node.bounded(\"relief6_coast_release\", Op.SMOOTHSTEP, \"continentalness\", -.02, .14)
""" + "Node.operation();\n" * 8
V7 = """Node.bounded(\"relief7_foothill_entry\", Op.SMOOTHSTEP, \"continentalness\", .10, .34)
Node.bounded(\"relief7_highland_entry\", Op.SMOOTHSTEP, \"continentalness\", .56, .80)
""" + "Node.operation();\n" * 8


def blob_id(text):
    data = text.encode("utf-8")
    return hashlib.sha1(b"blob " + str(len(data)).encode() + b"\0" + data).hexdigest()


class ReliefV7RunnerFailures(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.src = Path(self.temp.name)
        self.write("V8ReliefJoinV6.java", V6)
        self.write("V8ReliefJoinV7.java", V7)
        self.globals = patch.multiple(
            RUNNER,
            SRC=self.src,
            EXPECTED={
                "V8ReliefJoinV6.java": blob_id(V6),
                "V8ReliefJoinV7.java": blob_id(V7),
            },
            checks=0,
        )
        self.globals.start()

    def tearDown(self):
        self.globals.stop()
        self.temp.cleanup()

    def write(self, name, text):
        (self.src / name).write_text(text, encoding="utf-8")

    def run_main(self):
        with contextlib.redirect_stdout(io.StringIO()) as output:
            RUNNER.main()
        return output.getvalue()

    def test_known_good_fixture_passes(self):
        receipt = self.run_main()
        self.assertIn("V8_RELIEF_V7_WINDOW_PASS assertions=37316", receipt)

    def test_unpinned_source_mutation_is_rejected(self):
        self.write("V8ReliefJoinV7.java", V7.replace(".56, .80", ".12, .14"))
        with self.assertRaisesRegex(AssertionError, "unexpected source blob"):
            self.run_main()

    def test_repinned_narrow_window_is_rejected_behaviorally(self):
        mutant = V7.replace(".56, .80", ".12, .14")
        self.write("V8ReliefJoinV7.java", mutant)
        RUNNER.EXPECTED["V8ReliefJoinV7.java"] = blob_id(mutant)
        with self.assertRaisesRegex(AssertionError, "V7 join not broader than frozen V6"):
            self.run_main()

    def test_missing_dependency_is_not_a_pass(self):
        (self.src / "V8ReliefJoinV6.java").unlink()
        with self.assertRaises(FileNotFoundError):
            self.run_main()


if __name__ == "__main__":
    unittest.main()
