package glitter.server.gen.terrain;

import java.util.List;
import com.google.common.collect.Lists;
import glitter.server.arch.GMath;
import glitter.server.arch.GRandom;
import glitter.server.model.Terrain;
import glitter.server.model.Tile;
import ox.Log;

public class TerrainGen {

  private final GRandom rand = new GRandom();

  // threshold for land, in the noise
  private final double threshold = .6;

  private final IslandFinder islandFinder = new IslandFinder(rand, threshold);
  private final TerrainSmoother smoother = new TerrainSmoother(threshold);
  private final BridgeBuilder bridgeBuilder = new BridgeBuilder(rand, threshold);

  private Terrain generate(int minTiles) {
    Log.info("Generating terrain (%d tiles)", minTiles);

    Islands islands = islandFinder.findIslands(minTiles);
    Log.debug("Generated %d islands.", islands.size());

    smoother.smooth(islands);
    bridgeBuilder.genBridges(islands);

    Tile[][] tiles = createTiles(islands);

    genTreasureChests(islands, tiles);

    Log.debug("Finished generating terrain (%d x %d)", tiles.length, tiles[0].length);

    return new Terrain(tiles);
  }

  private void genTreasureChests(Islands islands, Tile[][] tiles) {
    for (Island island : islands) {
      // on average, spawn 1 treasure chest for every 200 tiles
      int numChests = GMath.round(island.points.size() / 200 * (1 + rand.gauss() * .10));
      if (numChests <= 0 && rand.nextDouble() > .1) {
        numChests = 1;
      }
      Log.debug("island will have %d chests", numChests);
      List<Point> chestLocations = Lists.newArrayList();
      int minDistance = 20;
      outerloop: while (numChests > 0) {

        Point p = rand.random(island.points);
        // check to see if this chest location is too close to another chest
        for (Point pp : chestLocations) {
          if (GMath.distSquared(p, pp) < minDistance * minDistance) {
            minDistance = Math.max(minDistance - 1, 0);
            continue outerloop;
          }
        }

        chestLocations.add(p);
        tiles[p.x][p.y] = Tile.CHEST;
        numChests--;
      }
    }
  }

  private Tile[][] createTiles(Islands islands) {
    Tile[][] ret = new Tile[islands.noise.length][islands.noise[0].length];
    for (int i = 0; i < ret.length; i++) {
      for (int j = 0; j < ret[0].length; j++) {
        ret[i][j] = Tile.VOID;
      }
    }

    for (Island island : islands) {
      for (Point p : island.points) {
        ret[p.x][p.y] = Tile.GRASS;
      }
    }

    for (Point p : islands.bridges) {
      ret[p.x][p.y] = Tile.BRIDGE;
    }
    for (Point p : islands.debug) {
      ret[p.x][p.y] = Tile.LAVA;
    }

    return ret;
  }

  public static Terrain generateFor(int numPlayers) {
    // let's give each player about 2000 tiles of space
    return new TerrainGen().generate(numPlayers * 2000);
  }

}
