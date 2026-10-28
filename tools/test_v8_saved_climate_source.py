#!/usr/bin/env python3
"""Strict isolated Java21 contract regression for the opt-in V8/V7 climate bridge.

No Gradle/Minecraft/native world, GPU, or production TerrainEngine is executed.
API doubles pin method contracts only; owner runs the full Java25 integration.
"""
import hashlib
import json
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "src/main/java/com/shnewbs/deepprimal/engine/V8SavedClimateSource.java"
TEST = ROOT / "tools/tests/V8SavedClimateSourceChecks.java"
PLAN = ROOT / "src/main/java/com/shnewbs/deepprimal/engine/V8EcotoneColumnPlan.java"

DOUBLES = {
    "com/shnewbs/deepprimal/core/TerrainOptions.java": '''package com.shnewbs.deepprimal.core;
public record TerrainOptions(int tag) {}\n''',
    "com/shnewbs/deepprimal/core/TerrainNoise.java": '''package com.shnewbs.deepprimal.core;
public final class TerrainNoise {
  private final long seed;
  public TerrainNoise(long seed) {this.seed=seed;}
  public double field(double x,double z,double scale,long salt) {
    return Math.sin(x/scale + z/(scale*2) + seed*1.e-12 + salt*.00017);
  }
}\n''',
    "com/shnewbs/deepprimal/core/TerrainEngine.java": '''package com.shnewbs.deepprimal.core;
public final class TerrainEngine {
  private final long seed;
  private final TerrainOptions options;
  private final int channelRevision;
  public final TerrainNoise noise;
  public TerrainEngine(long seed,int revision,TerrainOptions options,int channelRevision) {
    if (revision!=7) throw new IllegalArgumentException("not V7");
    this.seed=seed;this.options=options;this.channelRevision=channelRevision;
    noise=new TerrainNoise(seed);
  }
  public record Sample(double rain,double slope,double water) {
    public boolean wet() {return Double.isFinite(water);}
  }
  public Sample sample(double x,double z) {
    int ix=(int)x,iz=(int)z;
    // Ocean, moving river and STILL lake all require domain exclusion.
    boolean wet=ix%17==0 || iz%19==0 || ix%29==0;
    double rain=Math.max(.08,Math.min(1,.47+noise.field(x,z,2200,81)*.24
        +options.tag()*.007+channelRevision*.002));
    double slope=Math.abs(noise.field(x,z,600,90))*.9;
    return new Sample(rain,slope,wet?96:Double.NaN);
  }
  public boolean ocean(double x,double z) {return ((int)x)%17==0;}
  public double temperature(double x,double z,double height) {
    double t=.71+noise.field(x,z,4800,3100)*.12
        -Math.max(0,height-110)/410+options.tag()*.0009;
    return Math.max(0,Math.min(1,t));
  }
}\n''',
    "com/shnewbs/deepprimal/core/biome/EcotoneBlend.java": '''package com.shnewbs.deepprimal.core.biome;
public final class EcotoneBlend {
  private EcotoneBlend() {}
  public enum Domain {LAND,RIVER,OCEAN}
  public record Climate(double height,double temperature,double rainfall,double slope,double patch) {
    public Climate {
      if(!Double.isFinite(height)||!Double.isFinite(temperature)||temperature<0||temperature>1
          ||!Double.isFinite(rainfall)||rainfall<0||rainfall>1||!Double.isFinite(slope)||slope<0
          ||!Double.isFinite(patch)) throw new IllegalArgumentException("invalid climate");
    }
  }
}\n''',
    "com/shnewbs/deepprimal/core/density/DensityChunk.java": '''package com.shnewbs.deepprimal.core.density;
public final class DensityChunk {
  public static final int WIDTH=16;
  private final int minY,height,top;
  public DensityChunk(int minY,int height,int top) {
    this.minY=minY;this.height=height;this.top=top;
  }
  public int minY() {return minY;}
  public int maxYExclusive() {return Math.addExact(minY,height);}
  public int surfaceExclusive(int x,int z) {return top+1;}
}
''',
    "com/shnewbs/deepprimal/core/biome/EcotoneSurfaceField.java": '''package com.shnewbs.deepprimal.core.biome;
public final class EcotoneSurfaceField {
  private EcotoneSurfaceField() {}
  public record Sample(double soilFactor) {}
}
''',
    "com/shnewbs/deepprimal/engine/GroundSurfaceProfile.java": '''package com.shnewbs.deepprimal.engine;
public record GroundSurfaceProfile(int soilDepth) {}
''',
    "com/shnewbs/deepprimal/engine/TerrainProfile.java": '''package com.shnewbs.deepprimal.engine;
import com.shnewbs.deepprimal.core.TerrainOptions;
public record TerrainProfile(TerrainOptions options) {}
''',
    "com/shnewbs/deepprimal/engine/V8SurfacePolicy.java": '''package com.shnewbs.deepprimal.engine;
public record V8SurfacePolicy(String id) {
  public static final V8SurfacePolicy FROZEN_R1=new V8SurfacePolicy("frozen-r1");
}\n''',
    "com/shnewbs/deepprimal/engine/V8GeneratorSnapshot.java": '''package com.shnewbs.deepprimal.engine;
import com.shnewbs.deepprimal.core.biome.EcotoneBlend;
import com.shnewbs.deepprimal.core.biome.EcotoneSurfaceField;
public record V8GeneratorSnapshot(int version,long seed,V8SurfacePolicy surfacePolicy) {
  public static final int SURFACE_VERSION=3;
  public record Prepared(V8GeneratorSnapshot snapshot,int minY,int height,
      GroundSurfaceProfile surface) {
    public void requireHeight(int min,int value) {
      if(min!=minY||value!=height)throw new IllegalArgumentException("height mismatch");
    }
    public EcotoneSurfaceField.Sample previewEcotoneSurface(int x,int z,
        EcotoneBlend.Climate climate,EcotoneBlend.Domain domain) {
      if(domain!=EcotoneBlend.Domain.LAND)throw new IllegalArgumentException("water painted");
      return new EcotoneSurfaceField.Sample(.65);
    }
  }
}\n''',
}


def execute(source: str):
    with tempfile.TemporaryDirectory(prefix="deepprimal-v8-climate-") as tmp:
        temp = Path(tmp)
        paths = []
        for rel, data in DOUBLES.items():
            path = temp / "doubles" / rel
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(data, encoding="utf-8")
            paths.append(path)
        target = temp / "producer" / "com/shnewbs/deepprimal/engine/V8SavedClimateSource.java"
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(source, encoding="utf-8")
        classes = temp / "classes"
        classes.mkdir()
        compile_args = ["javac", "--release", "21", "-Xlint:all", "-Werror",
                        "-d", str(classes), *map(str, paths), str(target), str(PLAN), str(TEST)]
        compiled = subprocess.run(compile_args, capture_output=True, text=True, timeout=60)
        if compiled.returncode:
            return {"compiled": False, "compile_log": (compiled.stdout + compiled.stderr)[-6000:]}
        launched = subprocess.run(["java", "-ea", "-Xverify:all", "-cp", str(classes),
                                  "com.shnewbs.deepprimal.engine.V8SavedClimateSourceChecks"],
                                 capture_output=True, text=True, timeout=60)
        return {"compiled": True, "pass": launched.returncode == 0,
                "exit": launched.returncode, "stdout": launched.stdout.strip(),
                "stderr": launched.stderr[-2000:]}


def main():
    for tool in ("javac", "java"):
        if not shutil.which(tool):
            raise SystemExit(f"V8_CLIMATE_BRIDGE_UNAVAILABLE missing {tool}")
    version = subprocess.run(["java", "-version"], capture_output=True,
                             text=True, check=True).stderr.splitlines()[0]
    production = SOURCE.read_text(encoding="utf-8")
    positive = execute(production)
    if not positive.get("compiled") or not positive.get("pass"):
        print(json.dumps({"version": version, "positive": positive}, indent=2))
        raise SystemExit("V8_CLIMATE_BRIDGE_FAIL positive regression")
    needle = ": water.wet() ? EcotoneBlend.Domain.RIVER : EcotoneBlend.Domain.LAND;"
    if production.count(needle) != 1:
        raise SystemExit("V8_CLIMATE_BRIDGE_FAIL mutation marker missing or duplicate")
    broken = production.replace(needle,
        ": false ? EcotoneBlend.Domain.RIVER : EcotoneBlend.Domain.LAND;")
    negative = execute(broken)
    if not negative.get("compiled") or negative.get("pass"):
        print(json.dumps({"version": version, "negative": negative}, indent=2))
        raise SystemExit("V8_CLIMATE_BRIDGE_FAIL negative control not rejected")
    summary = {
        "status": "V8_SAVED_CLIMATE_BRIDGE_PASS",
        "java": version, "api_doubles_only": True,
        "source_sha256": hashlib.sha256(production.encode()).hexdigest(),
        "test_sha256": hashlib.sha256(TEST.read_bytes()).hexdigest(),
        "positive": positive, "broken_control": {"compiled": negative["compiled"],
                                         "rejected": not negative["pass"],
                                         "exit": negative["exit"]},
        "java25_full_build": False, "native_minecraft": False, "gpu": False,
    }
    print(json.dumps(summary, indent=2))


if __name__ == "__main__":
    main()
