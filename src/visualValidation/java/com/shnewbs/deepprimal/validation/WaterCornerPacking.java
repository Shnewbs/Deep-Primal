package com.shnewbs.deepprimal.validation;
import java.nio.ByteBuffer;
final class WaterCornerPacking {
  static boolean spatial(ByteBuffer b,int at){return b.getFloat(at+16)<-1;}
  static int[] ids(ByteBuffer b,int at,boolean target){int word=target?Math.round(-b.getFloat(at+16))-2:Math.round(b.getFloat(at+20));return new int[]{word&31,(word>>>5)&31,(word>>>10)&31,(word>>>15)&31};}
  static int[] ages(ByteBuffer b,int at){int to=Math.round(-b.getFloat(at+16))-2,from=Math.round(b.getFloat(at+20));return new int[]{(b.getShort(at+26)>>>8)&255,(b.getShort(at+28)>>>8)&255,(b.getShort(at+30)>>>8)&255,((to>>>20)&15)|(((from>>>20)&7)<<4)};}
  static int heightOffset(ByteBuffer b,int at){return spatial(b,at)?b.get(at+26):b.getShort(at+26);}
  static void uniform(ByteBuffer b,int at,int source,int target,int age,int cell){
    if(!spatial(b,at)||(b.get(at+15)&255)!=cell)throw new AssertionError("Missing corner field/cell metadata");
    var targets=ids(b,at,true);var sources=ids(b,at,false);var ages=ages(b,at);
    for(int n=0;n<4;n++)if(targets[n]!=target||sources[n]!=(age>=100?target:source)||ages[n]!=age)throw new AssertionError("Corner endpoint/age mismatch n="+n+" actual="+sources[n]+"->"+targets[n]+" age="+ages[n]+" expected="+source+"->"+target+" age="+age);
  }
  private WaterCornerPacking(){}
}
