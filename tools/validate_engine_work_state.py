#!/usr/bin/env python3
"""Read-only coordination preflight; never silently discard duplicate JSON keys."""
import argparse
import json
from datetime import datetime
from pathlib import Path


def unique_object(pairs):
    result = {}
    for key, value in pairs:
        if key in result:
            raise ValueError(f"duplicate JSON key: {key}")
        result[key] = value
    return result


def timestamp(value, name):
    if not isinstance(value, str):
        raise ValueError(f"{name} must be a timestamp")
    try:
        parsed = datetime.fromisoformat(value.replace('Z', '+00:00'))
    except ValueError as error:
        raise ValueError(f"invalid {name}") from error
    if parsed.tzinfo is None or parsed.utcoffset() is None:
        raise ValueError(f"{name} requires an explicit timezone")
    return parsed


def validate(text):
    state = json.loads(text, object_pairs_hook=unique_object)
    if not isinstance(state, dict):
        raise ValueError('work state must be an object')
    if type(state.get('schema')) is not int or state['schema'] != 1:
        raise ValueError('unsupported work-state schema')
    for key in ('status', 'source_ref', 'worker', 'current_gate'):
        if not isinstance(state.get(key), str) or not state[key].strip():
            raise ValueError(f'missing/nonempty root string required: {key}')
    for key in ('owner_acceptance_target', 'builder_auditor_coordination',
                'performance_measurement', 'builder_cycle'):
        if not isinstance(state.get(key), dict):
            raise ValueError(f'missing root coordination object: {key}')
    if type(state.get('acceptance_complete')) is not bool:
        raise ValueError('root acceptance_complete must be boolean')
    updated = timestamp(state.get('updated_at'), 'updated_at')
    expiry = timestamp(state.get('active_until'), 'active_until')
    if state['status'] == 'active' and expiry <= updated:
        raise ValueError('active lease must expire after updated_at')
    # Wall-clock expiry is deliberately left to the caller: expired claims remain readable.
    for word, revision in [('three', 3), ('four', 4), ('five', 5)]:
        key = 'latest_channel_revision_' + word
        if key in state:
            record = state[key]
            if not isinstance(record, dict) or type(record.get('revision')) is not int or record['revision'] != revision:
                raise ValueError(f'{key} must identify revision {revision}')
    return state


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('path', nargs='?', type=Path, default=Path('docs/ENGINE-WORK-STATE.json'))
    args = parser.parse_args()
    try:
        state = validate(args.path.read_text(encoding='utf-8'))
    except (OSError, ValueError) as error:
        parser.exit(1, f'ENGINE_WORK_STATE_FAIL: {error}\n')
    print('ENGINE_WORK_STATE_PASS source_ref=' + state['source_ref'] + ' status=' + state['status'])


if __name__ == '__main__':
    main()
