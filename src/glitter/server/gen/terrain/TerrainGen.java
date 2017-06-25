package glitter.server.gen.terrain;

import static ox.util.Utils.propagate;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import glitter.server.gen.terrain.perlin.ContinentsAndIslands;
import glitter.server.gen.terrain.perlin.NoiseFunction;
import glitter.server.model.Terrain;
import glitter.server.model.Tile;
import ox.Log;
import ox.Pair;
import ox.Rect;

public class TerrainGen {

  private final Random rand = new Random();
  private final double threshold = .6;
  private final int MIN_ISLAND_SIZE = 64;

  private Terrain generate(int minTiles) {
    Log.info("Generating terrain (%d tiles)", minTiles);

    NoiseFunction noiseFunction = new ContinentsAndIslands();
    Map<Point, Double> noiseCache = Maps.newHashMap();

    Pair<List<Island>, Rect> pair;
    while (true) {
      try {
        pair = findIslands(minTiles, noiseFunction, noiseCache);
        break;
      } catch (Exception e) {
        if (e.getMessage().equals("Island too big!")) {
          noiseCache.clear();
          Log.debug("Island too big, trying a different spot.");
          continue;
        }
        throw propagate(e);
      }
    }
    List<Island> islands = pair.a;
    Log.debug("Generated %d islands.", islands.size());

    double[][] noise = computeNoiseArray(pair.b, islands, noiseCache);
    smooth(noise); // TODO smoothing needs to alter the Island object

    int grassCount = 0;
    Tile[][] tiles = new Tile[noise.length][noise[0].length];
    for (int i = 0; i < tiles.length; i++) {
      for (int j = 0; j < tiles[0].length; j++) {
        if (noise[i][j] >= threshold) {
          grassCount++;
          tiles[i][j] = Tile.GRASS;
        } else {
          tiles[i][j] = Tile.VOID;
        }
      }
    }

    Log.debug("Finished generating terrain (%d x %d) (%d tiles)", tiles.length, tiles[0].length,
        grassCount);

    return new Terrain(tiles);
  }

  private double[][] computeNoiseArray(Rect bounds, List<Island> islands,
      Map<Point, Double> noiseCache) {
    // create a 1 tile buffer of void around the whole map
    double[][] ret = new double[bounds.w() + 2][bounds.h() + 2];
    for (Island island : islands) {
      for (Point p : island.points) {
        ret[p.x - bounds.x() + 1][p.y - bounds.y() + 1] = noiseCache.get(p);
      }
    }
    return ret;
  }

  private Pair<List<Island>, Rect> findIslands(int minTiles, NoiseFunction noiseFunction,
      Map<Point, Double> noiseCache) {
    int centerX = rand.nextInt(10_000_000);
    int centerY = rand.nextInt(10_000_000);

    List<Island> islands = Lists.newArrayList();

    Stopwatch watch = Stopwatch.createStarted();
    Rect bounds = new Rect();

    int x = centerX, y = centerY;
    int dirIndex = -1;
    int radius = 1;
    int c = 0;
    int segments = 0;
    int islandTileCount = 0;

    while (true) {
      Island island = findIsland(noiseCache, noiseFunction, x, y,
          (int) Math.min(((1.1 * minTiles) - islandTileCount), minTiles * .7));
      if (island != null) {
        if (island.size() < MIN_ISLAND_SIZE) {
          continue;
        }
        Log.debug("adding island: " + island.points.size() + " tiles");
        islands.add(island);
        if (bounds.w == 0) {
          bounds = island.bounds;
        } else {
          bounds = bounds.union(island.bounds);
        }
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
      Point dir = dirs[dirIndex];
      x += dir.x;
      y += dir.y;
    }

    Log.debug("finished initial island search in " + watch);

    return Pair.of(islands, bounds);
  }

  private static final Point[] dirs = new Point[] {
      new Point(0, -1), new Point(-1, 0),
      new Point(0, 1), new Point(1, 0) };
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

    List<Point> points = Lists.newArrayList();
    Queue<Point> queue = Lists.newLinkedList();
    queue.add(p);

    while (!queue.isEmpty()) {
      p = queue.poll();
      points.add(p);

      if (points.size() > maxSize) {
        throw new RuntimeException("Island too big!");
      }

      for (Point dir : dirs) {
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

  private void smooth(double[][] noise) {
    int w = noise.length;
    int h = noise[0].length;

    // plug up holes, remove single tile islands, remove tiles that are only connected to one other tile.
    for (int pass = 0; pass < 3; pass++) {
      int fixed = 0;
      for (int i = 1; i < w - 1; i++) {
        looping: for (int j = 1; j < h - 1; j++) {
          boolean b = noise[i][j] < threshold;
          int count = 0;
          for (Point dir : dirs) {
            if (noise[i + dir.x][j + dir.y] < threshold == b) {
              if (++count > 1) {
                continue looping;
              }
            }
          }
          noise[i][j] = 1 - noise[i][j];
          fixed++;
        }
      }
      Log.debug("Smoothed %d tiles.", fixed);
    }
  }

  public static Terrain generateFor(int numPlayers) {
    // let's give each player about 2000 tiles of space
    return new TerrainGen().generate(numPlayers * 2000);
  }

}
