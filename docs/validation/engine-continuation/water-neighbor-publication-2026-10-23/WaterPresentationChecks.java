import java.nio.*;
import java.util.*;
import com.shnewbs.deepprimal.core.*;

/** Independent decoding/clock and immutable-upload reproduction; not native GPU acceptance. */
public final class WaterPresentationChecks {
  static void require(boolean v,String message){if(!v)throw new AssertionError(message);}
  static int id(ByteBuffer b,int p,int corner,boolean source){int v=source?Math.round(b.getFloat(p+20)):Math.round(-b.getFloat(p+16))-2;return(v>>>(corner*5))&31;}
  static int age(ByteBuffer b,int p,int n){int to=Math.round(-b.getFloat(p+16))-2,from=Math.round(b.getFloat(p+20));return n==3?((to>>>20)&15)|(((from>>>20)&7)<<4):(b.getShort(p+(n==0?26:n==1?28:30))&65535)>>>8;}
  static double mix(int a,int c,int age,long elapsed){double t=Math.max(0,Math.min(1,(age+elapsed)/100.0));return a*(1-t)+c*t;}
  public static void main(String[] args){
    long tested=0;Random r=new Random(20261023);long[] elapsed={0,1,25,99,100,101,1000,32768,Long.MAX_VALUE};
    for(int trial=0;trial<20000;trial++)for(long dt:elapsed){
      ByteBuffer b=ByteBuffer.allocate(288).order(ByteOrder.LITTLE_ENDIAN);byte[] original=new byte[288];r.nextBytes(original);b.put(original).position(0);
      int to=0,from=0;int[] ages=new int[4];
      for(int n=0;n<4;n++){to|=r.nextInt(17)<<(n*5);from|=r.nextInt(17)<<(n*5);ages[n]=r.nextInt(101);}
      int height=-r.nextInt(101),sideHeight=-r.nextInt(32769),opacity=-r.nextInt(101),sideAge=r.nextInt(101);
      for(int v=0;v<8;v++){
        int p=v*36;b.put(p+34,(byte)opacity);
        if(v<4){b.putFloat(p+16,WaterFlowField.targets(to,ages[3]));b.putFloat(p+20,WaterFlowField.sources(from,ages[3]));b.putShort(p+26,(short)((height&255)|(ages[0]<<8)));b.putShort(p+28,(short)(176|(ages[1]<<8)));b.putShort(p+30,(short)(240|(ages[2]<<8)));}
        else{b.putFloat(p+16,.25f);b.putFloat(p+20,.75f);b.putShort(p+26,(short)sideHeight);b.putShort(p+28,(short)0x9ab0);b.putShort(p+30,(short)(32768|(sideAge<<8)|240));}
      }
      byte[] before=b.array().clone();WaterPresentation.rebase(b,dt);
      for(int v=0;v<8;v++){
        int p=v*36;
        for(int k=0;k<36;k++)if(!(k>=16&&k<=23&&v<4)&&!(k>=26&&k<=31)&&k!=34)require(b.get(p+k)==before[p+k],"Nonclock byte changed");
        require((b.getShort(p+28)&255)==176&&(b.getShort(p+30)&255)==240,"Light changed");
        require(b.get(p+34)==Math.max(-100,opacity-Math.min(dt,100)),"Opacity deadline moved");
        if(v<4){for(int n=0;n<4;n++){require(id(b,p,n,false)==((to>>>(n*5))&31)&&id(b,p,n,true)==((from>>>(n*5))&31),"Endpoint changed");require(age(b,p,n)==Math.min(100,ages[n]+Math.min(dt,100)),"Corner age changed");}require((byte)b.getShort(p+26)==Math.max(-100,height-Math.min(dt,100)),"Height clock");}
        else{require(b.getShort(p+26)==Math.max(-32768,sideHeight-Math.min(dt,32768)),"Side height clock");require((b.getShort(p+28)&65535)==0x9ab0,"Side presentation flags");require(((b.getShort(p+30)&65535)>>>8&127)==Math.min(100,sideAge+Math.min(dt,100)),"Side material clock");}
      }
      for(int n=0;n<4;n++){int target=r.nextInt(17),source=r.nextInt(17),a=r.nextInt(101);WaterPresentation.corner(b,0,n,target,source,a);for(int v=0;v<4;v++)require(id(b,v*36,n,false)==target&&id(b,v*36,n,true)==source&&age(b,v*36,n)==a,"Patched corner differs");}
      tested++;
    }
    var shared=new WaterFlowTransition(2,0);int residentDirection=2;shared.target(6,100,1);
    double oldUpload=mix(residentDirection,residentDirection,100,50),newUpload=mix(shared.source(),shared.target(),shared.age(100),50);
    require(oldUpload!=newUpload,"Expected resident upload defect not reproduced");
    System.out.println("WATER_RESIDENT_UPLOAD_REPRODUCED old="+oldUpload+" new="+newUpload+" source=2 target=6 elapsed=50");
    System.out.println("WATER_PRESENTATION_CORE_PASS cases="+tested+" geometryLightStable=true endpointAndClockRebase=true nativeGpuAcceptance=false");
  }
}
