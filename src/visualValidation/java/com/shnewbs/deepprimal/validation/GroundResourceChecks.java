package com.shnewbs.deepprimal.validation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shnewbs.deepprimal.client.GroundMaterialColors;
import com.shnewbs.deepprimal.engine.GroundSurfaceProfile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import javax.imageio.ImageIO;

/** Headless checks of the actual runtime resources, including original-art identity. */
public final class GroundResourceChecks {
  private static int checks;

  public static void main(String[] args) throws Exception {
    var manifest = JsonParser.parseString(Files.readString(Path.of(args[0]))).getAsJsonObject();
    for (var entry : manifest.getAsJsonArray("assets")) {
      var asset = entry.getAsJsonObject();
      String resource = asset.get("file").getAsString()
          .replace('\\', '/').replace("src/main/resources/", "");
      byte[] data = bytes(resource);
      require(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data))
          .equals(asset.get("sha256").getAsString()), "Owner asset changed: " + resource);
      var image = ImageIO.read(new java.io.ByteArrayInputStream(data));
      require(image != null && image.getWidth() == 64 && image.getHeight() == 64,
          "Ground image dimensions: " + resource);
      for (int z = 0; z < 64; z++)
        for (int x = 0; x < 64; x++) {
          int pixel = image.getRGB(x, z);
          require((pixel >>> 24) == 255
              && ((pixel >>> 16) & 255) == ((pixel >>> 8) & 255)
              && ((pixel >>> 8) & 255) == (pixel & 255), "Opaque grayscale: " + resource);
        }
      for (int i = 0; i < 64; i++) {
        require(image.getRGB(0, i) == image.getRGB(63, i), "Horizontal repeat: " + resource);
        if (asset.get("repeat_axes").getAsString().equals("xy"))
          require(image.getRGB(i, 0) == image.getRGB(i, 63), "Vertical repeat: " + resource);
      }
    }
    for (String material : List.of("grass", "podzol")) {
      var surface = ImageIO.read(new java.io.ByteArrayInputStream(bytes(
          "assets/deepprimal/textures/block/ground/masks/" + material + "_side_surface.png")));
      var soil = ImageIO.read(new java.io.ByteArrayInputStream(bytes(
          "assets/deepprimal/textures/block/ground/masks/" + material + "_side_soil.png")));
      for (int z = 0; z < 64; z++)
        for (int x = 0; x < 64; x++)
          require(surface.getRaster().getSample(x, z, 0) + soil.getRaster().getSample(x, z, 0) == 255,
              "Complementary mask weights: " + material);
    }

    var parent = json("assets/deepprimal/models/block/ground_material.json");
    var faces = parent.getAsJsonArray("elements").get(0).getAsJsonObject().getAsJsonObject("faces");
    var lang = json("assets/deepprimal/lang/en_us.json");
    var shovel = json("data/minecraft/tags/block/mineable/shovel.json").getAsJsonArray("values");
    var dirt = json("data/minecraft/tags/block/dirt.json").getAsJsonArray("values");
    for (String material : List.of("grass", "dirt", "podzol")) {
      String id = "ground_" + material;
      var state = json("assets/deepprimal/blockstates/" + id + ".json");
      require(state.getAsJsonObject("variants").getAsJsonObject("").get("model").getAsString()
          .equals("deepprimal:block/" + id), "Blockstate model " + id);
      var model = json("assets/deepprimal/models/block/" + id + ".json");
      require(model.get("parent").getAsString().equals("deepprimal:block/ground_material"),
          "Parent model " + id);
      var textures = model.getAsJsonObject("textures");
      for (String name : List.of("top", "side", "bottom")) {
        String texture = textures.get(name).getAsString();
        require(texture.startsWith("deepprimal:"), "Texture namespace " + texture);
        if (name.equals("side") && !material.equals("dirt")) {
          require(texture.equals("deepprimal:block/ground/" + material + "_side_material"),
              "Generated dual-tint side " + id);
        } else {
          bytes("assets/deepprimal/textures/" + texture.substring("deepprimal:".length()) + ".png");
        }
      }
      var item = json("assets/deepprimal/items/" + id + ".json").getAsJsonObject("model");
      require(item.get("type").getAsString().equals("minecraft:model")
          && item.get("model").getAsString().equals("deepprimal:block/" + id), "Item model " + id);
      var tints = item.getAsJsonArray("tints");
      require(tints.size() == 3, "Three tint layers " + id);
      require(tints.get(1).getAsJsonObject().get("value").getAsInt() == GroundMaterialColors.SOIL,
          "Inventory soil matches block default " + id);
      for (var face : faces.entrySet()) {
        var value = face.getValue().getAsJsonObject();
        int expected = face.getKey().equals("up") ? 0 : face.getKey().equals("down") ? 1 : 2;
        require(value.get("tintindex").getAsInt() == expected, "Face tint layer " + id);
        require(value.get("cullface").getAsString().equals(face.getKey()), "Face culling " + id);
      }
      var topTint = tints.get(0).getAsJsonObject();
      if (material.equals("grass")) {
        require(topTint.get("type").getAsString().equals("minecraft:grass")
            && topTint.get("temperature").getAsDouble() == .5
            && topTint.get("downfall").getAsDouble() == 1, "Grass inventory climate");
      } else {
        require(topTint.get("type").getAsString().equals("minecraft:constant")
            && topTint.get("value").getAsInt()
                == (material.equals("dirt") ? GroundMaterialColors.SOIL : -10732494),
            "Inventory top matches default " + id);
      }
      require(lang.has("block.deepprimal." + id), "Localized block name " + id);
      require(shovel.asList().stream().anyMatch(v -> v.getAsString().equals("deepprimal:" + id))
          && dirt.asList().stream().anyMatch(v -> v.getAsString().equals("deepprimal:" + id)),
          "Substrate and shovel tags " + id);
      var loot = json("data/deepprimal/loot_table/blocks/" + id + ".json");
      require(loot.getAsJsonArray("pools").get(0).getAsJsonObject().getAsJsonArray("entries")
          .get(0).getAsJsonObject().get("name").getAsString().equals("deepprimal:" + id),
          "Self-drop resource " + id);
    }
    System.out.println("GROUND_RESOURCES_PASS checks=" + checks
        + " groundImages=" + manifest.getAsJsonArray("assets").size()
        + " manifestMatched=true masksComplementary=true modelLinks=true tintLayers=true");
    GroundSurfaceProfileChecks.run();
    GroundSideMathChecks.run();
    var example = GroundSurfaceProfile.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE,
        JsonParser.parseString(Files.readString(Path.of(args[1])))).getOrThrow();
    require(example.version() == 1 && example.salt() == 34001 && example.soilDepth() == 4,
        "Documented surface example");
  }

  private static byte[] bytes(String name) throws IOException {
    try (var stream = GroundResourceChecks.class.getClassLoader().getResourceAsStream(name)) {
      if (stream == null) throw new AssertionError("Missing runtime resource " + name);
      return stream.readAllBytes();
    }
  }

  private static JsonObject json(String name) throws IOException {
    return JsonParser.parseString(new String(bytes(name), StandardCharsets.UTF_8)).getAsJsonObject();
  }

  private static void require(boolean condition, String message) {
    checks++;
    if (!condition) throw new AssertionError(message);
  }

  private GroundResourceChecks() {}
}

