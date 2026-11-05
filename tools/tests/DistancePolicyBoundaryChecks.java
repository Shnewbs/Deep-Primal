import com.shnewbs.deepprimal.core.DistancePolicy;
import java.util.OptionalInt;

/** Source-only regression checks for native render tracking limits and overflow safety. */
public final class DistancePolicyBoundaryChecks {
    private static int assertions;
    private static void check(boolean ok, String message) {
        assertions++;
        if (!ok) throw new AssertionError(message);
    }
    private static void rejects(String input, int min, int max) {
        check(DistancePolicy.parse(input, min, max).isEmpty(), "Accepted invalid distance: " + input);
    }
    public static void main(String[] args) {
        check(DistancePolicy.MAX_RENDER == 125, "Signed-byte tracker distance limit changed");
        check(DistancePolicy.MAX_SIMULATION == 32, "Simulation limit changed");
        for (int distance = -100; distance <= 250; distance++) {
            int render = DistancePolicy.clampRender(distance);
            int simulation = DistancePolicy.clampSimulation(distance);
            check(render >= 2 && render <= 125, "Render clamp out of range");
            check(simulation >= 2 && simulation <= 32, "Simulation clamp out of range");
            check(DistancePolicy.queuePriorityCount(0, distance) >= render + 3,
                    "Priority queue cannot represent tracker sentinel");
        }
        check(DistancePolicy.parse("125", 125).equals(OptionalInt.of(125)), "Maximum not accepted");
        check(DistancePolicy.parse("0002", 125).equals(OptionalInt.of(2)), "Zero padding changed");
        for (String invalid : new String[]{null, "", " ", "-1", "+2", "1.5", "１２", "126", "99999999999"}) {
            rejects(invalid, 2, 125);
        }
        check(DistancePolicy.sectionSlots(0, 1) == 1, "Origin section window incorrect");
        check(DistancePolicy.sectionSlots(125, 24) == 1_512_024L, "Native render window estimate incorrect");
        check(DistancePolicy.sectionSlots(Integer.MAX_VALUE, Integer.MAX_VALUE) == Long.MAX_VALUE,
                "Overflow must saturate instead of wrapping");
        check(DistancePolicy.sectionSlots(Integer.MAX_VALUE, 0) == 0, "Empty vertical window must be zero");
        for (int[] invalid : new int[][]{{-1, 1}, {1, -1}}) {
            try {
                DistancePolicy.sectionSlots(invalid[0], invalid[1]);
                throw new AssertionError("Negative section window accepted");
            } catch (IllegalArgumentException expected) {
                assertions++;
            }
        }
        System.out.println("DISTANCE_POLICY_BOUNDARY_PASS assertions=" + assertions);
    }
}
