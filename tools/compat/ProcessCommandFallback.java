package com.shnewbs.deepprimal.buildcompat;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.ProtectionDomain;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/** Development-only NeoForm 2.0.31 fallback when a container hides its process command. */
public final class ProcessCommandFallback {
  private static final String TARGET = "net/neoforged/neoform/runtime/engine/NeoFormEngine";
  private static final String ACTION = "net/neoforged/neoform/runtime/actions/ExternalJavaToolAction";
  private static final String SELF = "com/shnewbs/deepprimal/buildcompat/ProcessCommandFallback";
  private static final String EXPECTED_CLASS_SHA256 =
      "e2cfd2b7d08af46475bc74b84fd438103aadaab3c50a93aa6c804849a5e28290";
  private static final String EXPECTED_ACTION_SHA256 =
      "25294ada957ef5dc61153b734cba5eea1dd449c423f28825bb3e0a343877ac7d";

  public static void premain(String arguments, Instrumentation instrumentation) {
    instrumentation.addTransformer(new ClassFileTransformer() {
      @Override
      public byte[] transform(ClassLoader loader, String name, Class<?> type,
          ProtectionDomain protection, byte[] bytes) {
        String expected = TARGET.equals(name) ? EXPECTED_CLASS_SHA256
            : ACTION.equals(name) ? EXPECTED_ACTION_SHA256 : null;
        if (expected == null) return null;
        try {
          String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
          if (!hash.equals(expected))
            throw new IllegalStateException("Unrecognized NeoForm class; this shim is pinned to 2.0.31");
        } catch (NoSuchAlgorithmException exception) {
          throw new IllegalStateException(exception);
        }
        var writer = new ClassWriter(0);
        int[] replacements = {0};
        new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9, writer) {
          @Override
          public MethodVisitor visitMethod(int access, String method, String descriptor,
              String signature, String[] exceptions) {
            var original = super.visitMethod(access, method, descriptor, signature, exceptions);
            if (!method.equals(TARGET.equals(name) ? "<init>" : "run")) return original;
            return new MethodVisitor(Opcodes.ASM9, original) {
              @Override
              public void visitMethodInsn(int opcode, String owner, String called,
                  String desc, boolean itf) {
                if (opcode == Opcodes.INVOKEVIRTUAL && owner.equals("java/util/Optional")
                    && called.equals("orElseThrow") && desc.equals("()Ljava/lang/Object;")) {
                  replacements[0]++;
                  super.visitMethodInsn(Opcodes.INVOKESTATIC, SELF, "executable",
                      "(Ljava/util/Optional;)Ljava/lang/Object;", false);
                } else super.visitMethodInsn(opcode, owner, called, desc, itf);
              }
            };
          }
        }, 0);
        if (replacements[0] != 1)
          throw new IllegalStateException("Unexpected NeoForm host lookup; do not apply this shim");
        return writer.toByteArray();
      }
    });
  }

  public static Object executable(Optional<?> original) {
    if (original.isPresent()) return original.get();
    boolean windows = System.getProperty("os.name").startsWith("Windows");
    Path java = Path.of(System.getProperty("java.home"), "bin", windows ? "java.exe" : "java");
    if (!Files.isExecutable(java))
      throw new IllegalStateException("No executable in the running JDK's java.home");
    return java.toString();
  }

  private ProcessCommandFallback() {}
}
