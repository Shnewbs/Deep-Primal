package com.shnewbs.deepprimal.data;

import com.shnewbs.deepprimal.core.SoilTexture;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Locale;

/** Deterministic build-time content generation from the same catalog used by runtime registration. */
public final class SoilResources {
  private static void write(Path root, String path, String json) throws IOException {
    Path target = root.resolve(path);
    Files.createDirectories(target.getParent());
    Files.writeString(target, json + "\n", StandardCharsets.UTF_8);
  }

  public static void main(String[] args) throws IOException {
    Path output = Path.of(args[0]);
    var names = new ArrayList<String>();
    var ids = new ArrayList<String>();
    var grassIds = new ArrayList<String>();
    var sandIds = new ArrayList<String>();
    for (var soil : SoilTexture.values())
      for (String cover : new String[] {"", "_grass", "_podzol"}) {
        String name = "soil_" + soil.id() + cover;
        String id = "deepprimal:" + name;
        ids.add("\"" + id + "\"");
        if (cover.equals("_grass")) grassIds.add("\"" + id + "\"");
        if (soil == SoilTexture.SAND) sandIds.add("\"" + id + "\"");
        String mineral = soil == SoilTexture.SAND ? "sand" : "dirt_top";
        String top = cover.isEmpty() ? mineral : cover.equals("_grass") ? "grass_top" : "podzol_top";
        String side = cover.isEmpty() ? "deepprimal:block/ground/" + mineral
            : "deepprimal:block/soil/" + soil.id() + cover + "_side";
        String particle = cover.isEmpty() ? mineral : cover.equals("_grass") ? "grass_side" : "podzol_side";
        write(output, "assets/deepprimal/blockstates/" + name + ".json",
            "{\"variants\":{\"\":{\"model\":\"deepprimal:block/" + name + "\"}}}");
        write(output, "assets/deepprimal/models/block/" + name + ".json",
            "{\"parent\":\"deepprimal:block/ground_material\",\"textures\":{\"top\":\"deepprimal:block/ground/"
                + top + "\",\"bottom\":\"deepprimal:block/ground/" + mineral + "\",\"side\":\"" + side
                + "\",\"particle_side\":\"deepprimal:block/ground/" + particle + "\"}}");
        String constant = "{\"type\":\"minecraft:constant\",\"value\":" + soil.color() + "}";
        String surface = cover.isEmpty() ? constant : cover.equals("_grass")
            ? "{\"type\":\"minecraft:grass\",\"temperature\":0.5,\"downfall\":1.0}"
            : "{\"type\":\"minecraft:constant\",\"value\":-10732494}";
        write(output, "assets/deepprimal/items/" + name + ".json",
            "{\"model\":{\"type\":\"minecraft:model\",\"model\":\"deepprimal:block/" + name
                + "\",\"tints\":[" + surface + "," + constant + "," + surface + "]}}");
        write(output, "data/deepprimal/loot_table/blocks/" + name + ".json",
            "{\"type\":\"minecraft:block\",\"pools\":[{\"rolls\":1,\"entries\":[{\"type\":\"minecraft:item\",\"name\":\""
                + id + "\"}],\"conditions\":[{\"condition\":\"minecraft:survives_explosion\"}]}]}");
        String label = soil.id().replace('_', ' ');
        label = label.substring(0, 1).toUpperCase(Locale.ROOT) + label.substring(1);
        if (!cover.isEmpty()) label += cover.equals("_grass") ? " Grass" : " Podzol";
        names.add("\"block.deepprimal." + name + "\":\"" + label + "\"");
      }
    write(output, "data/deepprimal/tags/block/soils.json", "{\"replace\":false,\"values\":[" + String.join(",", ids) + "]}");
    write(output, "data/deepprimal/tags/block/grass_soils.json", "{\"replace\":false,\"values\":[" + String.join(",", grassIds) + "]}");
    write(output, "data/deepprimal/tags/block/sand_soils.json", "{\"replace\":false,\"values\":[" + String.join(",", sandIds) + "]}");
    String language = Files.readString(Path.of(args[1]), StandardCharsets.UTF_8).stripTrailing();
    if (!language.endsWith("}")) throw new IOException("Expected a JSON language object");
    write(output, "assets/deepprimal/lang/en_us.json",
        language.substring(0, language.length() - 1) + ",\n" + String.join(",\n", names) + "\n}");
    System.out.println("SOIL_RESOURCES_GENERATED families=12 blocks=36 texturesReused=true");
  }

  private SoilResources() {}
}
