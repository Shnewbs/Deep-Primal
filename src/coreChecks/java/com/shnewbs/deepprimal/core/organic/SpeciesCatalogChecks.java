package com.shnewbs.deepprimal.core.organic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public final class SpeciesCatalogChecks {
  private static long checks;

  private static void check(boolean condition, String reason) {
    checks++;
    if (!condition) throw new AssertionError(reason + " at " + checks);
  }

  private static void rejects(Runnable action) {
    checks++;
    try {
      action.run();
    } catch (IllegalArgumentException | NullPointerException expected) {
      return;
    }
    throw new AssertionError("invalid catalog input accepted at " + checks);
  }

  private static SpeciesPhysiology species(String id) {
    return new SpeciesPhysiology(1, id, SpeciesPhysiology.Kind.PERENNIAL,
        -20, 10, 40, .1, .2, .9, .25, 300, 120);
  }

  public static void main(String[] args) {
    var oak = species("deepprimal:oak");
    var fern = species("deepprimal:fern");
    var definitions = new ArrayList<>(java.util.List.of(oak, fern));
    var catalog = new SpeciesCatalog(1, definitions);
    definitions.clear();

    check(catalog.schemaVersion() == 1, "schema retained");
    check(catalog.species().equals(java.util.List.of(fern, oak)), "canonical ID order");
    check(catalog.find("deepprimal:oak").orElseThrow() == oak, "ID lookup");
    check(catalog.find("deepprimal:missing").isEmpty(), "missing ID");
    check(catalog.species().size() == 2, "input collection is defensively copied");
    rejects(() -> catalog.find(null));
    try {
      catalog.species().clear();
      throw new AssertionError("catalog species list is mutable");
    } catch (UnsupportedOperationException expected) {
      checks++;
    }
    rejects(() -> new SpeciesCatalog(2, java.util.List.of(oak)));
    rejects(() -> new SpeciesCatalog(1, null));
    rejects(() -> new SpeciesCatalog(1, java.util.Arrays.asList(oak, null)));
    rejects(() -> new SpeciesCatalog(1, java.util.List.of(oak, oak)));

    var shuffled = new ArrayList<SpeciesPhysiology>();
    for (int i = 0; i < SpeciesCatalog.MAX_SPECIES; i++)
      shuffled.add(species(String.format(java.util.Locale.ROOT, "deepprimal:species_%04d", i)));
    Random random = new Random(0x5EEDCA7L);
    Collections.shuffle(shuffled, random);
    var full = new SpeciesCatalog(1, shuffled);
    var reordered = new ArrayList<>(shuffled);
    Collections.shuffle(reordered, new Random(0xCA7A10L));
    check(new SpeciesCatalog(1, reordered).species().equals(full.species()),
        "input order does not change catalog snapshot");
    for (int i = 0; i < SpeciesCatalog.MAX_SPECIES; i++)
      check(full.species().get(i).id().endsWith(String.format(java.util.Locale.ROOT, "%04d", i)),
          "bounded catalog has stable canonical order");
    shuffled.add(species("deepprimal:species_overflow"));
    rejects(() -> new SpeciesCatalog(1, shuffled));
    System.out.println("SPECIES_CATALOG_PASS checks=" + checks
        + " maxSpecies=" + SpeciesCatalog.MAX_SPECIES + " canonical=true immutable=true");
  }

  private SpeciesCatalogChecks() {}
}
