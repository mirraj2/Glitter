package server.gen.world;

import java.util.Iterator;
import java.util.List;
import com.google.common.collect.ImmutableList;
import ox.Log;
import ox.Rect;

public class TerrainSmoother {

  private final double threshold;

  public TerrainSmoother(double threshold) {
    this.threshold = threshold;
  }

  public void smooth(Islands islands) {
    computeNoiseArray(islands);

    for (int i = 0; i < 3; i++) {
      int fixed = 0;
      for (Island island : islands) {
        fixed += smooth(island, islands.noise);
      }
      Log.debug("Smoothed %d tiles.", fixed);
    }

  }

  private int smooth(Island island, double[][] noise) {
    int fixed = 0;

    // plug up void holes
    Point buf = new Point();
    for (int i = island.bounds.x(); i < island.bounds.maxX(); i++) {
      looping: for (int j = island.bounds.y(); j < island.bounds.maxY(); j++) {
        if (noise[i][j] < threshold) {
          for (Point dir : Traversals.dirs) {
            buf.x = i + dir.x;
            buf.y = j + dir.y;
            if (!island.points.contains(buf)) {
              continue looping;
            }
          }
          Point p = new Point(i, j);
          noise[i][j] = threshold;
          island.points.add(p);
          fixed++;
        }
      }
    }

    // remove tiles which are only connected by a single tile
    Iterator<Point> iter = island.points.iterator();
    looping: while (iter.hasNext()) {
      Point p = iter.next();
      int tileCount = 0;
      for (Point dir : Traversals.dirs) {
        if (noise[p.x + dir.x][p.y + dir.y] >= threshold) {
          if (++tileCount > 1) {
            continue looping;
          }
        }
      }
      noise[p.x][p.y] = 0;
      iter.remove();
      fixed++;
    }

    return fixed;
  }

  private void computeNoiseArray(Islands islands) {
    // create a 1 tile buffer of void around the whole map
    Rect bounds = islands.bounds;
    bounds.x--;
    bounds.y--;
    bounds.w += 2;
    bounds.h += 2;

    double[][] noise = new double[bounds.w()][bounds.h()];
    for (Island island : islands) {
      for (Point p : island.points) {
        noise[p.x - bounds.x()][p.y - bounds.y()] = islands.noiseCache.get(p);
      }
    }
    islands.noise = noise;

    // move all the islands bounding boxes to be based on the outer bounds
    for (Island island : islands) {
      island.bounds.x -= islands.bounds.x;
      island.bounds.y -= islands.bounds.y;

      List<Point> points = ImmutableList.copyOf(island.points);
      island.points.clear();
      for (Point p : points) {
        p.x -= islands.bounds.x;
        p.y -= islands.bounds.y;
        island.points.add(p);
      }
    }
    islands.noiseCache.clear(); // this should no longer be used, now that we modified the points.
    islands.bounds.location(0, 0);

  }

}
