package com.shnewbs.deepprimal.validation;
import com.google.gson.*;
import com.shnewbs.deepprimal.engine.*;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

final class TerrainAuditScenes {
  static boolean handle(Minecraft mc,String action,Path out)throws Exception {
    if(!action.startsWith("audit="))return false;
    String name=action.substring(6);
    if(name.equals("smooth-check")) {
      if(com.shnewbs.deepprimal.client.render.AnimatedWaterRenderer.ownsWater()
          ||com.shnewbs.deepprimal.client.render.SmoothWaterRenderer.renderedCells==0)
        throw new AssertionError("Fallback renderer was not exercised");
      System.out.println("SMOOTH_WATER_RENDER_PASS cells="+com.shnewbs.deepprimal.client.render.SmoothWaterRenderer.renderedCells);return true;
    }
    if(name.contains("fallback")) {
      com.shnewbs.deepprimal.settings.WaterVisualConfig.INTERPOLATE.set(false);
      com.shnewbs.deepprimal.settings.WaterVisualConfig.SMOOTH.set(true);
      mc.levelRenderer.allChanged();
    }
    var scenes=name.startsWith("water-bed")?null:JsonParser.parseString(Files.readString(Path.of(System.getenv("DEEPPRIMAL_AUDIT_SITES")))).getAsJsonObject().getAsJsonObject("scenes");
    mc.getSingleplayerServer().execute(()->{
      var server=mc.getSingleplayerServer();var level=server.overworld();var source=server.createCommandSourceStack();
      var t=((EngineChunkGenerator)level.getChunkSource().getGenerator()).geography();double x,z,y,yaw=180,pitch=35;
      for(String command:new String[]{"gamemode spectator @a","time set noon","weather clear","effect give @a minecraft:night_vision 100000 0 true"})server.getCommands().performPrefixedCommand(source,command);
      if(name.startsWith("water-bed")) {
        // Static controlled rendering witness: matched native solid beds and 1/16-unit water.
        // Tick freeze keeps the two JARs' input identical; this is not a solver benchmark.
        server.getCommands().performPrefixedCommand(source,"tick freeze");
        for(int zz=-8;zz<=8;zz++)for(int xx=-12;xx<=12;xx++) {
          int floor=500-Math.floorDiv(xx+12,2);
          for(int yy=486;yy<=514;yy++) {
            var state=yy<=floor?Blocks.SANDSTONE.defaultBlockState():Blocks.AIR.defaultBlockState();
            if(Math.abs(zz)<7&&yy==floor+1)state=EngineRegistry.WATER_BLOCK.get().defaultBlockState()
                .setValue(EngineFluid.UNITS,name.contains("full")?12:1).setValue(EngineFluid.FLOW,0).setValue(EngineFluid.SPEED,3);
            level.setBlock(new BlockPos(xx,yy,zz),state,2|16);
          }
        }
        server.getCommands().performPrefixedCommand(source,"fillbiome -12 480 -8 12 516 8 minecraft:plains");
        if(!level.getBiome(new BlockPos(0,500,0)).is(net.minecraft.world.level.biome.Biomes.PLAINS))throw new AssertionError("Matched fixture biome");
        try {
          var digest=java.security.MessageDigest.getInstance("SHA-256");long units=0;
          for(int zz=-8;zz<=8;zz++)for(int xx=-12;xx<=12;xx++)for(int yy=486;yy<=514;yy++) {
            var state=level.getBlockState(new BlockPos(xx,yy,zz));digest.update((state.toString()+"\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));
            if(state.is(EngineRegistry.WATER_BLOCK.get()))units+=state.getValue(EngineFluid.UNITS);
          }
          var record=new JsonObject();record.addProperty("state_sha256",java.util.HexFormat.of().formatHex(digest.digest()));record.addProperty("water_units",units);record.addProperty("frozen",true);record.addProperty("biome","minecraft:plains");
          Files.writeString(out.resolve(name+"-input.json"),record.toString());
        } catch(Exception error){throw new RuntimeException(error);}
        x=21;z=23;y=517;yaw=136;pitch=39;
      } else {
        server.getCommands().performPrefixedCommand(source,"tick unfreeze");
        var at=scenes.getAsJsonArray(name);if(at==null)throw new AssertionError("Missing scene "+name);
        x=at.get(0).getAsDouble();z=at.get(1).getAsDouble();y=t.column(x,z).ground()+26;
        z+=30;y=Math.max(y,t.column(x,z).ground()+16);
      }
      server.getCommands().performPrefixedCommand(source,"tp @a "+x+" "+y+" "+z+" "+yaw+" "+pitch);
      try{Files.writeString(out.resolve(name+"-camera.txt"),"x="+x+" y="+y+" z="+z+" yaw="+yaw+" pitch="+pitch+"\n");}catch(Exception e){throw new RuntimeException(e);}
      System.out.println("TERRAIN_AUDIT_CAMERA_READY "+name);
    });mc.setScreen(null);mc.options.hideGui=true;return true;
  }
}
