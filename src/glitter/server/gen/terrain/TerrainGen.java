package glitter.server.gen.terrain;

import java.util.List;
import java.util.Random;
import com.google.common.collect.Lists;
import glitter.server.model.Terrain;
import glitter.server.model.Tile;
import ox.Log;

public class TerrainGen {

  private final Random rand = new Random();

  private Terrain generate(int width, int height) {
    Log.info("Generating terrain (%d x %d)", width, height);

    Tile[][] tiles = new Tile[width][height];

    List<Island> islands = generateIslands(width, height);
    Log.debug("%d islands", islands.size());
    islands.forEach(Log::debug);
    
    for(int i = 0; i < width; i++){
      for (int j = 0; j < height; j++) {
        tiles[i][j] = Tile.VOID;
      }
    }

    for (Island island : islands) {
      for (int i = island.x; i < island.x+island.w; i++) {
        for (int j = island.y; j < island.y+ island.h; j++) {
          tiles[i][j] = Tile.GRASS;
        }
      }
    }

    return new Terrain(tiles);
  }

  private List<Island> generateIslands(int width, int height) {
    double targetMass = .6 + rand.nextGaussian() * .08;
    Log.debug("targeting a mass of " + targetMass);

    int targetIslandTiles = (int) (targetMass * (width * height));
    int numIslandTiles = 0;

    List<Island> ret = Lists.newArrayList();

    while (true) {
      int islandsAdded = 0;
      for (int islandSize = (int) (width * .75 + rand.nextGaussian() * .1); islandSize >= 8; islandSize *= .85) {
        // the smaller the island, the more attempts it gets
        int attempts = (width * height) / (islandSize * islandSize);
        innerLoop: for (int n = 0; n < attempts; n++) {
          int x = rand.nextInt(width);
          int y = rand.nextInt(height);
          int w = islandSize;
          int h = islandSize;

          if (x + w > width || y + h > height) {
            continue innerLoop;
          }

          for (Island island : ret) {
            if (island.intersects(x, y, w, h)) {
              continue innerLoop;
            }
          }

          ret.add(new Island(x, y, w, h));
          islandsAdded++;
          numIslandTiles += w * h;
        }
      }
      Log.debug(ret.size() + " islands, mass of " + 1.0 * numIslandTiles / (width * height));
      if (islandsAdded == 0 || numIslandTiles >= targetIslandTiles) {
        break;
      }
    }

    return ret;
  }

  public static class Island {
    public final int x, y, w, h;

    public Island(int x, int y, int w, int h) {
      this.x = x;
      this.y = y;
      this.w = w;
      this.h = h;
    }

    public boolean intersects(int x, int y, int w, int h) {
      if ((x >= this.x + this.w) || (x + w <= this.x)
          || (y >= this.y + this.h) || (y + h <= this.y)) {
        return false;
      }
      return true;
    }

    @Override
    public String toString() {
      return "Island [x=" + x + ", y=" + y + ", w=" + w + ", h=" + h + "]";
    }

  }

  public static Terrain generateFor(int numPlayers) {
    // let's give each player about 5000 tiles of space
    int totalTiles = numPlayers * 5000;

    int sqrt = (int) Math.sqrt(totalTiles);
    return new TerrainGen().generate(sqrt, sqrt);
  }

  // public static void main(String[] args) {
  // Log.debug("begin");
  // for (int i = 0; i < 1; i++) {
  // TerrainGen.generateFor(1);
  // }
  // }

}
