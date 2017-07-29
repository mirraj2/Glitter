package glitter.server.gen;

import static com.google.common.base.Preconditions.checkState;
import java.util.Collections;
import java.util.List;
import com.google.common.collect.Lists;
import glitter.server.arch.GRandom;
import glitter.server.arch.Rect;
import glitter.server.model.Terrain;
import glitter.server.model.Tile;
import glitter.server.model.World;

public class StructureGen {

  private final GRandom rand;

  public StructureGen(GRandom rand) {
    this.rand = rand;

  }

  public void generate(World world, Islands islands) {
    for (Island island : islands) {
      generate(world, island);
    }
  }

  private void generate(World world, Island island) {
    int numStructures = island.size() / 100;
    numStructures = rand.gaussInt(numStructures, numStructures / 10);

    numStructures = Math.max(1, numStructures);

    for (int i = 0; i < numStructures; i++) {
      Rect r = findLargeRect(world.terrain, island, 6, 6);
      if (r == null) {
        break;
      }
      generateStructure(r, world.terrain);
    }
  }

  private void generateStructure(Rect plotArea, Terrain terrain) {
    Rect r = getStructureBounds(plotArea);

    double averageNumDoors = (plotArea.w * 2 + plotArea.h * 2 - 4) / 16.0;
    int numDoors = rand.gaussInt(averageNumDoors, averageNumDoors * .5);
    numDoors = Math.max(numDoors, 1);

    Tile[][] tiles = terrain.tiles;
    List<Point> wallPoints = Lists.newArrayList();

    for (int x = r.x(); x < r.maxX(); x++) {
      wallPoints.add(new Point(x, r.y()));
      wallPoints.add(new Point(x, r.maxY() - 1));
    }
    for (int y = r.y() + 1; y < r.maxY() - 1; y++) {
      wallPoints.add(new Point(r.x(), y));
      wallPoints.add(new Point(r.maxX() - 1, y));
    }

    for (Point p : wallPoints) {
      tiles[p.x][p.y] = Tile.WALL;
    }

    // remove the corners
    checkState(wallPoints.remove(new Point(r.x(), r.y())));
    checkState(wallPoints.remove(new Point(r.maxX() - 1, r.y())));
    checkState(wallPoints.remove(new Point(r.maxX() - 1, r.maxY() - 1)));
    checkState(wallPoints.remove(new Point(r.x(), r.maxY() - 1)));

    Collections.shuffle(wallPoints, rand);

    numDoors = Math.min(numDoors, wallPoints.size());
    for (int i = 0; i < numDoors; i++) {
      Point p = wallPoints.get(i);
      tiles[p.x][p.y] = Tile.DOOR;
    }
  }

  private Rect getStructureBounds(Rect plotArea) {
    // give the structure some room around it for players to walk. We never want to obstruct a bridge / make movement to
    // another place impossible.
    plotArea.x++;
    plotArea.y++;
    plotArea.w -= 2;
    plotArea.h -= 2;

    int width = rand.gaussInt(6, 2);
    width = Math.max(width, 4);
    width = Math.min(width, plotArea.w());

    int height = rand.gaussInt(width, width * .15);
    height = Math.max(height, 4);
    height = Math.min(height, plotArea.h());

    return randomSubRect(plotArea, width, height);
  }

  private Rect randomSubRect(Rect r, int w, int h) {
    int x = rand.nextInt(r.w() - w + 1);
    int y = rand.nextInt(r.h() - h + 1);
    return new Rect(r.x + x, r.y + y, w, h);
  }

  private Rect findLargeRect(Terrain terrain, Island island, int minWidth, int minHeight) {
    for (int attempt = 0; attempt < 16; attempt++) {
      Rect r = findRandomRect(terrain, island);
      if (r.w >= minWidth && r.h >= minHeight) {
        return r;
      }
    }
    return null;
  }

  private Rect findRandomRect(Terrain terrain, Island island) {
    Point p = rand.random(island.points);

    Dir[] dirs = Dir.values();

    // shoot rays in 8 directions
    int[] rayLengths = new int[dirs.length];
    for (int i = 0; i < dirs.length; i++) {
      rayLengths[i] = traceRay(p.x, p.y, dirs[i], terrain);
    }

    // determine which diagonal is the longest
    Dir bestDir = null;
    int bestLen = -1;
    for (int i = 1; i < dirs.length; i += 2) {
      int a = rayLengths[i - 1];
      int b = rayLengths[i];
      int c = rayLengths[(i + 1) % rayLengths.length];

      int len = Math.min(Math.min(a, b), c);
      if (len > bestLen) {
        bestDir = dirs[i];
        bestLen = len;
      }
    }

    Rect r = findSquare(terrain, p, bestDir);
    growRect(terrain, r);
    return r;
  }

  /**
   * Given a rectangle, attempts to greedily grow it in all directions.
   */
  private void growRect(Terrain terrain, Rect r) {
    boolean growing = true;
    while (growing) {
      growing = false;
      if (canGrowNorth(terrain, r)) {
        growing = true;
        r.y--;
        r.h++;
      }
      if (canGrowEast(terrain, r)) {
        growing = true;
        r.w++;
      }
      if (canGrowSouth(terrain, r)) {
        growing = true;
        r.h++;
      }
      if (canGrowWest(terrain, r)) {
        growing = true;
        r.x--;
        r.w++;
      }
    }
  }

  private boolean canGrowNorth(Terrain terrain, Rect r) {
    for (int i = r.x(); i <= r.maxX(); i++) {
      if (terrain.get(i, r.y() - 1) != Tile.GRASS) {
        return false;
      }
    }
    return true;
  }

  private boolean canGrowEast(Terrain terrain, Rect r) {
    for (int j = r.y(); j <= r.maxY(); j++) {
      if (terrain.get((int) r.maxX(), j) != Tile.GRASS) {
        return false;
      }
    }
    return true;
  }

  private boolean canGrowSouth(Terrain terrain, Rect r) {
    for (int i = r.x(); i <= r.maxX(); i++) {
      if (terrain.get(i, (int) r.maxY()) != Tile.GRASS) {
        return false;
      }
    }
    return true;
  }

  private boolean canGrowWest(Terrain terrain, Rect r) {
    for (int j = r.y(); j <= r.maxY(); j++) {
      if (terrain.get(r.x() - 1, j) != Tile.GRASS) {
        return false;
      }
    }
    return true;
  }

  /**
   * Starting at a given point, goes along the diagonal, until it runs out of space. Returns the resulting square.
   */
  private Rect findSquare(Terrain terrain, Point p, Dir dir) {
    int currentX = p.x;
    int currentY = p.y;

    outerloop: while (true) {
      currentX += dir.dx;
      currentY += dir.dy;

      // check the new row
      for (int i = Math.min(p.x, currentX); i <= Math.max(p.x, currentX); i++) {
        if (terrain.get(i, currentY) != Tile.GRASS) {
          break outerloop;
        }
      }

      // check the new column
      for (int j = Math.min(p.y, currentY); j <= Math.max(p.y, currentY); j++) {
        if (terrain.get(currentX, j) != Tile.GRASS) {
          break outerloop;
        }
      }
    }

    currentX -= dir.dx;
    currentY -= dir.dy;

    return createRect(p, new Point(currentX, currentY));
  }

  private Rect createRect(Point a, Point b) {
    return new Rect(Math.min(a.x, b.x), Math.min(a.y, b.y), Math.abs(a.x - b.x) + 1, Math.abs(a.y - b.y) + 1);
  }

  private int traceRay(int x, int y, Dir dir, Terrain terrain) {
    int ret = 0;
    while (true) {
      Tile tile = terrain.get(x, y);
      if (tile != Tile.GRASS) {
        break;
      }
      ret++;
      x += dir.dx;
      y += dir.dy;
    }
    return ret;
  }

  private static enum Dir {
    NORTH(0, -1), NORTH_EAST(1, -1), EAST(1, 0), SOUTH_EAST(1, 1), SOUTH(0, 1), SOUTH_WEST(-1, 1), WEST(-1, 0),
    NORTH_WEST(-1, -1);

    public final int dx, dy;

    private Dir(int dx, int dy) {
      this.dx = dx;
      this.dy = dy;
    }
  }

  // private List<TreasureChest> genTreasureChests(Islands islands, Terrain terrain) {
  // List<TreasureChest> ret = Lists.newArrayList();
  //
  // for (Island island : islands) {
  // // on average, spawn 1 treasure chest for every 200 tiles
  // int numChests = GMath.round(island.points.size() / 200 * (1 + rand.gauss() * .10));
  // if (numChests <= 0 && rand.nextDouble() > .1) {
  // numChests = 1;
  // }
  // Log.debug("island will have %d chests", numChests);
  // List<TreasureChest> islandChests = Lists.newArrayList();
  // int minDistance = 20;
  // outerloop: while (numChests > 0) {
  //
  // Point p = rand.random(island.points);
  // Rect r = new Rect(p.x * Tile.SIZE, p.y * Tile.SIZE, TreasureChest.WIDTH, TreasureChest.HEIGHT);
  // r.x += rand.nextInt(Tile.SIZE) - Tile.SIZE / 2;
  // r.y += rand.nextInt(Tile.SIZE) - Tile.SIZE / 2;
  //
  // // first check to see if this chest is not overlapping a bad tile
  // List<TileLoc> unwalkable = terrain.getTilesIntersecting(r, loc -> {
  // return !loc.tile.isWalkable();
  // });
  //
  // if (!unwalkable.isEmpty()) {
  // continue outerloop;
  // }
  //
  // for (TreasureChest chest : islandChests) {
  // if (GMath.distSquared(chest.bounds.x, chest.bounds.y, r.x, r.y) < minDistance * minDistance) {
  // minDistance = Math.max(minDistance - 1, 0);
  // continue outerloop;
  // }
  // }
  //
  // TreasureChest chest = new TreasureChest(r.x, r.y);
  // islandChests.add(chest);
  // ret.add(chest);
  // numChests--;
  // }
  // }
  //
  // return ret;
  // }

}
