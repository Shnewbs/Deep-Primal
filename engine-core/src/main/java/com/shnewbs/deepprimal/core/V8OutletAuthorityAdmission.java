package com.shnewbs.deepprimal.core;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * Fail-closed admission of an independently observed V8 outlet forest to the
 * existing once-only, conserved and world-bound ledger. Geography is NEVER a
 * source of physical volume: every admitted balance must be supplied from an
 * already funded authoritative account. Planned catchment/runoff is rejected.
 *
 * <p>The caller must supply independently certified DOWNSTREAM receiver edges
 * and saved-world marine roots. In particular, neither the undirected spill
 * links nor a low height is a receiver/ocean certificate. A solver-selected
 * path unsupported by those independent witnesses cannot move water. This
 * adapter does not validate the native certificates themselves, touch native
 * fluid cells, perform a Minecraft world transaction, or activate V8 worldgen.
 * It is for a complete explicitly opted-in authority graph only; unknown
 * frontiers, unresolved basins and old saves fail closed without migration.
 */
public final class V8OutletAuthorityAdmission {
  private V8OutletAuthorityAdmission() {}

  /** Immutable proof-carrying result: a caller cannot construct its own receipt. */
  public static final class Admission {
    private final V8ConservedOutletLedger.Snapshot snapshot;
    private final Set<Long> marineRoots;
    private final int admittedPorts;
    private final int admittedLinks;

    private Admission(V8ConservedOutletLedger.Snapshot snapshot, Set<Long> marineRoots,
        int admittedPorts, int admittedLinks) {
      this.snapshot = Objects.requireNonNull(snapshot, "snapshot");
      this.marineRoots = Set.copyOf(Objects.requireNonNull(marineRoots, "marineRoots"));
      if (this.marineRoots.isEmpty() || admittedPorts < this.marineRoots.size()
          || admittedLinks < 0 || admittedLinks > WatershedOutletTopology.MAX_LINKS)
        throw new IllegalArgumentException("invalid V8 authority admission receipt");
      this.admittedPorts = admittedPorts;
      this.admittedLinks = admittedLinks;
    }
    public V8ConservedOutletLedger.Snapshot snapshot() { return snapshot; }
    public Set<Long> marineRoots() { return marineRoots; }
    public int admittedPorts() { return admittedPorts; }
    public int admittedLinks() { return admittedLinks; }
  }

  /**
   * Bridges bounded, observed geometry to independently funded balances.
   * Only certified RECEIVER edges are authorized in the resulting ledger;
   * topology remains a plan, not a physical transfer or a marine domain proof.
   *
   * @param ports observed watershed ports (all runoffQuanta must be zero)
   * @param links observed, possibly undirected, candidate spill connections
   * @param marineRoots independently verified keys of saved marine anchors
   * @param approvedReceivers independently certified directed downstream edges
   * @param fundedBalances real preexisting water/salt/sediment stores, once each
   */
  public static Admission admit(Collection<WatershedOutletTopology.Port> ports,
      Collection<WatershedOutletTopology.Link> links, double seaStage,
      Set<Long> marineRoots, Map<Long, Long> approvedReceivers,
      Map<Long, V8ConservedOutletLedger.Balance> fundedBalances) {
    Objects.requireNonNull(ports, "ports");
    Objects.requireNonNull(links, "links");
    Objects.requireNonNull(marineRoots, "marineRoots");
    Objects.requireNonNull(approvedReceivers, "approvedReceivers");
    Objects.requireNonNull(fundedBalances, "fundedBalances");
    List<WatershedOutletTopology.Port> observed = List.copyOf(ports);
    List<WatershedOutletTopology.Link> crossings = List.copyOf(links);
    Set<Long> roots = Set.copyOf(marineRoots);
    Map<Long, Long> approved = Map.copyOf(approvedReceivers);
    Map<Long, V8ConservedOutletLedger.Balance> funded = Map.copyOf(fundedBalances);
    if (observed.isEmpty() || observed.size() > WatershedOutletTopology.MAX_PORTS
        || roots.isEmpty() || crossings.size() > WatershedOutletTopology.MAX_LINKS)
      throw new IllegalArgumentException("incomplete or unbounded V8 outlet authority");

    Set<Long> keys = new HashSet<>();
    Set<Long> observedAnchors = new HashSet<>();
    for (WatershedOutletTopology.Port port : observed) {
      long key = WatershedOutletTopology.key(port.x(), port.z());
      if (!keys.add(key)) throw new IllegalArgumentException("duplicate authority port");
      if (port.unknownFrontier())
        throw new IllegalArgumentException("unobserved watershed frontier cannot own water");
      if (port.runoffQuanta() != 0)
        throw new IllegalArgumentException("planned catchment cannot mint physical water");
      if (port.verifiedOceanAnchor()) observedAnchors.add(key);
    }
    if (!roots.equals(observedAnchors))
      throw new IllegalArgumentException("saved marine roots differ from observed anchors");
    if (!funded.keySet().equals(keys))
      throw new IllegalArgumentException("missing or invented physical outlet account");

    // Recompute rather than trusting a caller-fabricated Solution record.
    var solved = WatershedOutletTopology.solve(observed, crossings, seaStage);
    TreeMap<Long, Long> routed = new TreeMap<>();
    for (var entry : solved.outlets().entrySet()) {
      long key = entry.getKey();
      var out = entry.getValue();
      if (out.status() == WatershedOutletTopology.Status.CLOSED
          || out.status() == WatershedOutletTopology.Status.UNRESOLVED)
        throw new IllegalArgumentException("no certified downstream basin outlet");
      Long to = out.receiverKey();
      if (to != null) {
        routed.put(key, to);
      } else if (!roots.contains(key)) {
        throw new IllegalArgumentException("unanchored regional water terminal");
      }
    }
    if (!approved.equals(routed))
      throw new IllegalArgumentException("spilled links are not verified downstream receivers");

    // Ensure every physical account has a FINITE marine path. In addition to
    // the solver's hop invariant, this independently rejects forked/cyclic
    // authorized directions before any durable store is created.
    Set<Long> settled = new HashSet<>(roots);
    for (Long start : keys) {
      if (settled.contains(start)) continue;
      Set<Long> pending = new HashSet<>();
      ArrayList<Long> trail = new ArrayList<>();
      Long at = start;
      while (at != null && !settled.contains(at)) {
        if (!keys.contains(at) || !pending.add(at))
          throw new IllegalArgumentException("uncertified or cyclic V8 outlet receiver");
        trail.add(at);
        at = routed.get(at);
      }
      if (at == null) throw new IllegalArgumentException("river receiver lacks marine root");
      settled.addAll(trail);
    }

    // Reject globally unrepresentable totals rather than accepting balances
    // which can no longer be audited by fixed-point conservation checks.
    long water = 0, salt = 0, sediment = 0;
    for (var balance : funded.values()) {
      water = Math.addExact(water, balance.water());
      salt = Math.addExact(salt, balance.salt());
      sediment = Math.addExact(sediment, balance.sediment());
    }
    // Preserve the immutable provided balances: no implicit headwater,
    // rainfall, runoff allocation, seawater, dilution or chemistry creation.
    return new Admission(V8ConservedOutletLedger.open(funded, routed), roots,
        observed.size(), crossings.size());
  }

  /**
   * Explicit fresh-world V2 ledger creation ONLY after native owner supplies
   * an already persisted world UUID and saved V8 profile fingerprint. This is
   * not a native cell transaction and must not be used to adopt old worlds.
   */
  public static V8OutletLedgerStore createBound(Path path, Admission admitted,
      V8OutletLedgerStore.WorldBinding binding) throws IOException {
    Objects.requireNonNull(admitted, "admitted");
    return V8OutletLedgerStore.createBound(path, admitted.snapshot(), binding);
  }
}
