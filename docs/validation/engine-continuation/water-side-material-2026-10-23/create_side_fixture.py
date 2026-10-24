from pathlib import Path
R=Path(__file__).resolve().parent;P=R/'project'
(P/'src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterSideMaterialChecks.java').write_bytes((R/'baseline-WaterSideMaterialChecks.java').read_bytes())
p=P/'src/visualValidation/java/com/shnewbs/deepprimal/validation/ClientCapture.java';s=p.read_text()
if 'WaterSideMaterialChecks.run' not in s:
 at=s.index('if (action.equals("water-material-check"))')
 s=s[:at]+'if (action.equals("water-side-material-check")) { WaterSideMaterialChecks.run(mc, dir); return; }\n      '+s[at:];p.write_text(s)
