import com.shnewbs.deepprimal.core.*; import java.nio.file.*;
public class PackagedFrameChecks { public static void main(String[] args) throws Exception {
 if (!Path.of(FrameIntervalWindow.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toRealPath().equals(Path.of(args[0]).toRealPath())) throw new AssertionError("Wrong class source");
 FrameIntervalChecks.main(args); System.out.println("PACKAGED_FRAME_CLASS_SOURCE_PASS"); } }
