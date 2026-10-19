"""Compare the smoke harness's six alternating cold/repeat batches; Python 3 only."""
import argparse
import csv
import json
import math
from pathlib import Path
import re
import statistics


def summarize(directory):
    reports = sorted(Path(directory).glob('*.txt'))
    if len(reports) != 6:
        raise ValueError('Expected six reports: three cold regions, each followed by its repeat.')
    result = {}
    for kind, indices in [('cold', [0, 2, 4]), ('repeat', [1, 3, 5])]:
        samples = []
        elapsed = 0.0
        for index in indices:
            report = reports[index]
            elapsed += float(re.search(r'elapsed=([\d.]+)s', report.read_text())[1])
            with report.with_suffix('.csv').open() as source:
                samples.extend(float(row['request_to_ready_ms']) for row in csv.DictReader(source))
        samples.sort()
        result[kind] = {
            'requests': len(samples), 'elapsed_seconds': round(elapsed, 2),
            'requests_per_second': round(len(samples) / elapsed, 3),
            'median_ms': round(statistics.median(samples), 3),
            'p95_ms': round(samples[math.ceil(len(samples) * .95) - 1], 3),
            'p99_ms': round(samples[math.ceil(len(samples) * .99) - 1], 3),
            'max_ms': round(max(samples), 3),
        }
    return result


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('baseline')
    parser.add_argument('candidate')
    args = parser.parse_args()
    print(json.dumps({'baseline': summarize(args.baseline), 'candidate': summarize(args.candidate)}, indent=2))
