from pathlib import Path
import argparse,hashlib,json
parser=argparse.ArgumentParser();parser.add_argument('--reservation-source',type=Path,required=True);args=parser.parse_args()
r=Path(__file__).resolve().parent
baseline=(r/'baseline-HydrologyRuntime.java').read_text()
old='    long phase = Math.min(19, now - start);'
new='''    // With ordered signed ticks, negative subtraction means elapsed-time overflow.
    // Such an interval is already complete; never turn it into a negative quota.
    long elapsed = now - start;
    long phase = elapsed < 0 ? 19 : Math.min(19, elapsed);'''
if baseline.count(old)!=1: raise RuntimeError('Review changed allowance source before patching')
fixed=baseline.replace(old,new)
(r/'HydrologyRuntime.java').write_text(fixed)
reserved=args.reservation_source.read_text()
if reserved.count(old)!=1: raise RuntimeError('Review changed reservation source before patching')
(r/'combined-HydrologyRuntime.java').write_text(reserved.replace(old,new))
(r/'source-identity.json').write_text(json.dumps({name:hashlib.sha256((r/name).read_bytes()).hexdigest() for name in ['baseline-HydrologyRuntime.java','HydrologyRuntime.java','combined-HydrologyRuntime.java']},indent=2))
