package glitter.server.gen;

import glitter.server.arch.GRandom;
import glitter.server.model.Terrain;
import glitter.server.model.Tile;
import glitter.server.model.World;
import ox.Log;

public class WorldGen {

  private final GRandom rand = new GRandom();

  // threshold for land, in the noise
  private final double threshold = .6;

  private final IslandFinder islandFinder = new IslandFinder(rand, threshold);
  private final TerrainSmoother smoother = new TerrainSmoother(threshold);
  private final BridgeBuilder bridgeBuilder = new BridgeBuilder(rand, threshold);
  private final StructureGen structureGen = new StructureGen(rand);

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

    structureGen.generate(ret, islands);

    Log.debug("Finished generating terrain (%d x %d)", tiles.length, tiles[0].length);

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
