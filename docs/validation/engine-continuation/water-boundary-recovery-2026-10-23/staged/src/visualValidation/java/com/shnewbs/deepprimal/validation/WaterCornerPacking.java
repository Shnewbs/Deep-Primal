package com.shnewbs.deepprimal.validation;
import java.nio.ByteBuffer;
final class WaterCornerPacking {
  static boolean packet(ByteBuffer b,int p){return b.get(p+33)<0;}
  static boolean spatial(ByteBuffer b,int p){return packet(b,p)||b.getFloat(p+16)<-1;}
  private static long[] words(ByteBuffer b,int p){
    int x=Float.floatToRawIntBits(b.getFloat(p+16)),y=Float.floatToRawIntBits(b.getFloat(p+20));
    long a=(x&0x7fffffL)|((x>>>1)&0x1f800000L),c=(y&0x7fffffL)|((y>>>1)&0x1f800000L)|((y>>>2)&0x20000000L);
    long w0=(a|((c&7)<<29))&0xffffffffL,w1=((c>>>3)|((long)(b.getShort(p+26)>>>8&31)<<27))&0xffffffffL;
    long w2=((b.getShort(p+26)>>>13&7L)|((b.getShort(p+28)>>>8&255L)<<3)|((b.getShort(p+30)>>>8&255L)<<11)|((b.getShort(p+28)&15L)<<19)|((b.getShort(p+30)&15L)<<23)|((b.get(p+33)&31L)<<27))&0xffffffffL;
    return new long[]{w0,w1,w2,(b.get(p+33)>>>5)&1};
  }
  static int[] ids(ByteBuffer b,int at,boolean target){
    if(packet(b,at)){var w=words(b,at);long code=w[0]|((w[1]&1)<<32);int[] out=new int[4];for(int n=0;n<4;n++){int pair=(int)(code%289);code/=289;out[n]=target?pair/17:pair%17;}return out;}
    int word=target?Math.round(-b.getFloat(at+16))-2:Math.round(b.getFloat(at+20));return new int[]{word&31,(word>>>5)&31,(word>>>10)&31,(word>>>15)&31};
  }
  static int[] ages(ByteBuffer b,int at,long now){
    if(packet(b,at)){var w=words(b,at);int[] starts={(int)(w[1]>>>1)&65535,(int)((w[1]>>>17)|((w[2]&1)<<15)),(int)(w[2]>>>1)&65535,(int)((w[2]>>>17)|(w[3]<<15))};int[] a=new int[4];for(int n=0;n<4;n++)a[n]=Math.min(100,((int)now-starts[n])&65535);return a;}
    int to=Math.round(-b.getFloat(at+16))-2,from=Math.round(b.getFloat(at+20));return new int[]{(b.getShort(at+26)>>>8)&255,(b.getShort(at+28)>>>8)&255,(b.getShort(at+30)>>>8)&255,((to>>>20)&15)|(((from>>>20)&7)<<4)};
  }
  static int light(ByteBuffer b,int p,int at){return b.getShort(p+at)&(packet(b,p)?240:255);}
  static int opacityTarget(ByteBuffer b,int p){return packet(b,p)?((b.get(p+33)&64)!=0?127:0):b.get(p+33)&255;}
  static int heightOffset(ByteBuffer b,int at){return spatial(b,at)?b.get(at+26):b.getShort(at+26);}
  static void uniform(ByteBuffer b,int at,int source,int target,int age,int cell,long now){
    if(!spatial(b,at)||(b.get(at+15)&255)!=cell)throw new AssertionError("Missing corner field/cell metadata");
    var targets=ids(b,at,true);var sources=ids(b,at,false);var ages=ages(b,at,now);
    for(int n=0;n<4;n++)if(targets[n]!=target||sources[n]!=(age>=100?target:source)||ages[n]!=age)throw new AssertionError("Corner endpoint/age mismatch n="+n+" actual="+sources[n]+"->"+targets[n]+" age="+ages[n]+" expected="+source+"->"+target+" age="+age);
  }
  private WaterCornerPacking(){}
}
