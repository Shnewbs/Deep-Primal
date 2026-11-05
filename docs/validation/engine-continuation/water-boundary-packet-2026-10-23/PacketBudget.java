import java.nio.*;import java.util.*;import com.shnewbs.deepprimal.core.*;
class PacketBudget {
 public static void main(String[]args)throws Exception{int bytes=(32*1024*1024/144)*144;ByteBuffer b=ByteBuffer.allocateDirect(bytes).order(ByteOrder.nativeOrder());long active=0;for(int n=0;n<4;n++)active+=(6*17L+2)*new long[]{1,289,83521,24137569}[n];
 for(int p=0;p<bytes;p+=36){b.putShort(p+28,(short)176);b.putShort(p+30,(short)240);WaterCornerPacket.write(b,p,active,0,0,0,0,true);}
 for(int warm=0;warm<5;warm++)WaterCornerPacket.retire(b,0,WaterCornerPacket.SLICE_BYTES,50);
 long[] times=new long[(bytes+WaterCornerPacket.SLICE_BYTES-1)/WaterCornerPacket.SLICE_BYTES];int count=0;boolean modified=false;
 for(int start=0;start<bytes;start+=WaterCornerPacket.SLICE_BYTES){int end=Math.min(bytes,start+WaterCornerPacket.SLICE_BYTES);long t=System.nanoTime();modified|=WaterCornerPacket.retire(b,start,end,101);times[count++]=System.nanoTime()-t;}
 for(int q=0;q<bytes;q+=144)for(int n=0;n<4;n++)if(WaterCornerPacket.source(b,q,n)!=6||WaterCornerPacket.target(b,q,n)!=6)throw new AssertionError("Full slice retirement incomplete");
 try(var in=WaterCornerPacket.class.getResourceAsStream("WaterCornerPacket.class")){System.out.println("PACKET_BUDGET_CLASS_IDENTITY sha256="+java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(in.readAllBytes()))+" origin="+WaterCornerPacket.class.getProtectionDomain().getCodeSource().getLocation());}
 Arrays.sort(times);System.out.println("PACKET_RETIRE_BUDGET fullBytes="+bytes+" sliceBytes="+WaterCornerPacket.SLICE_BYTES+" slices="+count+" medianNanos="+times[count/2]+" p95Nanos="+times[(int)Math.ceil(count*.95)-1]+" maxNanos="+times[count-1]+" targetNanos=2000000 allActiveExpired="+modified+" scope=boundedMetadataRetirementCpuOnlyExcludesGpuCopyAndBoundaryPatch");
 }
}
