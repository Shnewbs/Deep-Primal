"""Download the pinned TFC development dependency; verify every downloaded byte."""
from pathlib import Path
import hashlib
import os
import urllib.request

ROOT = Path(__file__).resolve().parents[1]
DEST = ROOT / "libs/tfc-4.2.12.jar"
URL = "https://github.com/TerraFirmaCraft/TerraFirmaCraft/releases/download/v4.2.12/TerraFirmaCraft-NeoForge-1.21.1-4.2.12.jar"
SHA256 = "3c2d80e47c78cfbd617823ebe0d3d5e2f9ba9e7f04c1b34867dbab3894584440"

def valid(path):
    return path.is_file() and hashlib.sha256(path.read_bytes()).hexdigest() == SHA256

if not valid(DEST):
    DEST.parent.mkdir(parents=True, exist_ok=True)
    temporary = DEST.with_suffix(".download")
    request = urllib.request.Request(URL, headers={"User-Agent": "Deep-Primal-Maritime-port"})
    with urllib.request.urlopen(request, timeout=120) as response, temporary.open("wb") as output:
        while chunk := response.read(1024 * 1024):
            output.write(chunk)
    if not valid(temporary):
        raise SystemExit("TFC dependency checksum mismatch; the existing jar was not replaced")
    os.replace(temporary, DEST)
print("TFC 4.2.12 development dependency verified")
