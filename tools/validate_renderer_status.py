"""Focused Java renderer reporting/recovery checks with loader/client API doubles.

Runs production preferences, diagnostics and startup code. The main screen's backend-save
block is extracted verbatim to exercise real preference I/O without opening Minecraft.
This does not certify Vulkan hardware, mixin application or a full packaged game launch.
"""
import argparse
import pathlib
import shutil
import subprocess
import tempfile

ROOT = pathlib.Path(__file__).resolve().parents[1]
SETTINGS = ROOT / 'graphics-settings/src/main/java/com/shnewbs/deepprimal/settings'
STARTUP = ROOT / 'renderer-vulkan/src/main/java/net/vulkanmod/deepprimal/Startup.java'

STUBS = {
    'net/neoforged/fml/loading/FMLPaths.java': '''package net.neoforged.fml.loading;
public final class FMLPaths {
  public static final Config CONFIGDIR = new Config();
  public static final class Config {
    public java.nio.file.Path get() { return java.nio.file.Path.of(System.getProperty("test.config")); }
  }
}''',
    'org/slf4j/Logger.java': '''package org.slf4j;
public interface Logger {
  default void warn(String message) {}
  default void warn(String message, Throwable error) {}
  default void info(String message) {}
}''',
    'com/mojang/logging/LogUtils.java': '''package com.mojang.logging;
public final class LogUtils {
  public static org.slf4j.Logger getLogger() { return new org.slf4j.Logger() {}; }
}''',
    'net/minecraft/client/Minecraft.java': '''package net.minecraft.client;
public final class Minecraft {
  public static Minecraft getInstance() { return new Minecraft(); }
  public boolean isGameLoadFinished() { return Boolean.getBoolean("test.loaded"); }
}''',
    'com/shnewbs/deepprimal/settings/RendererOwnership.java': '''package com.shnewbs.deepprimal.settings;
public final class RendererOwnership {
  public static boolean owned() { return !Boolean.getBoolean("test.delegated"); }
  public static boolean installed(String id) { return !Boolean.getBoolean("test.missing"); }
  public static String owner() { return owned() ? "Deep Primal" : "external-renderer"; }
}''',
}

CHECKS = '''package com.shnewbs.deepprimal.settings;
import java.nio.file.*;
import java.util.*;
import java.util.function.IntConsumer;
import net.vulkanmod.deepprimal.Startup;
public final class RendererStatusChecks {
  static int checks;
  static void check(boolean value, String message) {
    checks++; if (!value) throw new AssertionError(message);
  }
  static String explain(String active, RendererPreferences.Backend next, boolean installed,
      boolean owned, String fallback, String override) {
    return RendererStatus.explanation(active, next, installed, owned, "sodium", fallback, override);
  }
  record Control(IntConsumer set) {}
  static boolean backendSelected;
  static Map<String,String> values = new HashMap<>(), baseline = new HashMap<>();
  static Map<String,Control> controls = Map.of("backend", new Control(v -> {
    try { RendererPreferences.save(v == 1 ? RendererPreferences.Backend.Vulkan
        : RendererPreferences.Backend.OpenGL); }
    catch (java.io.IOException e) { throw new java.io.UncheckedIOException(e); }
  }));
  static int number(Control ignored) { return Integer.parseInt(values.get("backend")); }
  static void applyBackend() {
    // EXTRACTED_SAVE_BLOCK
  }
  public static void main(String[] args) throws Exception {
    var gl = RendererPreferences.Backend.OpenGL;
    var vk = RendererPreferences.Backend.Vulkan;
    Path marker = RendererPreferences.recoveryMarker();
    switch (args[0]) {
      case "matrix" -> {
        check(RendererStatus.summary("OpenGL", vk).equals("Active: OpenGL | After restart: Vulkan"), "Separate active/requested");
        check(RendererStatus.summary("Vulkan", gl).contains("Active: Vulkan"), "Actual device preserved");
        check(explain("OpenGL",vk,false,true,"",null).contains("not installed"), "Missing module");
        check(explain("OpenGL",gl,false,true,"",null).contains("not installed"), "Missing module visible before selection");
        check(explain("OpenGL",vk,true,false,"",null).contains("sodium"), "Delegation reason");
        check(explain("OpenGL",vk,true,true,"recovery sentinel",null).equals("recovery sentinel"), "Recovery reason retained");
        check(explain("Vulkan",vk,true,true,"stale failure",null).contains("matches"), "Live Vulkan defeats stale failure");
        check(explain("OpenGL",vk,true,true,"",null).contains("restart"), "Pending Vulkan restart");
        check(explain("Vulkan",gl,true,true,"",null).contains("restart"), "Pending OpenGL restart");
        check(explain("OpenGL",gl,true,true,"",null).contains("matches"), "Matching OpenGL");
        check(explain("OpenGL",vk,true,true,"","OpenGL").contains("takes priority"), "Launch override visible");
        check(explain("OpenGL",vk,true,true,"failure sentinel","Vulkan").contains("failure sentinel"), "Override does not hide failure");
        RendererPreferences.reportStartupFallback("live failure");
        check(RendererStatus.explanation("OpenGL",vk).contains("live failure"), "Production capture includes fallback");
      }
      case "marker" -> {
        RendererPreferences.save(vk);
        check(RendererPreferences.savedBackend() == vk, "Preference round trip");
        Files.writeString(marker,"unfinished startup sentinel");
        baseline.put("backend","1"); values.put("backend","1"); backendSelected = false;
        applyBackend();
        check(Files.readString(marker).equals("unfinished startup sentinel"), "Unrelated Apply preserves recovery marker");
        backendSelected = true; applyBackend();
        check(!Files.exists(marker), "Explicit same-choice retry clears recovery marker");
        Files.writeString(marker,"unfinished startup sentinel");
        backendSelected = false; values.put("backend","0"); applyBackend();
        check(!Files.exists(marker) && RendererPreferences.savedBackend() == gl, "Changed backend clears marker");
      }
      case "recovery" -> {
        RendererPreferences.save(vk); Files.writeString(marker,"unfinished");
        check(!Startup.enabled(), "Crash recovery disables Vulkan");
        check(RendererPreferences.startupFallbackReason().contains("Previous Vulkan"), "Crash recovery records reason");
        check(Files.exists(marker), "Recovery keeps marker until explicit retry");
      }
      case "override" -> {
        RendererPreferences.save(vk); System.setProperty("deepprimal.graphicsBackend","OpenGL");
        check(RendererPreferences.requestedBackend() == gl, "Launch override wins");
        check(!Startup.enabled(), "Launch override disables Vulkan startup");
        check(RendererStatus.explanation("OpenGL",vk).contains("deepprimal.graphicsBackend=OpenGL"), "Exact override reported");
      }
      case "delegation" -> {
        RendererPreferences.save(vk); System.setProperty("test.delegated","true");
        check(!Startup.enabled(), "External owner disables Vulkan");
        check(RendererPreferences.startupFallbackReason().contains("external-renderer"), "Owner reported");
      }
      case "first-frame" -> {
        RendererPreferences.save(vk);
        check(Startup.enabled(), "Eligible Vulkan request enabled");
        Startup.arm(); check(Files.exists(marker), "Attempt armed");
        Startup.firstFrame(); check(Files.exists(marker), "Early frame cannot clear recovery");
        System.setProperty("test.loaded","true");
        Startup.firstFrame(); check(!Files.exists(marker), "Successful loaded frame clears recovery");
        Startup.arm(); check(Files.exists(marker), "Repeated device attempt rearmed");
        Startup.fallback(); check(!Files.exists(marker), "Device fallback clears attempt");
        check(Startup.consumeWindowFallback(), "Window fallback consumed once");
        check(!Startup.consumeWindowFallback(), "Window fallback does not repeat");
      }
      default -> throw new AssertionError(args[0]);
    }
    System.out.println("RENDERER_STATUS_PASS scenario=" + args[0] + " checks=" + checks);
  }
}'''


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--java-home', type=pathlib.Path)
    args = parser.parse_args()
    suffix = '.exe' if __import__('os').name == 'nt' else ''
    javac = str(args.java_home / 'bin' / ('javac' + suffix)) if args.java_home else shutil.which('javac')
    java = str(args.java_home / 'bin' / ('java' + suffix)) if args.java_home else shutil.which('java')
    if not java or not javac:
        parser.error('A JDK is required; supply --java-home')
    screen = (SETTINGS / 'PrimalGraphicsScreen.java').read_text(encoding='utf-8')
    block = screen.split('      Control backend = controls.get("backend");', 1)[1].split('      Control preset =', 1)[0]
    block = 'Control backend = controls.get("backend");' + block
    legacy = (SETTINGS / 'GraphicsSettingsScreen.java').read_text(encoding='utf-8')
    assert 'if (backendSelected) RendererPreferences.save(backend);' in legacy
    assert 'if (c.key.equals("backend")) backendSelected = true;' in screen
    build = ROOT / 'build' / 'renderer-status-checks'
    build.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix='run-', dir=build) as temp:
        temp = pathlib.Path(temp)
        sources = []
        for name, source in STUBS.items():
            path = temp / name
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(source, encoding='utf-8')
            sources.append(path)
        checkfile = temp / 'com/shnewbs/deepprimal/settings/RendererStatusChecks.java'
        checkfile.write_text(CHECKS.replace('// EXTRACTED_SAVE_BLOCK', block), encoding='utf-8')
        sources += [checkfile, SETTINGS / 'RendererPreferences.java', SETTINGS / 'RendererStatus.java', STARTUP]
        classes = temp / 'classes'
        subprocess.run([javac, '-encoding', 'UTF-8', '-d', str(classes), *map(str, sources)], check=True, timeout=60)
        for scenario in ['matrix', 'marker', 'recovery', 'override', 'delegation', 'first-frame']:
            config = temp / ('config-' + scenario)
            subprocess.run([java, '-Xverify:all', '-Dtest.config=' + str(config), '-cp', str(classes),
                            'com.shnewbs.deepprimal.settings.RendererStatusChecks', scenario], check=True, timeout=30)
        # A control that restores the old unconditional-save bug must fail the marker regression.
        mutant = block.replace('if (backendSelected || !Objects.equals(values.get("backend"), baseline.get("backend")))', '')
        assert mutant != block, 'Missing mutation target'
        checkfile.write_text(CHECKS.replace('// EXTRACTED_SAVE_BLOCK', mutant), encoding='utf-8')
        subprocess.run([javac, '-encoding', 'UTF-8', '-cp', str(classes), '-d', str(classes), str(checkfile)], check=True, timeout=60)
        result = subprocess.run([java, '-Dtest.config=' + str(temp / 'config-negative'), '-cp', str(classes),
                                 'com.shnewbs.deepprimal.settings.RendererStatusChecks', 'marker'], capture_output=True, text=True, timeout=30)
        assert result.returncode != 0 and 'NoSuchFileException' in result.stderr, 'Old marker bug was not detected'
        print('RENDERER_STATUS_NEGATIVE_PASS old_unconditional_save_rejected=true')


if __name__ == '__main__':
    main()
