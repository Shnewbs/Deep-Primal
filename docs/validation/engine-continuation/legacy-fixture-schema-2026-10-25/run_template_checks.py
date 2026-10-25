"""Check the actual private fixture JSON transformation with Java25 and Gson.

Usage: python3 run_template_checks.py REPO JDK25 GSON_JAR OUTPUT_DIR
This is not a Minecraft registry/codec execution or a packaged native gate.
"""
from pathlib import Path
import subprocess, sys, tempfile

repo, jdk, gson, out = map(Path, sys.argv[1:])
out.mkdir(parents=True, exist_ok=True)
source = (repo / 'src/visualValidation/java/com/shnewbs/deepprimal/validation/DepositNativeChecks.java').read_text()
start = source.index('    var json=template.deepCopy();', source.index('static EngineChunkGenerator generator'))
end = source.index('    var ops=l.registryAccess()', start)
with tempfile.TemporaryDirectory() as temp:
    work = Path(temp)
    extracted = work / 'TemplateUnderTest.java'
    extracted.write_text('import com.google.gson.*; public final class TemplateUnderTest { static JsonObject transform(JsonObject template,int rev) {\n' + source[start:end] + 'return json; }}\n')
    check = Path(__file__).with_name('LegacyTemplateChecks.java')
    with (out / 'template-checks.log').open('w') as log:
        subprocess.run([str(jdk/'bin/javac'), '-cp', str(gson), '-d', str(work), str(extracted), str(check)], stdout=log, stderr=subprocess.STDOUT, check=True, timeout=60)
        subprocess.run([str(jdk/'bin/java'), '-cp', str(work)+':'+str(gson), 'LegacyTemplateChecks'], stdout=log, stderr=subprocess.STDOUT, check=True, timeout=60)
print((out / 'template-checks.log').read_text())
