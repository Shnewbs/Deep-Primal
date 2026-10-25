package com.shnewbs.deepprimal.engine;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.shnewbs.deepprimal.core.density.DensityChunk;
import com.shnewbs.deepprimal.core.density.DensityGraph;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.chunk.ChunkAccess;

/** Complete bounded preview identity; not a registered generator or gameplay save migration. */
public record TerrainPreviewSnapshot(
    int version, long seed, int minY, int height,
    DensityGraph.Definition density, GroundSurfaceProfile surface) {
  public static final int MAX_BYTES = 131072;
  private static final int MAX_JSON_DEPTH = 16, MAX_JSON_VALUES = 8192;
  private static final Set<String> KEYS =
      Set.of("version", "seed", "min_y", "height", "density", "surface");

  public static final Codec<TerrainPreviewSnapshot> CODEC = Codec.PASSTHROUGH.comapFlatMap(
      dynamic -> {
        try {
          return DataResult.success(decode(dynamic.convert(JsonOps.INSTANCE).getValue()));
        } catch (IllegalArgumentException | IllegalStateException failure) {
          return DataResult.error(() -> "terrain preview snapshot: " + failure.getMessage());
        }
      },
      snapshot -> new Dynamic<>(JsonOps.INSTANCE, encode(snapshot)));

  public TerrainPreviewSnapshot {
    Objects.requireNonNull(density, "density");
    Objects.requireNonNull(surface, "surface");
    if (version != 1) throw new IllegalArgumentException("version must be 1");
    if (minY % 16 != 0 || height % 16 != 0 || height < 16 || height > DensityChunk.MAX_HEIGHT
        || minY < -DensityGraph.WORLD_LIMIT
        || (long) minY + height - 1 > DensityGraph.WORLD_LIMIT)
      throw new IllegalArgumentException("invalid aligned preview height bounds");
    density.compile(seed).channel(surface.channel(), 0, 0, 0);
  }

  public Compiled compile() {
    return new Compiled(this, surface.compile(seed, density.compile(seed)));
  }

  /** Reads at most MAX_BYTES+1 bytes; leaves ownership of the input stream with the caller. */
  public static TerrainPreviewSnapshot read(InputStream input) throws IOException {
    Objects.requireNonNull(input, "input");
    byte[] bytes = input.readNBytes(MAX_BYTES + 1);
    if (bytes.length > MAX_BYTES)
      throw new IOException("terrain preview snapshot exceeds " + MAX_BYTES + " UTF-8 bytes");
    String text;
    try {
      text = StandardCharsets.UTF_8.newDecoder()
          .onMalformedInput(CodingErrorAction.REPORT)
          .onUnmappableCharacter(CodingErrorAction.REPORT)
          .decode(ByteBuffer.wrap(bytes)).toString();
    } catch (CharacterCodingException failure) {
      throw new IOException("terrain preview snapshot is not valid UTF-8", failure);
    }
    try (var reader = new JsonReader(new StringReader(text))) {
      reader.setStrictness(Strictness.STRICT);
      JsonElement json = readValue(reader, 0, new int[1]);
      if (reader.peek() != JsonToken.END_DOCUMENT)
        throw new IOException("terrain preview snapshot has trailing data");
      var decoded = CODEC.parse(JsonOps.INSTANCE, json);
      if (decoded.error().isPresent())
        throw new IOException(decoded.error().orElseThrow().message());
      return decoded.getOrThrow();
    } catch (IllegalArgumentException | IllegalStateException failure) {
      throw new IOException("invalid terrain preview snapshot: " + failure.getMessage(), failure);
    }
  }

  public byte[] utf8() {
    byte[] bytes = encode(this).toString().getBytes(StandardCharsets.UTF_8);
    if (bytes.length > MAX_BYTES)
      throw new IllegalStateException("encoded terrain preview snapshot exceeds byte budget");
    return bytes;
  }

  public static final class Compiled {
    private final TerrainPreviewSnapshot snapshot;
    private final GroundSurfaceProfile.Compiled surface;

    private Compiled(TerrainPreviewSnapshot snapshot, GroundSurfaceProfile.Compiled surface) {
      this.snapshot = snapshot;
      this.surface = surface;
    }

    public DensityChunk fill(ChunkAccess chunk) {
      bounds(chunk);
      return DensityChunkMaterializer.fill(surface, chunk);
    }

    public NoiseColumn baseColumn(int x, int z, LevelHeightAccessor level) {
      bounds(level);
      return DensityChunkMaterializer.baseColumn(surface, x, z, level);
    }

    private void bounds(LevelHeightAccessor level) {
      Objects.requireNonNull(level, "level");
      if (level.getMinY() != snapshot.minY || level.getHeight() != snapshot.height)
        throw new IllegalArgumentException("native height bounds do not match saved terrain snapshot");
    }
  }

  private static JsonElement readValue(JsonReader reader, int depth, int[] values) throws IOException {
    if (depth > MAX_JSON_DEPTH || ++values[0] > MAX_JSON_VALUES)
      throw new IOException("terrain preview snapshot exceeds JSON depth/value budget");
    return switch (reader.peek()) {
      case BEGIN_OBJECT -> {
        var object = new JsonObject();
        reader.beginObject();
        while (reader.hasNext()) {
          String name = reader.nextName();
          if (object.has(name))
            throw new IOException("duplicate snapshot key " + name + " at " + reader.getPath());
          object.add(name, readValue(reader, depth + 1, values));
        }
        reader.endObject();
        yield object;
      }
      case BEGIN_ARRAY -> {
        var array = new JsonArray();
        reader.beginArray();
        while (reader.hasNext()) array.add(readValue(reader, depth + 1, values));
        reader.endArray();
        yield array;
      }
      case STRING -> new JsonPrimitive(reader.nextString());
      case NUMBER -> new JsonPrimitive(new BigDecimal(reader.nextString()));
      case BOOLEAN -> new JsonPrimitive(reader.nextBoolean());
      case NULL -> {
        reader.nextNull();
        yield JsonNull.INSTANCE;
      }
      default -> throw new IOException("expected snapshot value at " + reader.getPath());
    };
  }

  private static TerrainPreviewSnapshot decode(JsonElement value) {
    if (!value.isJsonObject()) throw new IllegalArgumentException("$ must be an object");
    var root = value.getAsJsonObject();
    if (!root.keySet().equals(KEYS))
      throw new IllegalArgumentException("$ must contain exactly " + KEYS);
    JsonElement seed = root.get("seed");
    if (!seed.isJsonPrimitive() || !seed.getAsJsonPrimitive().isString())
      throw new IllegalArgumentException("$.seed must be a signed-long decimal string");
    var density = DensityGraphCodec.CODEC.parse(JsonOps.INSTANCE, root.get("density"));
    if (density.error().isPresent())
      throw new IllegalArgumentException("$.density: " + density.error().orElseThrow().message());
    var surface = GroundSurfaceProfile.CODEC.parse(JsonOps.INSTANCE, root.get("surface"));
    if (surface.error().isPresent())
      throw new IllegalArgumentException("$.surface: " + surface.error().orElseThrow().message());
    return new TerrainPreviewSnapshot(integer(root.get("version"), "$.version"),
        Long.parseLong(seed.getAsString()), integer(root.get("min_y"), "$.min_y"),
        integer(root.get("height"), "$.height"), density.getOrThrow(), surface.getOrThrow());
  }

  private static int integer(JsonElement value, String path) {
    if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber())
      throw new IllegalArgumentException(path + " must be an exact integer");
    try {
      return value.getAsBigDecimal().intValueExact();
    } catch (ArithmeticException failure) {
      throw new IllegalArgumentException(path + " must be an exact 32-bit integer", failure);
    }
  }

  private static JsonObject encode(TerrainPreviewSnapshot snapshot) {
    var root = new JsonObject();
    root.addProperty("version", snapshot.version);
    root.addProperty("seed", Long.toString(snapshot.seed));
    root.addProperty("min_y", snapshot.minY);
    root.addProperty("height", snapshot.height);
    root.add("density", DensityGraphCodec.CODEC.encodeStart(JsonOps.INSTANCE, snapshot.density).getOrThrow());
    root.add("surface", GroundSurfaceProfile.CODEC.encodeStart(JsonOps.INSTANCE, snapshot.surface).getOrThrow());
    return root;
  }
}
