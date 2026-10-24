import java.nio.*;import java.util.*;import com.shnewbs.deepprimal.core.*;
public class WaterPresentationBudget {
 public static void main(String[] args){int bytes=(32*1024*1024/144)*144;ByteBuffer b=ByteBuffer.allocateDirect(bytes).order(ByteOrder.nativeOrder());
 for(int p=0;p<bytes;p+=36){b.putFloat(p+16,WaterFlowField.targets(0,100));b.putFloat(p+20,WaterFlowField.sources(0,100));b.putShort(p+26,(short)0x6400);b.putShort(p+28,(short)0x64b0);b.putShort(p+30,(short)0x64f0);b.put(p+34,(byte)-100);}
 for(int warm=0;warm<5;warm++)WaterPresentation.rebase(b,25);
 long[] times=new long[15];for(int i=0;i<times.length;i++){long start=System.nanoTime();WaterPresentation.rebase(b,25);times[i]=System.nanoTime()-start;}Arrays.sort(times);
 System.out.println("WATER_PRESENTATION_BUDGET bytes="+bytes+" vertices="+(bytes/36)+" samples=15 medianNanos="+times[7]+" p95Nanos="+times[14]+" extractionDeadlineNanos=2000000 fitsDeadline="+(times[14]<=2000000)+" scope=syntheticFullResidencyRebaseOnlyExcludesScanCopyGpuWrite");
 }
}
