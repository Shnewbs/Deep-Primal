package com.shnewbs.deepprimal.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.TreeMap;

/**
 * Deterministic, bounded inter-region outlet topology for explicitly supplied
 * watershed ports. A port may describe a lake sill, river connection or marine
 * gateway. Links are undirected physical passages with a minimum spill crest.
 *
 * <p>This is a <em>planning</em> graph, not a live-water solver. Inputs must
 * already be admitted by a saved routing policy; an ocean anchor must come from
 * proven marine geography. No generated volume, liquid height or constituent
 * account is changed by this class. Unobserved regional frontiers remain
 * unresolved rather than being silently treated as sea outlets. In particular,
 * port graphs must not be built by treating every region edge as an anchor.
 *
 * <p>The minimax priority flood finds the lowest possible maximum sill from
 * every port to a supplied sea anchor. A deterministic, acyclic receiver tree
 * accumulates integer planned runoff without duplicating any branch supply.
 * Connectivity and stage are only authoritative over a <em>complete</em> input
 * graph; this helper does not discover missing regions or activate V8 worldgen.
 */
public final class WatershedOutletTopology {
  public static final int STEP = 32;
  public static final int MAX_PORTS = 16_384;
  public static final int MAX_LINKS = 65_536;

  private WatershedOutletTopology() {}

  /** Runoff is measured in caller-chosen integer quanta and is not a source emitter. */
  public record Port(int x, int z, double floor, long runoffQuanta,
      boolean verifiedOceanAnchor, boolean unknownFrontier) {}

  /** A crossing or local connection; {@code sill} is the lowest pass elevation. */
  public record Link(int ax, int az, int bx, int bz, double sill) {}

  public enum Status { OCEAN, INLAND_SPILL, CLOSED, UNRESOLVED }

  public record Outlet(Status status, double minimumSpillLevel, Long receiverKey,
      long accumulatedRunoffQuanta) {
    public Outlet {
      Objects.requireNonNull(status, "status");
      if (status == Status.CLOSED || status == Status.UNRESOLVED) {
        // UNRESOLVED may still have a known but nonauthoritative spill path.
        if (status == Status.CLOSED && !Double.isNaN(minimumSpillLevel))
          throw new IllegalArgumentException("closed outlet cannot assert a spill");
      } else if (!Double.isFinite(minimumSpillLevel))
        throw new IllegalArgumentException("resolved outlet needs finite spill");
      if (accumulatedRunoffQuanta < 0) throw new IllegalArgumentException("negative runoff");
    }
  }

  public record Solution(Map<Long, Outlet> outlets, long totalRunoffQuanta,
      long terminalRunoffQuanta, int portCount, int linkCount) {
    public Solution {
      outlets = Collections.unmodifiableMap(new TreeMap<>(outlets));
      if (totalRunoffQuanta != terminalRunoffQuanta)
        throw new IllegalStateException("runoff lost or duplicated in outlet tree");
    }
    public Outlet at(int x, int z) {
      Outlet found = outlets.get(key(x, z));
      if (found == null) throw new IllegalArgumentException("port not in topology");
      return found;
    }
  }

  private static final class Node {
    final Port port;
    final long key;
    final List<Arc> arcs = new ArrayList<>();
    double spill = Double.POSITIVE_INFINITY;
    int hops = Integer.MAX_VALUE;
    long anchor = Long.MAX_VALUE;
    long parentKey = Long.MAX_VALUE;
    Node parent;
    int component = -1;
    long discharge;

    Node(Port port) {
      this.port = port;
      key = key(port.x(), port.z());
      discharge = port.runoffQuanta();
    }
  }

  private record Arc(Node other, double sill) {}
  private record LinkKey(long first, long second) {}
  private record Choice(Node node, double spill, int hops, long anchor, long parentKey) {}

  private static final Comparator<Choice> ORDER = Comparator
      .comparingDouble(Choice::spill).thenComparingInt(Choice::hops)
      .thenComparingLong(Choice::anchor).thenComparingLong(Choice::parentKey)
      .thenComparingLong(c -> c.node().key);

  /** Pack signed coordinates exactly; unlike double hashes this preserves seams. */
  public static long key(int x, int z) {
    if (Math.floorMod(x, STEP) != 0 || Math.floorMod(z, STEP) != 0)
      throw new IllegalArgumentException("watershed port is not 32-block aligned");
    return ((long) x << 32) | (z & 0xffffffffL);
  }

  public static Solution solve(Collection<Port> ports, Collection<Link> links, double seaStage) {
    Objects.requireNonNull(ports, "ports");
    Objects.requireNonNull(links, "links");
    if (!Double.isFinite(seaStage)) throw new IllegalArgumentException("nonfinite sea stage");
    if (ports.size() > MAX_PORTS || links.size() > MAX_LINKS)
      throw new IllegalArgumentException("regional port budget exceeded");
    TreeMap<Long, Node> nodes = new TreeMap<>();
    long total = 0;
    for (Port port : ports) {
      Objects.requireNonNull(port, "port");
      if (!Double.isFinite(port.floor()) || port.runoffQuanta() < 0
          || (port.verifiedOceanAnchor() && port.floor() > seaStage))
        throw new IllegalArgumentException("invalid port floor, runoff or sea anchor");
      Node node = new Node(port);
      if (nodes.putIfAbsent(node.key, node) != null)
        throw new IllegalArgumentException("duplicate watershed port");
      total = Math.addExact(total, port.runoffQuanta());
    }
    HashSet<LinkKey> uniqueLinks = new HashSet<>();
    for (Link link : links) {
      Objects.requireNonNull(link, "link");
      if (!Double.isFinite(link.sill())) throw new IllegalArgumentException("nonfinite spill sill");
      long a = key(link.ax(), link.az()), b = key(link.bx(), link.bz());
      if (a == b) throw new IllegalArgumentException("self-loop outlet link");
      Node first = nodes.get(a), second = nodes.get(b);
      if (first == null || second == null)
        throw new IllegalArgumentException("link endpoint outside admitted port set");
      LinkKey canonical = new LinkKey(Math.min(a, b), Math.max(a, b));
      if (!uniqueLinks.add(canonical))
        throw new IllegalArgumentException("duplicate outlet link");
      first.arcs.add(new Arc(second, link.sill()));
      second.arcs.add(new Arc(first, link.sill()));
    }

    // Unknown frontiers contaminate exactly their connected component; an
    // incomplete component cannot be certified as a closed inland watershed.
    ArrayList<Boolean> incompleteComponents = new ArrayList<>();
    ArrayDeque<Node> flood = new ArrayDeque<>();
    for (Node root : nodes.values()) {
      if (root.component >= 0) continue;
      int component = incompleteComponents.size();
      boolean unknown = false;
      root.component = component;
      flood.add(root);
      while (!flood.isEmpty()) {
        Node at = flood.removeFirst();
        unknown |= at.port.unknownFrontier();
        for (Arc arc : at.arcs) if (arc.other.component < 0) {
          arc.other.component = component;
          flood.addLast(arc.other);
        }
      }
      incompleteComponents.add(unknown);
    }

    PriorityQueue<Choice> queue = new PriorityQueue<>(ORDER);
    for (Node node : nodes.values()) if (node.port.verifiedOceanAnchor()) {
      node.spill = seaStage;
      node.hops = 0;
      node.anchor = node.key;
      node.parentKey = Long.MIN_VALUE;
      queue.add(choice(node));
    }
    while (!queue.isEmpty()) {
      Choice current = queue.poll();
      Node at = current.node();
      if (ORDER.compare(current, choice(at)) != 0) continue;
      for (Arc arc : at.arcs) {
        Node next = arc.other();
        double spill = Math.max(Math.max(at.spill, arc.sill()), next.port.floor());
        Choice candidate = new Choice(next, spill, at.hops + 1, at.anchor, at.key);
        if (ORDER.compare(candidate, choice(next)) < 0) {
          next.spill = spill;
          next.hops = candidate.hops();
          next.anchor = candidate.anchor();
          next.parentKey = candidate.parentKey();
          next.parent = at;
          queue.add(candidate);
        }
      }
    }

    // A receiver always has one fewer hops to the chosen sea root. Processing
    // leaves first ensures each quanta contribution is credited exactly once.
    ArrayList<Node> downstream = new ArrayList<>(nodes.values());
    downstream.sort(Comparator.comparingInt((Node n) -> n.hops).reversed()
        .thenComparingLong(n -> n.key));
    for (Node node : downstream) if (node.parent != null)
      node.parent.discharge = Math.addExact(node.parent.discharge, node.discharge);

    long terminals = 0;
    TreeMap<Long, Outlet> result = new TreeMap<>();
    for (Node node : nodes.values()) {
      if (node.parent == null) terminals = Math.addExact(terminals, node.discharge);
      boolean incomplete = incompleteComponents.get(node.component);
      boolean reached = Double.isFinite(node.spill);
      Status status = reached && node.spill <= seaStage ? Status.OCEAN
          : incomplete ? Status.UNRESOLVED
          : reached ? Status.INLAND_SPILL : Status.CLOSED;
      double sill = reached ? node.spill : Double.NaN;
      result.put(node.key, new Outlet(status, sill,
          node.parent == null ? null : node.parent.key, node.discharge));
    }
    return new Solution(result, total, terminals, nodes.size(), uniqueLinks.size());
  }

  private static Choice choice(Node node) {
    return new Choice(node, node.spill, node.hops, node.anchor, node.parentKey);
  }
}
