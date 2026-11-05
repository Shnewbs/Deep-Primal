# Renderer jam 62 — publish membership reuse

Implementation commit: 95ab79f433ab1921f490cfb97515fd2cc49e1052
Parent main observed before integration: 2e3f30c212e56697a735d912803bf33cea5c8099
Scope: renderer-only LOD disk recovery bookkeeping.

The completed cursor previously built a temporary HashSet from the published nearest list solely to decide which queued recovery keys to discard. The Selection already owns the authoritative bounded retained-key set. Jam 62 exposes a read-only membership probe and reuses that set, removing one full-size temporary table and a second hashing pass per completed cursor publication.

No terrain generation, water simulation, GUI redesign, port, or FSR/DLSS provider work was changed. No qualified JAR, GPU acceptance, or performance claim is made.
