package com.shnewbs.deepprimal.validation;

import java.nio.*;
import java.nio.file.*;
import java.util.*;
import net.vulkanmod.cache.BinaryCache;
import net.vulkanmod.vulkan.shader.DescriptorBindingState;
import net.vulkanmod.vulkan.shader.DescriptorStateCache;

public final class CacheChecks {
  private static void check(boolean value) {
    if (!value) throw new AssertionError();
  }

  private static String key(int i) {
    return BinaryCache.key(new byte[] {(byte) i, (byte) (i >> 8)});
  }

  public static void main(String[] args) throws Exception {
    Path dir = Files.createTempDirectory("dp-cache-checks");
    try {
      var cache = new BinaryCache(dir, 32, 144, 3);
      byte[] data = {1, 2, 3, 4};
      cache.write(key(0), data);
      check(Arrays.equals(data, cache.read(key(0))));
      Path file = dir.resolve(key(0) + ".bin");
      byte[] raw = Files.readAllBytes(file);
      raw[16] ^= 1;
      Files.write(file, raw);
      check(cache.read(key(0)) == null);
      cache.write(key(0), data);
      Files.write(file, new byte[] {1});
      check(cache.read(key(0)) == null);
      cache.write(key(0), new byte[33]);
      check(cache.read(key(0)) == null);
      for (int i = 0; i < 250; i++) {
        cache.write(key(i), data);
        check(Arrays.equals(data, cache.read(key(i))));
        try (var files = Files.list(dir)) {
          check(files.count() <= 3);
        }
      }
      check(cache.read(key(0)) == null);
      cache.read(key(247));
      cache.write(key(250), data);
      check(cache.read(key(248)) == null);
      check(cache.read(key(247)) != null);
      boolean rejected = false;
      try {
        cache.read("../escape");
      } catch (IllegalArgumentException expected) {
        rejected = true;
      }
      check(rejected);
      var states = new DescriptorStateCache(2);
      states.state[0] = 1;
      states.state[1] = 2;
      states.remember(0);
      states.state[1] = 3;
      states.remember(1);
      states.state[1] = 2;
      check(states.find() == 0);
      // Arrays.hashCode collisions must never count as descriptor equality.
      states.state[0] = 2;
      states.state[1] = -29;
      check(states.find() == -1);
      states.clear();
      check(states.find() == -1);
      var binding = new DescriptorBindingState();
      var offsets = IntBuffer.wrap(new int[] {11, 22});
      check(binding.changed(1, 2, 0, offsets));
      check(!binding.changed(1, 2, 0, offsets));
      offsets.put(1, 23);
      check(binding.changed(1, 2, 0, offsets));
      check(binding.changed(3, 2, 0, offsets));
      check(binding.changed(3, 4, 0, offsets));
      check(binding.changed(3, 4, 1, offsets));
      binding.reset();
      check(binding.changed(3, 4, 1, offsets));
      System.out.println(
          "CACHE_CHECKS_PASS corruption truncation bounds LRU250 exactDescriptorEquality"
              + " dynamicOffsets commandReset");
    } finally {
      try (var files = Files.walk(dir)) {
        for (Path p : files.sorted(Comparator.reverseOrder()).toList()) Files.delete(p);
      }
    }
  }
}
