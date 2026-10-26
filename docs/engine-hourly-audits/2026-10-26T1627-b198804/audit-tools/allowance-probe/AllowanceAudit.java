import java.math.BigInteger;public class AllowanceAudit {
  static int streamAllowance(int quota, long start, long now) {
    if (start == Long.MIN_VALUE || now < start) return quota;
    long phase = Math.min(19, now - start);
    return (int) (quota * (phase + 1) / 20);
  }
public static void main(String[]x){long start=Long.MIN_VALUE+1,now=Long.MAX_VALUE;int q=1024;BigInteger distance=BigInteger.valueOf(now).subtract(BigInteger.valueOf(start));int expected=distance.min(BigInteger.valueOf(19)).add(BigInteger.ONE).multiply(BigInteger.valueOf(q)).divide(BigInteger.valueOf(20)).intValueExact();int actual=streamAllowance(q,start,now);if(expected!=1024||actual!=-51)throw new AssertionError("characterization changed actual="+actual+" expected="+expected);System.out.println("PRODUCTION_ALLOWANCE_DEFECT_CONFIRMED actual="+actual+" expected="+expected+" exactSource=true BigIntegerOracle=true");}}