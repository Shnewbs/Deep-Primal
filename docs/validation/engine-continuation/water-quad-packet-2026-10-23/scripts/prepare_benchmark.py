from pathlib import Path
R=Path(__file__).parent
common='''package com.shnewbs.deepprimal.core;
import java.nio.*;import java.nio.file.*;import java.security.*;import java.util.*;
public final class CornerPacketBenchmark {
 static final int QUADS=2048,WARMUP=16,SAMPLES=31,PASSES=16;
 static final int TARGETS=8*(1+32+1024+32768),SOURCES=4*(1+32+1024+32768),AGES=25*0x01010101;

 METHOD
 public static void main(String[] args)throws Exception {
  Path out=Path.of(args[0]);Files.createDirectories(out);
  for(int mask:new int[]{3,15}) {
   byte[] bytes=new byte[QUADS*144];new Random(1234).nextBytes(bytes);var b=ByteBuffer.wrap(bytes).order(ByteOrder.nativeOrder());
   for(int q=0;q<QUADS;q++)for(int v=0;v<4;v++)WaterCornerPacket.write(b,q*144+v*36,72L*(1+289+83521+24137569L),0,0,0,0,(v&1)==0);
   Files.write(out.resolve("initial-"+mask+".bin"),bytes);long[] times=new long[SAMPLES];
   for(int round=0;round<WARMUP+SAMPLES;round++) {
    long begin=System.nanoTime();
    for(int pass=0;pass<PASSES;pass++)apply(b,mask,65520+round*(long)PASSES+pass);
    long elapsed=System.nanoTime()-begin;if(round>=WARMUP)times[round-WARMUP]=elapsed;
   }
   long[] sorted=times.clone();Arrays.sort(sorted);Files.write(out.resolve("final-"+mask+".bin"),bytes);
   System.out.println("{\\\"variant\\\":\\\"VARIANT\\\",\\\"mask\\\":"+mask+",\\\"quads\\\":"+QUADS+",\\\"passes_per_sample\\\":"+PASSES+",\\\"warmup_rounds\\\":"+WARMUP+",\\\"samples\\\":"+SAMPLES+",\\\"sample_nanos\\\":"+Arrays.toString(times)+",\\\"median_nanos\\\":"+sorted[SAMPLES/2]+",\\\"p95_nanos\\\":"+sorted[(int)Math.ceil(SAMPLES*.95)-1]+",\\\"declared_corner_updates\\\":"+(QUADS*(WARMUP+SAMPLES)*PASSES*(mask==3?2:6))+",\\\"sha256\\\":\\\""+HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes))+"\\\"}");
  }
 }
}
'''.replace('\\\"','\\"')
methods={
'scalar':'''static void apply(ByteBuffer b,int mask,long now) {
 for(int q=0;q<QUADS;q++)for(int n=0;n<4;n++)if((mask&(1<<n))!=0&&!WaterCornerPacket.matches(b,q*144,n,8,4,25,now)){WaterCornerPacket.corner(b,q*144,n,8,4,25,now);}
}''',
'bulk':'''static void apply(ByteBuffer b,int mask,long now) {
 for(int q=0;q<QUADS;q++){int m=WaterCornerPacket.changedCorners(b,q*144,mask,TARGETS,SOURCES,AGES,now);if(m!=0){WaterCornerPacket.patchCorners(b,q*144,m,TARGETS,SOURCES,AGES,now);}}
}'''}
for name,method in methods.items():
 d=R/'benchmarks'/name;d.mkdir(parents=True,exist_ok=True);(d/'CornerPacketBenchmark.java').write_text(common.replace(' METHOD',method).replace('VARIANT',name))
