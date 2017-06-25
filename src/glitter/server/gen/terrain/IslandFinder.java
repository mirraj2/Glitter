package glitter.server.gen.terrain;

import static ox.util.Utils.propagate;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import glitter.server.gen.terrain.perlin.ContinentsAndIslands;
import glitter.server.gen.terrain.perlin.NoiseFunction;
import ox.Log;

/**
 * Discovers islands in the noise!
 */
public class IslandFinder {

  private final int MIN_ISLAND_SIZE = 64;
  private final Random rand;
  private final double threshold;

  public IslandFinder(Random rand, double threshold) {
    this.rand = rand;
    this.threshold = threshold;
  }

  public Islands findIslands(int minTiles) {
    NoiseFunction noiseFunction = new ContinentsAndIslands();

    Islands islands;
    while (true) {
      try {
        islands = findIslands(minTiles, noiseFunction);
        break;
      } catch (Exception e) {
        if (e.getMessage().equals("Island too big!")) {
          Log.debug("Island too big, trying a different spot.");
          continue;
        }
        throw propagate(e);
      }
    }
    return islands;
  }

  private Islands findIslands(int minTiles, NoiseFunction noiseFunction) {
    int centerX = rand.nextInt(10_000_000);
    int centerY = rand.nextInt(10_000_000);

    Islands islands = new Islands();

    Stopwatch watch = Stopwatch.createStarted();

    int x = centerX, y = centerY;
    int dirIndex = -1;
    int radius = 1;
    int c = 0;
    int segments = 0;
    int islandTileCount = 0;

    while (true) {
      Island island = findIsland(islands.noiseCache, noiseFunction, x, y,
          (int) Math.min(((1.1 * minTiles) - islandTileCount), minTiles * .7));
      if (island != null) {
        if (island.size() < MIN_ISLAND_SIZE) {
          continue;
        }
        Log.debug("adding island: " + island.points.size() + " tiles");
        islands.add(island);
        islandTileCount += island.size();
        if (islandTileCount >= minTiles) {
          break;
        }
      }
      if (++c == radius) {
        c = 0;
        dirIndex = (dirIndex + 1) % 4;
        if (segments++ % 2 == 0) {
          radius++;
        }
      }
      Point dir = TerrainGen.dirs[dirIndex];
      x += dir.x;
      y += dir.y;
    }

    Log.debug("finished initial island search in " + watch);

    return islands;
  }

  private final Point pointBuf = new Point();

  private Island findIsland(Map<Point, Double> noiseCache, NoiseFunction noiseFunction, int x, int y, int maxSize) {
    pointBuf.x = x;
    pointBuf.y = y;
    Double val = noiseCache.get(pointBuf);

    if (val != null) {
      return null;
    }

    Point p = new Point(x, y);

    val = noiseFunction.getValue(p.x, p.y);
    noiseCache.put(p, val);
    if (val < threshold) {
      return null;
    }

    Set<Point> points = Sets.newHashSet();
    Queue<Point> queue = Lists.newLinkedList();
    queue.add(p);

    while (!queue.isEmpty()) {
      p = queue.poll();
      points.add(p);

      if (points.size() > maxSize) {
        throw new RuntimeException("Island too big!");
      }

      for (Point dir : TerrainGen.dirs) {
        Point next = new Point(p.x + dir.x, p.y + dir.y);
        val = noiseCache.get(next);
        if (val == null) {
          val = noiseFunction.getValue(next.x, next.y);
          noiseCache.put(next, val);
          if (val >= threshold) {
            queue.add(next);
          }
        }
      }
    }

    return new Island(points);
  }

}
