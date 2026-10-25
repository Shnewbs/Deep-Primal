import com.google.gson.*;
import java.util.Set;

/** Tests the extracted actual fixture transformation, not a replacement implementation. */
public class LegacyTemplateChecks {
  public static void main(String[] args) {
    int count=0;
    for(int rev=1;rev<=7;rev++)for(int material=0;material<=2;material++)for(int channel=0;channel<=2;channel++)for(boolean terrain:new boolean[]{false,true}){
      JsonObject source=new JsonObject();source.addProperty("type","deepprimal:continental_v7");
      source.add("biome_source",JsonParser.parseString("{\"type\":\"deepprimal:test\",\"palette\":[\"minecraft:plains\"]}"));
      source.addProperty("min_y",-128);source.addProperty("height",768);
      if(terrain)source.add("terrain",JsonParser.parseString("{\"profile\":\"sentinel\"}"));
      source.addProperty("material_revision",material);source.addProperty("channel_revision",channel);
      JsonObject original=source.deepCopy();JsonObject result=TemplateUnderTest.transform(source,rev);
      if(!source.equals(original))throw new AssertionError("mutated caller template");
      if(rev<6&&result.has("terrain"))throw new AssertionError("V"+rev+" codec cannot round-trip inherited terrain profile");
      if(rev<7&&(result.has("material_revision")||result.has("channel_revision")))throw new AssertionError("V7 revisions leaked");
      if(rev>=6&&terrain&&!result.get("terrain").equals(source.get("terrain")))throw new AssertionError("supported profile lost");
      if(rev==7&&(!result.get("material_revision").equals(source.get("material_revision"))||!result.get("channel_revision").equals(source.get("channel_revision"))))throw new AssertionError("V7 revisions changed");
      for(String key:Set.of("biome_source","min_y","height"))if(!source.get(key).equals(result.get(key)))throw new AssertionError("base field changed");
      if(!result.get("type").getAsString().equals("deepprimal:continental_v"+rev))throw new AssertionError("wrong revision");
      count++;
    }
    System.out.println("LEGACY_TEMPLATE_PASS cases="+count+" revisions=1..7 callerUnchanged=true nativeExecution=false");
  }
}
