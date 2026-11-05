package com.shnewbs.deepprimal.core.organic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Bounded, immutable species definitions in canonical ID order.
 * Serialization and live registry ownership belong to the host.
 */
public final class SpeciesCatalog {
  public static final int SCHEMA_VERSION = 1;
  public static final int MAX_SPECIES = 4096;

  private final int schemaVersion;
  private final List<SpeciesPhysiology> species;
  private final Map<String, SpeciesPhysiology> byId;

  public SpeciesCatalog(int schemaVersion, Collection<SpeciesPhysiology> definitions) {
    if (schemaVersion != SCHEMA_VERSION)
      throw new IllegalArgumentException("unsupported species catalog schema");
    Objects.requireNonNull(definitions, "definitions");
    if (definitions.size() > MAX_SPECIES)
      throw new IllegalArgumentException("species catalog exceeds its bounded capacity");

    ArrayList<SpeciesPhysiology> sorted = new ArrayList<>(definitions.size());
    for (SpeciesPhysiology definition : definitions)
      sorted.add(Objects.requireNonNull(definition, "species definition"));
    sorted.sort(Comparator.comparing(SpeciesPhysiology::id));

    HashMap<String, SpeciesPhysiology> index = new HashMap<>(sorted.size());
    String previous = null;
    for (SpeciesPhysiology definition : sorted) {
      if (definition.id().equals(previous))
        throw new IllegalArgumentException("duplicate species ID: " + definition.id());
      previous = definition.id();
      index.put(definition.id(), definition);
    }

    this.schemaVersion = schemaVersion;
    this.species = List.copyOf(sorted);
    this.byId = Map.copyOf(index);
  }

  public int schemaVersion() { return schemaVersion; }

  /** Canonical ascending ID order for deterministic encoding and replay. */
  public List<SpeciesPhysiology> species() { return species; }

  public Optional<SpeciesPhysiology> find(String id) {
    return Optional.ofNullable(byId.get(Objects.requireNonNull(id, "id")));
  }
}
