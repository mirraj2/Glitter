package glitter.server.gen.world;

import java.util.List;
import com.google.common.collect.Lists;
import glitter.server.arch.GMath;
import glitter.server.arch.GRandom;
import glitter.server.arch.Rect;
import glitter.server.model.Terrain;
import glitter.server.model.Terrain.TileLoc;
import glitter.server.model.Tile;
import glitter.server.model.TreasureChest;
import glitter.server.model.World;
import ox.Log;

public class WorldGen {

  private final GRandom rand = new GRandom();

  // threshold for land, in the noise
  private final double threshold = .6;

  private final IslandFinder islandFinder = new IslandFinder(rand, threshold);
  private final TerrainSmoother smoother = new TerrainSmoother(threshold);
  private final BridgeBuilder bridgeBuilder = new BridgeBuilder(rand, threshold);

  private World generate(int minTiles) {
    Log.info("Using random with seed: " + rand.seed);

    Log.info("Generating terrain (%d tiles)", minTiles);

    Islands islands = islandFinder.findIslands(minTiles);
    Log.debug("Generated %d islands.", islands.size());

    smoother.smooth(islands);
    bridgeBuilder.genBridges(islands);

    Tile[][] tiles = createTiles(islands);
    Terrain terrain = new Terrain(tiles);
    World ret = new World(rand, terrain, true);

    ret.addEntities(genTreasureChests(islands, terrain));

    Log.debug("Finished generating terrain (%d x %d)", tiles.length, tiles[0].length);

    return ret;
  }

  private List<TreasureChest> genTreasureChests(Islands islands, Terrain terrain) {
    List<TreasureChest> ret = Lists.newArrayList();

    for (Island island : islands) {
      // on average, spawn 1 treasure chest for every 200 tiles
      int numChests = GMath.round(island.points.size() / 200 * (1 + rand.gauss() * .10));
      if (numChests <= 0 && rand.nextDouble() > .1) {
        numChests = 1;
      }
      Log.debug("island will have %d chests", numChests);
      List<TreasureChest> islandChests = Lists.newArrayList();
      int minDistance = 20;
      outerloop: while (numChests > 0) {

        Point p = rand.random(island.points);
        Rect r = new Rect(p.x * Tile.SIZE, p.y * Tile.SIZE, TreasureChest.WIDTH, TreasureChest.HEIGHT);
        r.x += rand.nextInt(Tile.SIZE) - Tile.SIZE / 2;
        r.y += rand.nextInt(Tile.SIZE) - Tile.SIZE / 2;

        // first check to see if this chest is not overlapping a bad tile
        List<TileLoc> unwalkable = terrain.getTilesIntersecting(r, loc -> {
          return !loc.tile.isWalkable();
        });

        if (!unwalkable.isEmpty()) {
          continue outerloop;
        }

        for (TreasureChest chest : islandChests) {
          if (GMath.distSquared(chest.bounds.x, chest.bounds.y, r.x, r.y) < minDistance * minDistance) {
            minDistance = Math.max(minDistance - 1, 0);
            continue outerloop;
          }
        }

        TreasureChest chest = new TreasureChest(r.x, r.y);
        islandChests.add(chest);
        ret.add(chest);
        numChests--;
      }
    }

    return ret;
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

  public static World generateFor(int numPlayers) {
    // let's give each player about 2000 tiles of space
    return new WorldGen().generate(numPlayers * 2000);
  }

}
