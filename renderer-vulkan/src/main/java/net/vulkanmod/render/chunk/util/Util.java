// VulkanMod 0.6.8+26.1.2, Collateral and contributors. LGPL-3.0-only.
// Reconstructed from the matching published artifact; modified for Deep Primal on 2026-10-19.
package net.vulkanmod.render.chunk.util;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;

public class Util {
   public static final Direction[] DIRECTIONS = Direction.values();
   public static final Direction[] XZ_DIRECTIONS = getXzDirections();

   public static byte getOppositeDirIdx(byte idx) {
      return (byte)((idx & 1) != 0 ? idx - 1 : idx + 1);
   }

   private static Direction[] getXzDirections() {
      Direction[] directions = new Direction[4];
      int i = 0;

      for (Direction direction : Direction.values()) {
         if (direction.getAxis() == Axis.X || direction.getAxis() == Axis.Z) {
            directions[i] = direction;
            i++;
         }
      }

      return directions;
   }

   public static long posLongHash(int x, int y, int z) {
      return x & 65535L | (long)z << 16 & 4294901760L | (long)y << 32 & 281470681743360L;
   }

   public static int flooredLog(int v) {
      assert v > 0;

      int log = 30;

      for (int t = 1073741824; (v & t) == 0; log--) {
         t >>= 1;
      }

      return log;
   }

   public static long align(long l, int alignment) {
      if (alignment == 0) {
         return l;
      } else {
         long r = l % alignment;
         return r != 0L ? l + alignment - r : l;
      }
   }
}
