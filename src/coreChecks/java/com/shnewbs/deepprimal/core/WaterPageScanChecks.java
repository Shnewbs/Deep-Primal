package com.shnewbs.deepprimal.core;

import java.util.*;

public final class WaterPageScanChecks {
  private static long checks;
  private static void need(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
  public static void main(String[] args) {
    for(int base:new int[]{-100,0,73})for(int radius=0;radius<=8;radius++) {
      var scan=new WaterPageScan(base,2,-base,radius,-8,40);
      var seen=new HashSet<String>();int[] lastRing={-1},lastYDistance={-1};
      String[] column={""};
      while(!scan.done()) {
        int n=scan.drain(17,(x,y,z)->{
          int ring=Math.max(Math.abs(x-base),Math.abs(z+base));
          need(ring>=lastRing[0],"Far columns beat nearby water");lastRing[0]=ring;
          String c=x+":"+z;
          if(!column[0].equals(c)){lastYDistance[0]=-1;column[0]=c;}
          need(Math.abs(y-2)>=lastYDistance[0],"Height priority");lastYDistance[0]=Math.abs(y-2);
          need(seen.add(x+":"+y+":"+z),"Duplicate page");return true;
        });
        need(n<=17,"Exceeded page budget");
      }
      need(seen.size()==(2*radius+1)*(2*radius+1)*48,"Skipped page");
    }
    var scan=new WaterPageScan(0,0,0,125,-8,40);
    int[] refused={0};scan.drain(512,(x,y,z)->{need(x==0&&y==0&&z==0,"First nearby page");refused[0]++;return false;});
    need(!scan.done() && refused[0]==1,"Rejected work advanced");
    scan.drain(1,(x,y,z)->{need(x==0&&y==0&&z==0,"Retry dropped page");return true;});
    need(scan.shouldRetarget(0,1,0,125,-8,40),"Vertical travel did not retarget");
    need(scan.shouldRetarget(0,0,0,124,-8,40),"Distance did not retarget");
    for(int low:new int[]{Integer.MIN_VALUE,Integer.MAX_VALUE-48}) {
      var extreme=new WaterPageScan(0,low,0,0,low,low+48);int[] count={0};
      while(!extreme.done())extreme.drain(7,(x,y,z)->{need(y>=low && (long)y< (long)low+48,"Signed height wrap");count[0]++;return true;});
      need(count[0]==48,"Extreme coverage");
    }
    System.out.println("WATER_PAGE_SCAN_PASS checks="+checks+" nearestFirst=true resumable=true radius125FirstPage=1");
  }
}
