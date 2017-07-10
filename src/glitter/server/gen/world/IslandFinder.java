package glitter.server.gen.world;

import static com.google.common.base.Preconditions.checkNotNull;
import static ox.util.Utils.propagate;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import glitter.server.arch.GRandom;
import glitter.server.gen.world.Traversals.TState;
import glitter.server.gen.world.perlin.ContinentsAndIslands;
import glitter.server.gen.world.perlin.NoiseFunction;
import ox.Log;

/**
 * Discovers islands in the noise!
 */
public class IslandFinder {

  private final int MIN_ISLAND_SIZE = 64;
  private final GRandom rand;
  private final double threshold;

  public IslandFinder(GRandom rand, double threshold) {
    this.rand = rand;
    this.threshold = threshold;
  }

  public Islands findIslands(int minTiles) {
    NoiseFunction noiseFunction = new ContinentsAndIslands();

    Islands islands = null;
    for (int i = 0; i < 1000; i++) {
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

    checkNotNull(islands, "After 1000 iterations, all the islands found were too big. minTiles = " + minTiles);

    return islands;
  }

  private Islands findIslands(int minTiles, NoiseFunction noiseFunction) {
    int centerX = rand.nextInt(10_000_000);
    int centerY = rand.nextInt(10_000_000);

    Islands islands = new Islands();

    Stopwatch watch = Stopwatch.createStarted();

    AtomicInteger islandTileCount = new AtomicInteger(0);

    Traversals.spiral(centerX, centerY, p -> {
      Island island = findIsland(islands.noiseCache, noiseFunction, p.x, p.y,
          (int) Math.min(((1.1 * minTiles) - islandTileCount.get()), minTiles * .7));
      if (island != null) {
        if (island.size() < MIN_ISLAND_SIZE) {
          return true;
        }
        Log.debug("adding island: " + island.points.size() + " tiles");
        islands.add(island);
        if (islandTileCount.addAndGet(island.size()) >= minTiles) {
          return false;
        }
      }
      return true;
    });

    Log.debug("finished initial island search in " + watch);

    return islands;
  }

  private final Point pointBuf = new Point();

  private Island findIsland(Map<Point, Double> noiseCache, NoiseFunction noiseFunction, int x, int y, int maxSize) {
    pointBuf.x = x;
    pointBuf.y = y;

    if (noiseCache.get(pointBuf) != null) {
      return null;
    }

    Set<Point> points = Sets.newHashSet();
    
    Traversals.flood(new Point(x, y), p -> {
      Double val = noiseCache.get(p);
      if (val == null) {
        val = noiseFunction.getValue(p.x, p.y);
        noiseCache.put(p, val);
        if (val >= threshold) {
          points.add(p);
          if (points.size() > maxSize) {
            throw new RuntimeException("Island too big!");
          }
          return TState.FILL;
        }
      }
      return TState.NO_FILL;
    });

    return new Island(points);
  }

}
