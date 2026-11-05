#!/bin/bash
set -euo pipefail
TC=/workspace/scratch/a22609fabda0/water-frame-1732/toolchain
exec python3 tools/build_with_current_proxy.py --java-home "$TC/jdk-25.0.4.1+1" --gradle "$TC/gradle-9.2.1/bin/gradle" --gradle-user-home "$TC/gradle-cache" --trust-store /etc/ssl/certs/java/cacerts --neoform-agent "$TC/checked-agent/neoform-process-fallback.jar" -- -I /workspace/scratch/1a7cb29b04a2/terrain-next/fresh-compile.gradle --offline --no-daemon --no-build-cache --console=plain --max-workers=2 -Dorg.gradle.jvmargs=-Xmx1G -Porg.gradle.java.installations.paths="$TC/jdk-21.0.12.1+1,$TC/jdk-25.0.4.1+1" -PvulkanPlatform=linux "$@"
