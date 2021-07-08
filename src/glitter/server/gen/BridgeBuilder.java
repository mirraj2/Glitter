package glitter.server.gen;

import static ox.util.Functions.map;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import glitter.server.arch.GRandom;
import glitter.server.arch.Ref;
import glitter.server.gen.Traversals.TState;
import ox.Log;

public class BridgeBuilder {

  private final GRandom rand;
  private final double threshold;

  public BridgeBuilder(GRandom rand, double threshold) {
    this.rand = rand;
    this.threshold = threshold;
  }

  public void genBridges(Islands islands) {
    if (islands.size() == 1) {
      // no bridges necessary
      return;
    }

    Multimap<Cluster, Cluster> edges = HashMultimap.create();

    List<Cluster> clusters = map(islands, i -> new Cluster(i.points));
    Collections.shuffle(clusters, rand);

    for (int n = 0; n < 3; n++) {
      for (Cluster from : clusters) {
        Set<Cluster> possible = Sets.newHashSet(clusters);
        possible.remove(from);
        possible.removeAll(edges.get(from));

        if (!possible.isEmpty()) {
          Cluster to = createBridge(from, possible, islands);
          if (to != null) {
            edges.put(from, to);
            edges.put(to, from);
          }
        }
      }
    }
  }

  private Cluster createBridge(Cluster from, Collection<Cluster> possible, Islands islands) {
    Ref<Cluster> ref = Ref.ofNull();

    List<Point> path = Traversals.flood(from.points, p -> {
      double noise = islands.getNoise(p.x, p.y);
      if (noise >= threshold) {
        if (!from.points.contains(p)) {
          ref.val = getClusterForPoint(p, possible);
          if (ref.val != null) {
            return TState.RETURN_PATH;
          }
        }
      }
      return TState.FILL;
    });

    path = path.subList(1, path.size() - 1);
    if (path.isEmpty()) {
      Log.debug("empty path");
      return null;
    }

    // make sure this bridge doesn't cross another island
    for (Point p : path) {
      if (islands.noise[p.x][p.y] > threshold) {
        Log.debug("Skipping bridge that crosses over a land mass.");
        // islands.debug.addAll(path);
        return null;
      }
    }

    Log.info("Creating %d length bridge", path.size());
    islands.bridges.addAll(path);
    from.points.addAll(path);
    ref.val.points.addAll(path);

    for (Point p : path) {
      islands.noise[p.x][p.y] = threshold;
    }

    return ref.val;
  }

  private Cluster getClusterForPoint(Point p, Collection<Cluster> clusters) {
    for (Cluster c : clusters) {
      if (c.points.contains(p)) {
        return c;
      }
    }
    return null;
  }

  private static class Cluster {
    private final Set<Point> points;

    public Cluster(Set<Point> points) {
      this.points = Sets.newHashSet(points);

    }
  }

}
