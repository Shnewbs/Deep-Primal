package com.shnewbs.deepprimal.engine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.shnewbs.deepprimal.core.*;
import java.util.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.*;

/** Versioned dimension-owned stores. Chunk loading never resets an existing aquifer or its age. */
public final class HydrologyData extends SavedData {
  private static Codec<Long> bounded(long max) {
    return Codec.LONG.validate(n -> n >= 0 && n <= max ? com.mojang.serialization.DataResult.success(n)
        : com.mojang.serialization.DataResult.error(() -> "Water account outside valid range"));
  }
  static final Codec<WaterAccount.Parcel> PARCEL = RecordCodecBuilder.create(i -> i.group(
      bounded(WaterAccount.MAX).fieldOf("water").forGetter(WaterAccount.Parcel::water),
      bounded(WaterAccount.MAX * 1024).fieldOf("salt").forGetter(WaterAccount.Parcel::salt),
      bounded(WaterAccount.MAX * 1024).fieldOf("sediment").forGetter(WaterAccount.Parcel::sediment)
  ).apply(i, WaterAccount.Parcel::new));
  private record Region(long key, WaterAccount.Parcel ground, WaterAccount.Parcel snow,
      int wet, boolean active, long tick, int dynamicWet) {
    static final Codec<Region> CODEC = RecordCodecBuilder.create(i -> i.group(
      Codec.LONG.fieldOf("key").forGetter(Region::key), PARCEL.fieldOf("ground").forGetter(Region::ground),
      PARCEL.fieldOf("snow").forGetter(Region::snow), Codec.intRange(0, Aquifer.WET_STEPS).fieldOf("wet").forGetter(Region::wet),
      Codec.BOOL.fieldOf("active").forGetter(Region::active), Codec.LONG.fieldOf("tick").forGetter(Region::tick),
      Codec.intRange(0, Aquifer.WET_STEPS).optionalFieldOf("dynamicWet", 0).forGetter(Region::dynamicWet)
    ).apply(i, Region::new));
  }
  private record Cell(long key, WaterAccount.Parcel contents) {
    static final Codec<Cell> CODEC = RecordCodecBuilder.create(i -> i.group(
      Codec.LONG.fieldOf("key").forGetter(Cell::key), PARCEL.fieldOf("contents").forGetter(Cell::contents)
    ).apply(i, Cell::new));
  }
  public static final Codec<HydrologyData> CODEC = RecordCodecBuilder.create(i -> i.group(
      Codec.intRange(1, 1).fieldOf("schema").forGetter(d -> 1),
      Region.CODEC.listOf().fieldOf("regions").forGetter(HydrologyData::regions),
      Cell.CODEC.listOf().fieldOf("composition").forGetter(HydrologyData::cells),
      PARCEL.fieldOf("ocean").forGetter(d -> d.ocean.snapshot()),
      PARCEL.fieldOf("atmosphere").forGetter(d -> d.atmosphere.snapshot()),
      Codec.LONG.fieldOf("initialized").forGetter(d -> d.initialized)
  ).apply(i, HydrologyData::new));
  public static final SavedDataType<HydrologyData> TYPE = new SavedDataType<>(
      Identifier.fromNamespaceAndPath("deepprimal", "hydrology_v1"), HydrologyData::new, CODEC);
  public final Map<Long, Aquifer> aquifers = new HashMap<>();
  final Map<Long, WaterAccount.Parcel> composition = new HashMap<>();
  public final WaterAccount ocean, atmosphere;
  public long initialized;
  public HydrologyData() {
    this(1, List.of(), List.of(),
        new WaterAccount.Parcel(WaterAccount.MAX / 2, WaterAccount.MAX / 2 * WaterAccount.OCEAN_SALT, 0),
        new WaterAccount.Parcel(WaterAccount.MAX / 4, 0, 0), 0);
  }
  private HydrologyData(int schema, List<Region> regions, List<Cell> cells,
      WaterAccount.Parcel ocean, WaterAccount.Parcel atmosphere, long initialized) {
    for (var r : regions) {
      if (aquifers.put(r.key, new Aquifer(r.ground, r.snow, r.wet, r.active, r.tick, r.dynamicWet)) != null)
        throw new IllegalArgumentException("Duplicate aquifer");
    }
    for (var c : cells) if (composition.put(c.key, c.contents) != null)
      throw new IllegalArgumentException("Duplicate water composition");
    this.ocean = new WaterAccount(WaterAccount.MAX, ocean);
    this.atmosphere = new WaterAccount(WaterAccount.MAX, atmosphere);
    this.initialized = initialized;
  }
  private List<Region> regions() {
    return aquifers.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(e ->
      new Region(e.getKey(), e.getValue().ground.snapshot(), e.getValue().snow.snapshot(),
          e.getValue().wetSteps(), e.getValue().active(), e.getValue().lastTick(), e.getValue().dynamicWetSteps())).toList();
  }
  private List<Cell> cells() {
    return composition.entrySet().stream().sorted(Map.Entry.comparingByKey())
        .map(e -> new Cell(e.getKey(), e.getValue())).toList();
  }
  public static HydrologyData get(ServerLevel level) { return level.getDataStorage().computeIfAbsent(TYPE); }
  public static long key(int x, int z) { return ((long) Math.floorDiv(x, 64) << 32) | (Math.floorDiv(z, 64) & 0xffffffffL); }
  public Aquifer aquifer(int x, int z, double rain, long tick) {
    long key = key(x, z);
    var old = aquifers.get(key);
    if (old != null) return old;
    // Explicit finite seeded groundwater is initialized once, never on every chunk load.
    long initial = (long) (Aquifer.CAPACITY * (.3 + Math.clamp(rain, 0, 1) * .40));
    old = new Aquifer(new WaterAccount.Parcel(initial, 0, 0), WaterAccount.Parcel.EMPTY,
        Aquifer.WET_STEPS, true, tick);
    aquifers.put(key, old); initialized += initial; setDirty(); return old;
  }
}
