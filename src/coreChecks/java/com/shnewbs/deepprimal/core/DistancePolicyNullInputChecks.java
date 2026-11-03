package com.shnewbs.deepprimal.core;

import java.util.OptionalInt;
import java.util.Random;

/** Fail-closed admission for absent distance text without changing valid settings. */
public final class DistancePolicyNullInputChecks {
  private static long checks;
  private static void check(boolean condition, String message) {
    checks++;
    if (!condition) throw new AssertionError(message);
  }

  private static OptionalInt reference(String text, int minimum, int maximum) {
    if (text == null || text.isEmpty() || text.length() > 10) return OptionalInt.empty();
    long parsed = 0;
    for (int i = 0; i < text.length(); i++) {
      char digit = text.charAt(i);
      if (digit < '0' || digit > '9') return OptionalInt.empty();
      parsed = parsed * 10 + digit - '0';
      if (parsed > maximum) return OptionalInt.empty();
    }
    return parsed >= minimum && parsed <= maximum
        ? OptionalInt.of((int)parsed) : OptionalInt.empty();
  }

  public static void main(String[] args) {
    // A missing persisted input is invalid, not an exception on the render/UI thread.
    check(DistancePolicy.parse(null, 125).isEmpty(), "null default minimum");
    check(DistancePolicy.parse(null, 0, 96).isEmpty(), "null full-height setting");
    check(DistancePolicy.parse(null, -1, 100).isEmpty(), "null custom minimum");
    for (String text : new String[]{"", "0", "00", "1", "2", "96", "125", "0000000125",
        "0000000000", "9999999999", "10000000000", "-1", "+2", "1.5", " 3", "3 ",
        "١", "９", "\u0000", "2147483647", "2147483648"}) {
      for (int min : new int[]{-1,0,1,2,125}) for (int max : new int[]{-1,0,32,96,125,Integer.MAX_VALUE}) {
        check(DistancePolicy.parse(text,min,max).equals(reference(text,min,max)), "edge: "+text);
        check(DistancePolicy.parse(text,max).equals(reference(text,2,max)), "default: "+text);
      }
    }
    Random random = new Random(0xD157AACE2026L);
    char[] alphabet = "0123456789abcXYZ-+ .".toCharArray();
    for (int i = 0; i < 250_000; i++) {
      int length = random.nextInt(13);
      StringBuilder text = new StringBuilder(length);
      for (int j = 0; j < length; j++) text.append(alphabet[random.nextInt(alphabet.length)]);
      int min = random.nextInt(130) - 2;
      int max = random.nextInt(260) - 2;
      String input = text.toString();
      check(DistancePolicy.parse(input,min,max).equals(reference(input,min,max)), "random parse "+i);
      check(DistancePolicy.parse(input,max).equals(reference(input,2,max)), "random default "+i);
    }
    check(DistancePolicy.parse("125",125).orElseThrow()==125,"maximum supported render");
    check(DistancePolicy.parse("126",125).isEmpty(),"native render cap");
    check(DistancePolicy.parse("0",0,96).orElseThrow()==0,"full height sentinel");
    check(DistancePolicy.MAX_RENDER==125,"native tracker limit unchanged");
    check(DistancePolicy.MAX_SIMULATION==32,"simulation cap unchanged");
    System.out.println("DISTANCE_NULL_INPUT_PASS checks="+checks+" randomInputs=250000 nullInputs=3");
  }
  private DistancePolicyNullInputChecks() {}
}
