import copy
import json
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

from validate_engine_work_state import validate


class WorkStateChecks(unittest.TestCase):
    def setUp(self):
        self.state = dict(schema=1, status='active', source_ref='main', worker='builder',
                          current_gate='test', acceptance_complete=False,
                          updated_at='2026-10-25T00:00:00Z', active_until='2026-10-25T00:30:00Z',
                          owner_acceptance_target={}, builder_auditor_coordination={},
                          performance_measurement={}, builder_cycle={})

    def test_valid_expired_claim_remains_readable(self):
        self.assertEqual(validate(json.dumps(self.state)), self.state)

    def test_preserves_unknown_fields(self):
        self.state['future_evidence'] = {'values': [1, 'saved', None]}
        self.assertEqual(validate(json.dumps(self.state)), self.state)

    def test_duplicate_root(self):
        with self.assertRaisesRegex(ValueError, 'duplicate'):
            validate('{"status":"active","status":"checkpointed"}')

    def test_duplicate_nested(self):
        with self.assertRaisesRegex(ValueError, 'duplicate'):
            validate('{"nested":{"revision":5,"revision":4}}')

    def test_accidental_wrapper(self):
        nested = copy.deepcopy(self.state)
        self.state = {'schema': 1, 'latest_channel_revision_five': nested}
        with self.assertRaises(ValueError):
            validate(json.dumps(self.state))

    def test_missing_coordination_objects(self):
        for key in ('owner_acceptance_target', 'builder_auditor_coordination', 'performance_measurement', 'builder_cycle'):
            with self.subTest(key=key):
                state = copy.deepcopy(self.state)
                del state[key]
                with self.assertRaises(ValueError): validate(json.dumps(state))

    def test_bad_root_fields(self):
        for key, value in [('schema', True), ('source_ref', ''), ('worker', None), ('acceptance_complete', 0)]:
            with self.subTest(key=key):
                state = dict(self.state, **{key: value})
                with self.assertRaises(ValueError): validate(json.dumps(state))

    def test_naive_and_invalid_timestamps(self):
        for value in ['2026-10-25T00:30:00', 'yesterday', None]:
            with self.subTest(value=value):
                with self.assertRaises(ValueError): validate(json.dumps(dict(self.state, active_until=value)))

    def test_active_lease_order(self):
        for expiry in ['2026-10-25T00:00:00Z', '2026-10-24T00:00:00Z']:
            with self.assertRaises(ValueError): validate(json.dumps(dict(self.state, active_until=expiry)))

    def test_checkpoint_equal_timestamps(self):
        state = dict(self.state, status='checkpointed', active_until=self.state['updated_at'])
        self.assertEqual(validate(json.dumps(state)), state)

    def test_revision_record_identity(self):
        for word, revision in [('three', 3), ('four', 4), ('five', 5)]:
            key = 'latest_channel_revision_' + word
            state = dict(self.state, **{key: {'revision': revision}})
            self.assertEqual(validate(json.dumps(state)), state)
            state[key]['revision'] = revision + 1
            with self.assertRaises(ValueError): validate(json.dumps(state))

    def test_cli_read_only_and_exit_codes(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / 'state.json'
            script = Path(__file__).with_name('validate_engine_work_state.py')
            for text, exit_code in [(json.dumps(self.state), 0), ('{"x":1,"x":2}', 1)]:
                path.write_text(text)
                result = subprocess.run([sys.executable, *(['-OO'] if sys.flags.optimize == 2 else ['-O'] if sys.flags.optimize else []), str(script), str(path)], capture_output=True, text=True)
                self.assertEqual(result.returncode, exit_code)
                self.assertEqual(path.read_text(), text)
                self.assertIn('ENGINE_WORK_STATE_' + ('PASS' if exit_code == 0 else 'FAIL'), result.stdout + result.stderr)


if __name__ == '__main__':
    unittest.main()
