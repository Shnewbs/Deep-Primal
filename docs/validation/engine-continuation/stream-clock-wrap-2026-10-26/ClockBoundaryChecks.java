public class ClockBoundaryChecks {
 public static void main(String[] args) {
  var random=new java.util.Random(20261026L); int checks=0,legacyFailures=0;
  for(int i=0;i<100000;i++) {
   long start=i<6?new long[]{0,-1,Long.MAX_VALUE,Long.MIN_VALUE,Long.MAX_VALUE-1000000,Long.MIN_VALUE+1}[i]:random.nextLong();
   for(long budget:new long[]{1000000,2000000})
   for(long elapsed:new long[]{0,1,budget-1,budget,budget+1,3000000}) {
    long deadline=start+budget, now=start+elapsed;
    boolean expected=elapsed<budget;
    if((now-deadline<0)!=expected)throw new AssertionError("elapsed boundary");
    if((now<deadline)!=expected)legacyFailures++;
    checks++;
   }
  }
  if(legacyFailures==0)throw new AssertionError("missing wrap control");
  System.out.println("CLOCK_BOUNDARY_PASS checks="+checks+" legacy_disagreements="+legacyFailures);
 }
}
