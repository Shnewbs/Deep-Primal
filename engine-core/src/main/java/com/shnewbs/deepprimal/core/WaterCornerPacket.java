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
    float targetPacket=b.getFloat(p+16),sourcePacket=b.getFloat(p+20);
    boolean finite=Float.isFinite(targetPacket)&&Float.isFinite(sourcePacket);
    int to=finite?Math.round(-targetPacket)-2:0,from=finite?Math.round(sourcePacket):0;
    int[] ages={(b.getShort(p+26)>>>8)&255,(b.getShort(p+28)>>>8)&255,(b.getShort(p+30)>>>8)&255,((to>>>20)&15)|(((from>>>20)&7)<<4)};
    long pairs=0;
    // Each base-289 digit must encode only a 0..16 direction pair. A legacy
    // five-bit ID of 17..31 would carry into the adjacent corner otherwise.
    // Nonfinite legacy packets fail closed to still water instead of animating
    // arbitrary flow directions.
    for(int n=0;n<4;n++){
      int target=finite?Math.min(16,(to>>>(n*5))&31):16;
      int source=finite?Math.min(16,(from>>>(n*5))&31):16;
      pairs+=(target*17L+source)*POW[n];
    }
    write(b,p,pairs,(int)(now-ages[0])&65535,(int)(now-ages[1])&65535,(int)(now-ages[2])&65535,(int)(now-ages[3])&65535,(b.get(p+33)&127)>=64);
  }
  public static boolean matches(ByteBuffer b,int p,int n,int to,int from,int age,long now){
    // Public scalar admission must agree with corner(), which maps invalid IDs to still water.
    int safeTo=to>=0&&to<=16?to:16,safeFrom=from>=0&&from<=16?from:16;
    if(target(b,p,n)!=safeTo)return false;int old=source(b,p,n);return old==safeTo||age(b,p,n,now)>=100?safeFrom==safeTo:old==safeFrom&&start(b,p,n)==(((int)now-age)&65535);
  }
  public static void corner(ByteBuffer b,int q,int n,int to,int from,int age,long now){
    int safeTo=to>=0&&to<=16?to:16,safeFrom=from>=0&&from<=16?from:16;
    long pairs=pairCode(b,q),old=pairs/POW[n]%289;pairs+=(safeTo*17L+safeFrom-old)*POW[n];
    int a=start(b,q,0),c=start(b,q,1),d=start(b,q,2),e=start(b,q,3),value=((int)now-age)&65535;
    switch(n){case 0->a=value;case 1->c=value;case 2->d=value;case 3->e=value;default->throw new IllegalArgumentException();}
    for(int v=0;v<4;v++){int p=q+v*36;write(b,p,pairs,a,c,d,e,opacityTarget(b,p)==127);}
  }
  private static int start(int a, int c, int marker, int n) {
    return switch (n) {
      case 0 -> (a >>> 1) & 65535;
      case 1 -> (a >>> 17) | ((c & 1) << 15);
      case 2 -> (c >>> 1) & 65535;
      case 3 -> (c >>> 17) | ((marker & 32) << 10);
      default -> throw new IllegalArgumentException();
    };
  }

  /** Read a quad's shared packet once; return only requested corners that need publication.
   * The packed target/source fields use five bits per corner and ages use eight. */
  public static int changedCorners(ByteBuffer b, int q, int mask, int targets, int sources,
      int ages, long now) {
    if (mask == 0) return 0;
    long pairs = pairCode(b, q);
    int a = word1(b, q), c = word2(b, q), marker = b.get(q + 33), changed = 0;
    for (int n = 0; n < 4; n++) {
      if ((mask & (1 << n)) == 0) continue;
      // Match patchCorners() admission: five-bit values 17..31 encode still water.
      int pair = (int) (pairs / POW[n] % 289), to = Math.min(16, (targets >>> (n * 5)) & 31);
      int from = Math.min(16, (sources >>> (n * 5)) & 31),
          oldStart = start(a, c, marker, n);
      boolean same = pair / 17 == to &&
          (pair % 17 == to || (((int) now - oldStart) & 65535) >= 100
              ? from == to
              : pair % 17 == from && oldStart == (((int) now - (ages >>> (n * 8) & 255)) & 65535));
      if (!same) changed |= 1 << n;
    }
    return changed;
  }

  /** Apply the changed-corner mask to a private quad, encoding each vertex only once.
   * Unselected starts/pairs and each vertex's opacity/light/nonmaterial bytes are retained. */
  public static void patchCorners(ByteBuffer b, int q, int mask, int targets, int sources,
      int ages, long now) {
    if (mask == 0) return;
    long pairs = pairCode(b, q);
    int w1 = word1(b, q), w2 = word2(b, q), marker = b.get(q + 33);
    int a = start(w1, w2, marker, 0), c = start(w1, w2, marker, 1);
    int d = start(w1, w2, marker, 2), e = start(w1, w2, marker, 3);
    for (int n = 0; n < 4; n++) {
      if ((mask & (1 << n)) == 0) continue;
      // Invalid five-bit directions must not carry into adjacent base-289 corners.
      long pair = Math.min(16, (targets >>> (n * 5)) & 31) * 17L
          + Math.min(16, (sources >>> (n * 5)) & 31);
      pairs += (pair - pairs / POW[n] % 289) * POW[n];
      int value = ((int) now - (ages >>> (n * 8) & 255)) & 65535;
      switch (n) { case 0 -> a = value; case 1 -> c = value; case 2 -> d = value; case 3 -> e = value; }
    }
    for (int v = 0; v < 4; v++) {
      int at = q + v * 36;
      write(b, at, pairs, a, c, d, e, opacityTarget(b, at) == 127);
    }
  }

  /** Rebase failed material corners over current accepted bytes; never restore retired fields.
   * Full monotonic elapsed time disambiguates the queued16-bit timestamps across later wraps. */
  public static void rebaseCorners(ByteBuffer current, int dst, ByteBuffer queued, int src,
      int mask, long queuedAt, long now) {
    long elapsed = Math.max(0, now - queuedAt);
    int targets = 0, sources = 0, ages = 0;
    for (int n = 0; n < 4; n++) {
      int to = target(queued, src, n);
      int age = elapsed >= 100 ? 100 : (int)Math.min(100, elapsed
          + ((queuedAt - start(queued, src, n)) & 65535));
      int from = age == 100 ? to : source(queued, src, n);
      targets |= to << (n * 5); sources |= from << (n * 5); ages |= age << (n * 8);
    }
    patchCorners(current, dst, mask, targets, sources, ages, now);
  }

  public static final int RETIRE_LIVE = 1, RETIRE_EXPIRED = 2;

  /** One read-only slice classification. Steady pairs need no further polling; live pairs
   * must remain scheduled, and expired pairs can be retired after a successful GPU write. */
  public static int retirementState(ByteBuffer b, int begin, int end, long now) {
    if (begin < 0 || end > b.limit() || begin > end || begin % 144 != 0 || end % 144 != 0)
      throw new IllegalArgumentException();
    int result = 0;
    for (int q = begin; q < end; q += 144) {
      if (!packed(b, q)) continue;
      long pairs = pairCode(b, q);
      for (int n = 0; n < 4; n++) {
        int pair = (int) (pairs / POW[n] % 289);
        if (pair / 17 != pair % 17)
          result |= age(b, q, n, now) < 100 ? RETIRE_LIVE : RETIRE_EXPIRED;
      }
      if (result == (RETIRE_LIVE | RETIRE_EXPIRED)) return result;
    }
    return result;
  }

  /** Read-only admission check: steady slices need no temporary copy or GPU write. */
  public static boolean needsRetirement(ByteBuffer b, int begin, int end, long now) {
    return (retirementState(b, begin, end, now) & RETIRE_EXPIRED) != 0;
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
