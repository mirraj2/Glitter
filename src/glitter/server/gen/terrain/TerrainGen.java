package glitter.server.gen.terrain;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import glitter.server.gen.terrain.perlin.IslandsGenerator;
import glitter.server.gen.terrain.perlin.NoiseFunction;
import glitter.server.model.Terrain;
import glitter.server.model.Tile;
import ox.Log;
import ox.Pair;
import ox.Rect;

public class TerrainGen {

  private final Random rand = new Random();

  private Terrain generate(int minTiles) {
    Log.info("Generating terrain (%d tiles)", minTiles);

    NoiseFunction noiseFunction = new IslandsGenerator(4, 12);
    Map<Point, Double> noiseCache = Maps.newHashMap();

    Pair<List<Island>, Rect> pair = findIslands(minTiles, noiseFunction, noiseCache);
    List<Island> islands = pair.a;
    Log.debug("Generated %d islands.", islands.size());

    double[][] noise = computeNoiseArray(pair.b, noiseFunction, noiseCache);
    smooth(noise);

    Tile[][] tiles = new Tile[noise.length][noise[0].length];
    for (int i = 0; i < tiles.length; i++) {
      for (int j = 0; j < tiles[0].length; j++) {
        if (noise[i][j] >= .5) {
          tiles[i][j] = Tile.GRASS;
        } else {
          tiles[i][j] = Tile.VOID;
        }
      }
    }

    return new Terrain(tiles);
  }

  private double[][] computeNoiseArray(Rect bounds, NoiseFunction noiseFunction,
      Map<Point, Double> noiseCache) {
    double[][] ret = new double[bounds.w()][bounds.h()];
    Point p = new Point();
    for (int i = 0; i < bounds.w(); i++) {
      for (int j = 0; j < bounds.h(); j++) {
        p.x = i + bounds.x();
        p.y = j + bounds.y();
        Double val = noiseCache.get(p);
        if (val != null) {
          ret[i][j] = val;
        }
      }
    }
    return ret;
  }

  private Pair<List<Island>, Rect> findIslands(int minTiles, NoiseFunction noiseFunction,
      Map<Point, Double> noiseCache) {
    int centerX = rand.nextInt(Integer.MAX_VALUE / 4 * 3);
    int centerY = rand.nextInt(Integer.MAX_VALUE / 4 * 3);

    List<Island> islands = Lists.newArrayList();

    Rect bounds = new Rect();
    int searchRadius = 0;
    outerloop: while (true) {
      Log.debug("search radius: " + searchRadius);
      for (int x = centerX - searchRadius; x <= centerX + searchRadius; x++) {
        for (int y = centerY - searchRadius; y <= centerY + searchRadius; y++) {
          Island island = findIsland(noiseCache, noiseFunction, x, y);
          if (island != null && island.bounds.w * island.bounds.h < minTiles) {
            islands.add(island);
            if (bounds.w == 0) {
              bounds = island.bounds;
            } else {
              bounds = bounds.union(island.bounds);
            }
            if (bounds.w * bounds.h >= minTiles) {
              break outerloop;
            }
          }
        }
      }
      Log.debug("map size: " + bounds.w * bounds.h);
      searchRadius++;
    }
    Log.debug("map size: " + bounds.w * bounds.h);

    // fill in the map
    for (int x = bounds.x(); x < bounds.maxX(); x++) {
      for (int y = bounds.y(); y < bounds.maxY(); y++) {
        Island island = findIsland(noiseCache, noiseFunction, x, y);
        if (island != null && bounds.contains(island.bounds)) {
          islands.add(island);
        }
      }
    }

    return Pair.of(islands, bounds);
  }

  private static final Point[] dirs = new Point[] { new Point(-1, 0), new Point(1, 0), new Point(0, -1),
      new Point(0, 1) };

  private Island findIsland(Map<Point, Double> noiseCache, NoiseFunction noiseFunction, int x, int y) {
    Point p = new Point(x, y);
    Double val = noiseCache.get(p);

    if (val != null) {
      return null;
    }

    double threshold = .5;

    val = noiseFunction.getValue(x, y);
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

      for (Point dir : dirs) {
        p = new Point(p.x + dir.x, p.y + dir.y);
        val = noiseCache.get(p);
        if (val == null) {
          val = noiseFunction.getValue(p.x, p.y);
          noiseCache.put(p, val);
          if (val >= threshold) {
            queue.add(p);
          }
        }
      }
    }

    return new Island(points);
  }

  private void smooth(double[][] noise) {
    double threshold = .5;
    int w = noise.length;
    int h = noise[0].length;
    for (int i = 0; i < w; i++) {
      for (int j = 0; j < h; j++) {
        if ((i == 0 || noise[i - 1][j] < threshold)
            && (i == w - 1 || noise[i + 1][j] < threshold)) {
          noise[i][j] = 0;
        } else if ((j == 0 || noise[i][j - 1] < threshold)
            && (j == h - 1 || noise[i][j + 1] < threshold)) {
          noise[i][j] = 0;
        }
      }
    }
  }

  // private double[][] generateNoise(int width, int height) {
  // double[][] ret = new double[width][height];
  // islandGen.setOctaves(4, 12);
  //
  // int xOffset = rand.nextInt(Integer.MAX_VALUE);
  // int yOffset = rand.nextInt(Integer.MAX_VALUE);
  //
  // for (int i = 0; i < width; i++) {
  // for (int j = 0; j < height; j++) {
  // ret[i][j] = islandGen.getValue(xOffset + i, yOffset + j);
  // }
  // }
  //
  // return ret;
  // }

  public static Terrain generateFor(int numPlayers) {
    // let's give each player about 5000 tiles of space
    return new TerrainGen().generate(numPlayers * 5000);
  }

  // public static void main(String[] args) {
  // Log.debug("begin");
  // for (int i = 0; i < 1; i++) {
  // TerrainGen.generateFor(1);
  // }
  // }

}
