package com.shnewbs.deepprimal.core;
import java.nio.*;
import java.util.*;
public final class WaterCornerLegacyAdmissionChecks {
 private static long checks;
 private static void need(boolean condition,String why){checks++;if(!condition)throw new AssertionError(why+" check="+checks);}
 private static ByteBuffer packet(int[] to,int[] from,int[] ages,boolean opaque){
  ByteBuffer b=ByteBuffer.allocate(36).order(ByteOrder.nativeOrder());
  int targetWord=0,sourceWord=0;
  for(int n=0;n<4;n++){targetWord|=to[n]<<(n*5);sourceWord|=from[n]<<(n*5);}
  targetWord|=(ages[3]&15)<<20;sourceWord|=(ages[3]>>>4)<<20;
  b.putFloat(16,-(float)targetWord-2f);b.putFloat(20,(float)sourceWord);
  b.putShort(26,(short)(77|(ages[0]<<8)));b.putShort(28,(short)(176|(ages[1]<<8)));
  b.putShort(30,(short)(240|(ages[2]<<8)));b.put(33,(byte)(opaque?127:0));
  return b;
 }
 private static void reference(ByteBuffer b,long now){
  int to=Math.round(-b.getFloat(16))-2,from=Math.round(b.getFloat(20));
  int[] ages={(b.getShort(26)>>>8)&255,(b.getShort(28)>>>8)&255,(b.getShort(30)>>>8)&255,((to>>>20)&15)|(((from>>>20)&7)<<4)};
  long pairs=0,power=1;
  for(int n=0;n<4;n++){pairs+=(((to>>>(n*5))&31)*17L+((from>>>(n*5))&31))*power;power*=289;}
  WaterCornerPacket.write(b,0,pairs,(int)(now-ages[0])&65535,(int)(now-ages[1])&65535,(int)(now-ages[2])&65535,(int)(now-ages[3])&65535,(b.get(33)&127)>=64);
 }
 private static void runCase(int[] to,int[] from,int[] ages,long now,boolean opaque){
  ByteBuffer b=packet(to,from,ages,opaque);byte[] original=b.array().clone();
  if(Arrays.stream(to).allMatch(v->v<=16)&&Arrays.stream(from).allMatch(v->v<=16)){
   ByteBuffer reference=packet(to,from,ages,opaque);reference(reference,now);
   WaterCornerPacket.fromLegacy(b,0,now);
   need(Arrays.equals(b.array(),reference.array()),"valid legacy byte parity");
  } else WaterCornerPacket.fromLegacy(b,0,now);
  for(int n=0;n<4;n++){
   need(WaterCornerPacket.target(b,0,n)==Math.min(16,to[n]),"invalid target or corner carry at "+n+" target="+to[n]);
   need(WaterCornerPacket.source(b,0,n)==Math.min(16,from[n]),"invalid source or corner carry at "+n+" source="+from[n]);
   need(WaterCornerPacket.age(b,0,n,now)==ages[n],"age changed "+n);
  }
  for(int k=0;k<36;k++)if(!(k>=16&&k<=23)&&!(k>=27&&k<=31)&&k!=33)
   need(b.get(k)==original[k],"nonmaterial byte changed "+k);
  need((b.get(33)&64)==(opaque?64:0),"opacity changed");
 }
 public static void main(String[] args){
  Random r=new Random(0x15411a7);
  for(int i=0;i<100000;i++){
   int[] to=new int[4],from=new int[4],ages=new int[4];
   for(int n=0;n<4;n++){to[n]=r.nextInt(17);from[n]=r.nextInt(17);ages[n]=r.nextInt(101);}
   runCase(to,from,ages,i*17L,r.nextBoolean());
  }
  for(int i=0;i<100000;i++){
   int[] to=new int[4],from=new int[4],ages=new int[4];
   for(int n=0;n<4;n++){to[n]=r.nextInt(32);from[n]=r.nextInt(32);ages[n]=r.nextInt(101);}
   runCase(to,from,ages,i*19L,r.nextBoolean());
  }
  for(int invalid=17;invalid<32;invalid++)for(int lane=0;lane<4;lane++){
   int[] to={1,2,3,4},from={5,6,7,8},ages={0,15,99,100};
   to[lane]=invalid;from[lane]=invalid;
   runCase(to,from,ages,65530L,true);
  }
  for(float malformed:new float[]{Float.NaN,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY}){
   ByteBuffer b=packet(new int[]{1,2,3,4},new int[]{5,6,7,8},new int[]{0,1,2,3},true);
   b.putFloat(16,malformed);WaterCornerPacket.fromLegacy(b,0,100);
   for(int n=0;n<4;n++){
    need(WaterCornerPacket.target(b,0,n)==16,"nonfinite target not still");
    need(WaterCornerPacket.source(b,0,n)==16,"nonfinite source not still");
   }
   b=packet(new int[]{1,2,3,4},new int[]{5,6,7,8},new int[]{0,1,2,3},true);
   b.putFloat(20,malformed);WaterCornerPacket.fromLegacy(b,0,100);
   for(int n=0;n<4;n++){
    need(WaterCornerPacket.target(b,0,n)==16,"nonfinite source-packet target not still");
    need(WaterCornerPacket.source(b,0,n)==16,"nonfinite source-packet source not still");
   }
  }
  System.out.println("WATER_CORNER_LEGACY_ADMISSION_PASS checks="+checks+" valid=100000 malformed=100000 invalidLanes=60 nonfinite=6");
 }
}
