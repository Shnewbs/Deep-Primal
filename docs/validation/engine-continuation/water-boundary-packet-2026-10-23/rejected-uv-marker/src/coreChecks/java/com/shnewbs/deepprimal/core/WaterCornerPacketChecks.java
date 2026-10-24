package com.shnewbs.deepprimal.core;
import java.nio.*;import java.util.*;
/** Independent input-oracle checks; native shader decoding is checked separately. */
final class WaterCornerPacketChecks {
 static void need(boolean b,String m){if(!b)throw new AssertionError(m);}
 static void run(){
  Random r=new Random(20261023);long cases=0;long[] clocks={0,100,65520,65535,65536,131071,Long.MAX_VALUE-1000};
  for(int trial=0;trial<30000;trial++)for(long now:clocks){
   ByteBuffer b=ByteBuffer.allocate(144).order(ByteOrder.nativeOrder());byte[] raw=new byte[144];r.nextBytes(raw);b.put(raw);
   int to=0,from=0;int[] ages=new int[4],targets=new int[4],sources=new int[4];for(int n=0;n<4;n++){targets[n]=r.nextInt(17);sources[n]=r.nextInt(17);ages[n]=r.nextInt(101);to|=targets[n]<<(n*5);from|=sources[n]<<(n*5);}
   boolean opaque=r.nextBoolean();int height=-r.nextInt(101);
   for(int v=0;v<4;v++){int p=v*36;b.putFloat(p+16,WaterFlowField.targets(to,ages[3]));b.putFloat(p+20,WaterFlowField.sources(from,ages[3]));b.putShort(p+26,(short)((height&255)|(ages[0]<<8)));b.putShort(p+28,(short)(176|(ages[1]<<8)));b.putShort(p+30,(short)(240|(ages[2]<<8)));b.put(p+33,(byte)(opaque?127:0));}
   byte[] before=b.array().clone();for(int v=0;v<4;v++)WaterCornerPacket.fromLegacy(b,v*36,now);
   for(int v=0;v<4;v++){int p=v*36;need(Float.isFinite(b.getFloat(p+16))&&b.getFloat(p+16)>=2&&Float.isFinite(b.getFloat(p+20))&&Math.abs(b.getFloat(p+20))>=2,"Non-normalized packet float");
    for(int n=0;n<4;n++){need(WaterCornerPacket.target(b,p,n)==targets[n]&&WaterCornerPacket.source(b,p,n)==sources[n],"IDs");need(WaterCornerPacket.age(b,p,n,now)==ages[n],"Epoch age");}
    need(WaterCornerPacket.light(b,p,28)==176&&WaterCornerPacket.light(b,p,30)==240&&WaterCornerPacket.opacityTarget(b,p)==(opaque?127:0),"Light/target opacity");
    for(int k=0;k<36;k++)if(!(k>=16&&k<=23)&&!(k>=27&&k<=31)&&k!=33)need(b.get(p+k)==before[p+k],"Nonmaterial byte modified");
   }
   WaterCornerPacket.retire(b,0,144,now+101);
   for(int n=0;n<4;n++){need(WaterCornerPacket.source(b,0,n)==targets[n],"Expired source not retired");need(WaterCornerPacket.source(b,0,n)==WaterCornerPacket.target(b,0,n),"Wrap could revive retired transition");}
   int slot=r.nextInt(4),target=r.nextInt(17),source=r.nextInt(17),age=r.nextInt(101);WaterCornerPacket.corner(b,0,slot,target,source,age,now);
   for(int v=0;v<4;v++)need(WaterCornerPacket.target(b,v*36,slot)==target&&WaterCornerPacket.source(b,v*36,slot)==source&&WaterCornerPacket.age(b,v*36,slot,now)==age,"Partial corner patch");cases++;
  }
  int frames=(33554432+WaterCornerPacket.SLICE_BYTES-1)/WaterCornerPacket.SLICE_BYTES+(2048+31)/32;
  need(frames==193&&frames*250<65536-100,"Retirement sweep cannot beat wrap");
  System.out.println("WATER_CORNER_PACKET_CORE_PASS cases="+cases+" payloadBits=97 directionStates=17 epochBits=16 floatInputsNormalizedFinite=true heightOpacityGeometryLightPreserved=true retirementFramesBound="+frames+" maxClockMillisPerFrame=250");
 }
}
