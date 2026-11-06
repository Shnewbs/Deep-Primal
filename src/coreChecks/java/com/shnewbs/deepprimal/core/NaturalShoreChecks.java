package com.shnewbs.deepprimal.core;

import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.*;
import java.util.function.DoubleBinaryOperator;

/** Differential geometry/identity checks for saved channel revision 9. */
public final class NaturalShoreChecks {
  private static long checks;
  private static void require(boolean value, String message) {
    checks++;
    if (!value) throw new AssertionError(message);
  }

  public static void main(String[] args) throws Exception {
    DoubleBinaryOperator height = (x, z) ->
        150 + x * .014 - 38 * Math.exp(-((x - 300) * (x - 300) + z * z) / (260. * 260));
    var noise = new TerrainNoise(20261105, true);
    var old = new WatershedRegion(-4096, -4096, height, (x, z) -> .7,
        true, true, noise, true, true, true, null, null, true, true);
    var next = new WatershedRegion(-4096, -4096, height, (x, z) -> .7,
        true, true, noise, true, true, true, null, null, true, true, true);
    old.verify(); next.verify();
    require(old.lakeProbes().equals(next.lakeProbes()), "Lake plans and outlets changed");
    require(old.centerline().equals(next.centerline()), "River routing changed");
    int changed = 0, shallower = 0, deeper = 0, wet = 0, retreated = 0;
    StringBuilder capture = args.length >= 3 ? new StringBuilder("x,z,before,after,before_water,after_water\n") : null;
    for (int z = -480; z <= 480; z += 4)
      for (int x = -180; x <= 780; x += 4) {
        double raw = height.applyAsDouble(x, z);
        var a = old.sample(x, z, raw); var b = next.sample(x, z, raw);
        require(!Double.isFinite(b.water()) || Double.isFinite(a.water()), "Water escaped original basin");
        if (Double.isFinite(a.water()) && !Double.isFinite(b.water())) {
          retreated++;
          require(b.ground() >= a.water() + .35, "Retreated bank is not contained dry land");
        } else require(Double.doubleToLongBits(a.water()) == Double.doubleToLongBits(b.water()),
            "Lake stage changed");
        require(a.flowX() == b.flowX() && a.flowZ() == b.flowZ() && a.speed() == b.speed(),
            "Routed flow changed");
        require(Double.isFinite(b.ground()), "Non-finite bed");
        if (Double.isFinite(b.water())) {
          wet++;
          require(b.ground() <= Math.ceil(b.water()) - 1.001 + 1e-8, "Lost wet voxel");
          if (b.ground() > a.ground() + .05) shallower++;
          if (b.ground() < a.ground() - .05) deeper++;
        }
        if (Math.abs(a.ground() - b.ground()) > .05) changed++;
        require(b.equals(next.sample(x, z, raw)), "Replay or sampling-order instability");
        if (capture != null)
          capture.append(x).append(',').append(z).append(',').append(a.ground()).append(',')
              .append(b.ground()).append(',').append(a.water()).append(',').append(b.water()).append('\n');
      }
    require(wet > 1000 && changed > 1000 && shallower > 100 && deeper > 20,
        "Insufficient shelf/pool relief: " + wet + "/" + changed + "/" + shallower + "/" + deeper);
    require(retreated > 100, "Outer shoreline remains a uniform contour");
    int protectedSamples = 0;
    for (var reach : old.centerline()) {
      var a = old.sample(reach.x(),reach.z(),height.applyAsDouble(reach.x(),reach.z()));
      if (!Double.isFinite(a.water())) continue;
      var b = next.sample(reach.x(),reach.z(),height.applyAsDouble(reach.x(),reach.z()));
      require(Double.doubleToLongBits(a.water()) == Double.doubleToLongBits(b.water()),
          "Margin blocked a routed reach or outlet");
      protectedSamples++;
    }
    require(protectedSamples > 0, "No protected outlet/reach witness");
    var beds = EnumSet.noneOf(NaturalShore.Bed.class);
    int coherent = 0, neighbours = 0;
    for (int z = -256; z <= 256; z += 2)
      for (int x = -256; x <= 256; x += 2) {
        var shallow = new TerrainEngine.Sample(120, 123, .6, .7, .1, 1, 0, 0, 0, 0);
        var deep = new TerrainEngine.Sample(110, 123, .85, .7, .1, 1, 0, 0, 0, 0);
        var gravel = new TerrainEngine.Sample(120, 123, .6, .7, .1, 1, 0, 0, .6, 0);
        var kind = NaturalShore.bed(noise, x, z, shallow);
        beds.add(kind); beds.add(NaturalShore.bed(noise, x, z, deep));
        beds.add(NaturalShore.bed(noise, x, z, gravel));
        neighbours++;
        if (kind == NaturalShore.bed(noise, x + 1, z, shallow)) coherent++;
        require(NaturalShore.bankGround(noise,x,z,123,123.01,123.01) >= 123.35,
            "Dry margin broke containment");
        require(NaturalShore.lakeBed(noise,x,z,123,123,123) == 123,
            "Shelf changed shore boundary");
      }
    require(beds.size() == 4, "Missing bed facies: " + beds);
    require(coherent > neighbours * .9, "Bed materials became per-cell dithering");
    require(NaturalShore.treeChance(1,.7,.95) > NaturalShore.treeChance(1,.7,.05) * 3,
        "Riparian canopy lacks clusters and gaps");
    require(NaturalShore.treeChance(1,.7,.5) > Vegetation.treeChance(1,.7),
        "Typical eligible bank lacks canopy admission improvement");
    require(NaturalShore.treeChance(2,.7,.95) == 0, "Desert became forest");
    require(NaturalShore.treeChance(0,.7,1) <= .2, "Prairie lost open canopy limit");
    require(NaturalShore.coverChance(0) == .3 && NaturalShore.coverChance(1) == .72,
        "Ground cover bounds");
    int engineWet = 0, engineChanged = 0;
    for (long seed : new long[] {0L, 20261020L}) {
      var before = new TerrainEngine(seed,7,TerrainOptions.DEFAULT,8);
      var after = new TerrainEngine(seed,7,TerrainOptions.DEFAULT,9);
      var lakes = before.waterways(0,0).lakeProbes();
      require(lakes.equals(after.waterways(0,0).lakeProbes()), "Engine revision 9 changed lake planning");
      for (var lake : lakes.stream().limit(4).toList())
        for (int dz = -64; dz <= 64; dz += 16)
          for (int dx = -64; dx <= 64; dx += 16) {
            var a = before.column(lake.x()+dx,lake.z()+dz);
            var b = after.column(lake.x()+dx,lake.z()+dz);
            require(!b.wet() || a.wet(), "Engine water escaped original basin");
            if (a.wet() && b.wet()) require(a.water() == b.water(), "Engine lake stage changed");
            if (a.wet()) {
              engineWet++;
              if (Math.abs(a.ground()-b.ground()) > .01) engineChanged++;
            }
          }
    }
    require(engineWet > 0 && engineChanged > 0, "Revision 9 not connected to engine geography");
    System.out.println("NATURAL_SHORE_ENGINE_PASS seeds=2 wet=" + engineWet + " changed=" + engineChanged);
    if (args.length >= 2) legacyReplay(Path.of(args[0]), Path.of(args[1]));
    if (capture != null) Files.writeString(Path.of(args[2]), capture);
    System.out.println("NATURAL_SHORE_PASS checks=" + checks + " wet=" + wet
        + " changed=" + changed + " shallower=" + shallower + " deeper=" + deeper
        + " retreated=" + retreated + " protectedReachSamples=" + protectedSamples
        + " facies=" + beds + " coherentNeighbours=" + coherent + "/" + neighbours);
  }

  /** A separate loader executes the exact pre-change engine sources and unchanged core dependencies. */
  private static void legacyReplay(Path baseline, Path coreJar) throws Exception {
    try (var loader = new URLClassLoader(new java.net.URL[] {
        baseline.toUri().toURL(), coreJar.toUri().toURL()}, ClassLoader.getPlatformClassLoader())) {
      var type = loader.loadClass("com.shnewbs.deepprimal.core.TerrainEngine");
      var optionsType = loader.loadClass("com.shnewbs.deepprimal.core.TerrainOptions");
      var options = optionsType.getField("DEFAULT").get(null);
      var constructor = type.getConstructor(long.class,int.class,optionsType,int.class);
      var column = type.getMethod("column", double.class, double.class);
      for (int revision = 0; revision <= 8; revision++)
        for (long seed : new long[] {0L, 20261020L}) {
          var old = constructor.newInstance(seed,7,options,revision);
          var next = new TerrainEngine(seed,7,TerrainOptions.DEFAULT,revision);
          for (int n = 0; n < 32; n++) {
            int x = 160 + n * 37, z = -1200 + n * 59;
            Object a = column.invoke(old,x,z); var b = next.column(x,z);
            String[] fields = {"ground","water","rain","temperature","slope","sediment","flowX","flowZ","speed","rock"};
            double[] expected = {b.ground(),b.water(),b.rain(),b.temperature(),b.slope(),b.sediment(),b.flowX(),b.flowZ(),b.speed(),b.rock()};
            for (int i = 0; i < fields.length; i++) {
              double actual = ((Number)a.getClass().getMethod(fields[i]).invoke(a)).doubleValue();
              require(Double.doubleToLongBits(actual) == Double.doubleToLongBits(expected[i]),
                  "Saved revision " + revision + " changed " + fields[i]);
            }
          }
        }
    }
    System.out.println("NATURAL_SHORE_LEGACY_PASS revisions=0..8 columns=576 fields=5760");
  }
}
