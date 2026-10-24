import com.shnewbs.deepprimal.core.density.*;
import java.lang.management.*;
public class CompileProbe {
 static volatile Object sink;
 public static void main(String[]args){var d=TerrainShapeV8.definition(false);var b=(com.sun.management.ThreadMXBean)ManagementFactory.getThreadMXBean();b.setThreadAllocatedMemoryEnabled(true);for(int i=0;i<50;i++)sink=d.compile(i);long a=b.getThreadAllocatedBytes(Thread.currentThread().threadId()),t=System.nanoTime();for(int i=0;i<200;i++)sink=d.compile(i);long dt=System.nanoTime()-t,da=b.getThreadAllocatedBytes(Thread.currentThread().threadId())-a;System.out.println("COMPILE_PROBE n=200 meanNanos="+(dt/200.0)+" meanAllocatedBytes="+(da/200.0));}
}
