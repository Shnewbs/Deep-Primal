package com.shnewbs.deepprimal.core;

import java.nio.ByteBuffer;

/** Four direction pairs and 16-bit presentation starts in existing normalized finite UV bits.
 * Expired pairs must be retired within 65536 ms; the renderer services bounded resident slices. */
public final class WaterCornerPacket {
  public static final int PERIOD = 65536, SLICE_BYTES = 144 * 1820;
  private static final long[] POW = {1,289,83521,24137569};
  public static boolean packed(ByteBuffer b,int p){return b.get(p+33)<0;}
  private static int unpack(float f,boolean signed){
    int v=Float.floatToRawIntBits(f);return (v&0x7fffff)|((v>>>1)&0x1f800000)|(signed?((v>>>2)&0x20000000):0);
  }
  private static float pack(int v,boolean signed){return Float.intBitsToFloat(0x40000000|(v&0x7fffff)|((v&0x1f800000)<<1)|(signed?((v&0x20000000)<<2):0));}
  private static long pairCode(ByteBuffer b,int p){int a=unpack(b.getFloat(p+16),false),c=unpack(b.getFloat(p+20),true);return Integer.toUnsignedLong(a|((c&7)<<29))|((long)((c>>>3)&1)<<32);}
  private static int word1(ByteBuffer b,int p){return(unpack(b.getFloat(p+20),true)>>>3)|((b.getShort(p+26)>>>8&31)<<27);}
  private static int word2(ByteBuffer b,int p){return(b.getShort(p+26)>>>13&7)|((b.getShort(p+28)>>>8&255)<<3)|((b.getShort(p+30)>>>8&255)<<11)|((b.getShort(p+28)&15)<<19)|((b.getShort(p+30)&15)<<23)|((b.get(p+33)&31)<<27);}
  public static int target(ByteBuffer b,int p,int n){return(int)(pairCode(b,p)/POW[n]%289)/17;}
  public static int source(ByteBuffer b,int p,int n){return(int)(pairCode(b,p)/POW[n]%289)%17;}
  public static int start(ByteBuffer b,int p,int n){int a=word1(b,p),c=word2(b,p);return switch(n){case 0->(a>>>1)&65535;case 1->(a>>>17)|((c&1)<<15);case 2->(c>>>1)&65535;case 3->(c>>>17)|((b.get(p+33)&32)<<10);default->throw new IllegalArgumentException();};}
  public static int age(ByteBuffer b,int p,int n,long now){return Math.min(100,((int)now-start(b,p,n))&65535);}
  public static int light(ByteBuffer b,int p,int at){return b.getShort(p+at)&240;}
  public static int opacityTarget(ByteBuffer b,int p){return(b.get(p+33)&64)!=0?127:0;}
  public static void write(ByteBuffer b,int p,long pairs,int a,int c,int d,int e,boolean opaque){
    int w0=(int)pairs,w1=(int)(pairs>>>32)|(a<<1)|(c<<17),w2=(c>>>15)|(d<<1)|(e<<17),w3=e>>>15;
    int normal=(opaque?64:0)|(w2>>>27)|((w3&1)<<5);
    // SNORM maps both -128 and -127 to -1. The unused first-UV sign distinguishes them.
    float first=pack(w0&0x1fffffff,false);b.putFloat(p+16,normal==0?-first:first);b.putFloat(p+20,pack((w0>>>29)|((w1&0x07ffffff)<<3),true));
    b.putShort(p+26,(short)((b.getShort(p+26)&255)|(((w1>>>27)|((w2&7)<<5))<<8)));
    b.putShort(p+28,(short)((b.getShort(p+28)&240)|((w2>>>19)&15)|(((w2>>>3)&255)<<8)));
    b.putShort(p+30,(short)((b.getShort(p+30)&240)|((w2>>>23)&15)|(((w2>>>11)&255)<<8)));
    b.put(p+33,(byte)(128|normal));
  }
  public static void fromLegacy(ByteBuffer b,int p,long now){
    int to=Math.round(-b.getFloat(p+16))-2,from=Math.round(b.getFloat(p+20));
    int[] ages={(b.getShort(p+26)>>>8)&255,(b.getShort(p+28)>>>8)&255,(b.getShort(p+30)>>>8)&255,((to>>>20)&15)|(((from>>>20)&7)<<4)};
    long pairs=0;for(int n=0;n<4;n++)pairs+=(((to>>>(n*5))&31)*17L+((from>>>(n*5))&31))*POW[n];
    write(b,p,pairs,(int)(now-ages[0])&65535,(int)(now-ages[1])&65535,(int)(now-ages[2])&65535,(int)(now-ages[3])&65535,(b.get(p+33)&127)>=64);
  }
  public static boolean matches(ByteBuffer b,int p,int n,int to,int from,int age,long now){
    if(target(b,p,n)!=to)return false;int old=source(b,p,n);return old==to||age(b,p,n,now)>=100?from==to:old==from&&start(b,p,n)==(((int)now-age)&65535);
  }
  public static void corner(ByteBuffer b,int q,int n,int to,int from,int age,long now){
    long pairs=pairCode(b,q),old=pairs/POW[n]%289;pairs+=(to*17L+from-old)*POW[n];
    int a=start(b,q,0),c=start(b,q,1),d=start(b,q,2),e=start(b,q,3),value=((int)now-age)&65535;
    switch(n){case 0->a=value;case 1->c=value;case 2->d=value;case 3->e=value;default->throw new IllegalArgumentException();}
    for(int v=0;v<4;v++){int p=q+v*36;write(b,p,pairs,a,c,d,e,opacityTarget(b,p)==127);}
  }
  /** Read-only admission check: steady slices need no temporary copy or GPU write. */
  public static boolean needsRetirement(ByteBuffer b, int begin, int end, long now) {
    if (begin < 0 || end > b.limit() || begin > end || begin % 144 != 0 || end % 144 != 0)
      throw new IllegalArgumentException();
    for (int q = begin; q < end; q += 144) {
      if (!packed(b, q)) continue;
      long pairs = pairCode(b, q);
      for (int n = 0; n < 4; n++) {
        int pair = (int) (pairs / POW[n] % 289);
        if (pair / 17 != pair % 17 && age(b, q, n, now) >= 100) return true;
      }
    }
    return false;
  }
  public static boolean retire(ByteBuffer b,int begin,int end,long now){
    if(begin<0||end>b.limit()||begin%144!=0||end%144!=0)throw new IllegalArgumentException();boolean changed=false;
    for(int q=begin;q<end;q+=144){
      if(!packed(b,q))continue;long pairs=pairCode(b,q),next=0;boolean expired=false;
      for(int n=0;n<4;n++){int pair=(int)(pairs/POW[n]%289),to=pair/17,from=pair%17;if(to!=from&&age(b,q,n,now)>=100){from=to;expired=true;}next+=(to*17L+from)*POW[n];}
      if(expired){int a=start(b,q,0),c=start(b,q,1),d=start(b,q,2),e=start(b,q,3);for(int v=0;v<4;v++){int p=q+v*36;write(b,p,next,a,c,d,e,opacityTarget(b,p)==127);}changed=true;}
    }
    return changed;
  }
  private WaterCornerPacket(){}
}
