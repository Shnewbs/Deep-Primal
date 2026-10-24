package com.shnewbs.deepprimal.core;

import java.nio.*;
import java.util.*;

/** Known input/field oracle for deferred material, current nonmaterial and long elapsed epochs. */
final class WaterCornerRetryChecks {
  static void need(boolean value,String why){if(!value)throw new AssertionError(why);}
  static void run(){
    Random random=new Random(0x432026);long cases=0;
    for(int trial=0;trial<10000;trial++)for(long base:new long[]{0,65520,65535,65536,131071,Long.MAX_VALUE-10000})for(int delta:new int[]{0,1,74,75,99,100,500,65536}){
      if(base>Long.MAX_VALUE-delta)continue;long now=base+delta;int mask=trial&15,dst=trial%2==0?0:144,src=144-dst;
      byte[] raw=new byte[432],old=new byte[288];random.nextBytes(raw);random.nextBytes(old);var live=ByteBuffer.wrap(raw).order(ByteOrder.nativeOrder());var queued=ByteBuffer.wrap(old).order(ByteOrder.nativeOrder());
      int[] targets=new int[4],sources=new int[4],ages=new int[4];long pairs=0,power=1;
      for(int n=0;n<4;n++){targets[n]=random.nextInt(17);sources[n]=random.nextInt(17);ages[n]=random.nextInt(201);pairs+=(targets[n]*17L+sources[n])*power;power*=289;}
      for(int v=0;v<4;v++){
        WaterCornerPacket.write(queued,src+v*36,pairs,(int)(base-ages[0])&65535,(int)(base-ages[1])&65535,(int)(base-ages[2])&65535,(int)(base-ages[3])&65535,random.nextBoolean());
        WaterCornerPacket.write(live,dst+v*36,random.nextLong(6975757441L),11,22,33,44,random.nextBoolean());
      }
      byte[] before=raw.clone(),queuedBefore=old.clone();var expected=ByteBuffer.wrap(raw.clone()).order(ByteOrder.nativeOrder());
      for(int n=0;n<4;n++)if((mask&(1<<n))!=0){int age=Math.min(100,ages[n]+delta);WaterCornerPacket.corner(expected,dst,n,targets[n],age==100?targets[n]:sources[n],age,now);}
      WaterCornerPacket.rebaseCorners(live,dst,queued,src,mask,base,now);
      need(Arrays.equals(raw,expected.array()),"Rebased packet differs from scalar known-input result");need(Arrays.equals(old,queuedBefore),"Queued data mutated");
      for(int v=0;v<4;v++)for(int n=0;n<4;n++)if((mask&(1<<n))!=0){int at=dst+v*36,age=Math.min(100,ages[n]+delta);need(WaterCornerPacket.target(live,at,n)==targets[n],"Target differs");need(WaterCornerPacket.source(live,at,n)==(age==100?targets[n]:sources[n]),"Expired source revived");need(WaterCornerPacket.start(live,at,n)==((int)(now-age)&65535),"Clock differs");}
      for(int at=0;at<raw.length;at++){int within=at-dst,field=Math.floorMod(within,36);if(within<0||within>=144||!(field>=16&&field<=23)&&!(field>=27&&field<=31)&&field!=33)need(raw[at]==before[at],"Current nonmaterial byte replaced");}
      cases++;
    }
    System.out.println("WATER_CORNER_RETRY_CORE_PASS cases="+cases+" masks=16 currentBytesPreserved=true expiredPairsSteady=true longElapsedWraps=true");
  }
  public static void main(String[]args){run();}
}
