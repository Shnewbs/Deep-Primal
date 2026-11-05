from pathlib import Path
R=Path(__file__).resolve().parent;P=R/'project'
(P/'src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterMaterialTransitionChecks.java').write_text((R/'baseline-WaterMaterialTransitionChecks.java').read_text())
p=P/'src/visualValidation/java/com/shnewbs/deepprimal/validation/ClientCapture.java';s=p.read_text()
if 'action.equals("water-material-check")' not in s:
 at='      if (action.equals("water-direction-upload-check")) {'
 assert at in s;s=s.replace(at,'      if (action.equals("water-material-check")) {\n        WaterMaterialTransitionChecks.run(mc, dir);\n        return;\n      }\n'+at);p.write_text(s)
