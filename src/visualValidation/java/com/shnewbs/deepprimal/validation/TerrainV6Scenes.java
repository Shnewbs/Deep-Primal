package com.shnewbs.deepprimal.validation;
import com.google.gson.*;
import com.shnewbs.deepprimal.engine.*;
import java.nio.file.*;
import net.minecraft.client.Minecraft;

final class TerrainV6Scenes {
  static boolean handle(Minecraft mc,String action,Path out)throws Exception {
    if(!action.startsWith("terrain-v6="))return false;
    String name=action.substring(11);var sites=JsonParser.parseString(Files.readString(Path.of(System.getenv("DEEPPRIMAL_V6_SITES")))).getAsJsonObject();
    mc.getSingleplayerServer().execute(()->{
      var server=mc.getSingleplayerServer();var l=server.overworld();var g=(EngineChunkGenerator)l.getChunkSource().getGenerator();var t=g.geography();
      double x=-528,z=-816,y,yaw=0,pitch=30;
      if(name.equals("glacier")){var a=sites.getAsJsonArray("glacier");x=a.get(0).getAsDouble();z=a.get(1).getAsDouble();}
      if(name.equals("dunes")){
        double best=-1;
        for(int dz=6144;dz<=10240;dz+=128)for(int dx=-2048;dx<=2048;dx+=128){var s=t.sample(dx,dz);double supply=t.landscape.sandSupply(dx,dz,s.ground());if(!s.wet()&&s.ground()>108&&s.ground()<210&&s.slope()<.35&&supply>best){best=supply;x=dx;z=dz;}}
        if(best<.5)throw new AssertionError("Dune scene lacks sand supply");
      }
      y=t.column(x,z).ground()+32;z+=28;y=Math.max(y,t.column(x,z).ground()+16);yaw=180;
      if(name.equals("tunnel")){
        var a=sites.getAsJsonArray("tunnel");x=a.get(0).getAsDouble();z=a.get(1).getAsDouble();var h=t.hydraulicColumn(x,z);
        y=h.water()+.4;yaw=Math.toDegrees(Math.atan2(-h.flowX(),h.flowZ()));pitch=4;
      }
      var source=server.createCommandSourceStack();
      for(String command:new String[]{"gamemode spectator @a","time set noon","weather clear","effect give @a minecraft:night_vision 100000 0 true","tp @a "+x+" "+y+" "+z+" "+yaw+" "+pitch})server.getCommands().performPrefixedCommand(source,command);
      try{Files.writeString(out.resolve(name+"-camera.txt"),"x="+x+" y="+y+" z="+z+" yaw="+yaw+" pitch="+pitch+"\n");}catch(Exception e){throw new RuntimeException(e);}
      System.out.println("TERRAIN_V6_CAMERA_READY "+name);
    });mc.setScreen(null);mc.options.hideGui=true;return true;
  }
}
