package com.shnewbs.deepprimal.core;

public final class LegacyOreDigest {
  public static void main(String[] args)throws Exception{
    var hash=java.security.MessageDigest.getInstance("SHA-256");var random=new java.util.Random(491234L);int cases=0;
    for(long seed:new long[]{0,42,-914,Long.MIN_VALUE,Long.MAX_VALUE}){
      var geology=new Geology(new TerrainNoise(seed));
      for(int i=0;i<4000;i++){
        int x=random.nextInt(60000001)-30000000,z=random.nextInt(60000001)-30000000;
        var sample=new TerrainEngine.Sample(400,Double.NaN,random.nextDouble(),.5,random.nextDouble(),.1,0,0,0,0);
        var c=geology.column(x,z,sample);
        hash.update(c.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        for(int y=-128;y<=512;y+=3){hash.update((byte)c.ore(y,400).ordinal());hash.update((byte)c.rock(y).ordinal());cases++;}
      }
    }
    System.out.println("LEGACY_GEOLOGY_DIGEST cases="+cases+" sha256="+java.util.HexFormat.of().formatHex(hash.digest()));
  }
}
