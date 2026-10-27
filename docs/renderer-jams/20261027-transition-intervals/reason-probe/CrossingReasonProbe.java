import com.shnewbs.deepprimal.core.FrameIntervalWindow;
public class CrossingReasonProbe {
 public static void main(String[] args) {
  for(int f=0;f<8;f++) {
   var w=new FrameIntervalWindow(512);var world=new Object();
   w.observe(0,world,1920,1080,true,120,32,16,192);
   w.observe(7,world,1920,1080,true,120,32,16,192);
   w.observe(1007,f==0?new Object():world,f==1?1280:1920,f==2?720:1080,
       f!=3,f==4?60:120,f==5?16:32,f==6?8:16,f==7?256:192);
   if(w.transitionCount()!=1||w.transitionSnapshot().samples()!=1
      ||w.transitionSnapshot().p99Nanos()!=1000||w.snapshot().samples()!=0)
     throw new AssertionError("Context "+f);
  }
  System.out.println("CROSSING_REASON_PASS independentCoordinates=8 exactGap=1000 steadySamples=0");
 }
}
