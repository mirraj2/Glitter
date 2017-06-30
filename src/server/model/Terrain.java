package server.model;

import java.util.List;
import java.util.function.Predicate;
import com.google.common.collect.Lists;
import ox.Json;
import ox.Rect;
import server.arch.GMath;

public class Terrain {

  public final int width, height;
  public final Tile[][] tiles;

  public Terrain(int width, int height) {
    this.width = width;
    this.height = height;
    tiles = new Tile[width][height];
  }

  public Terrain(Tile[][] tiles) {
    this.tiles = tiles;
    this.width = tiles.length;
    this.height = tiles[0].length;
  }

  public List<TileLoc> getTilesIntersecting(Rect r, Predicate<TileLoc> filter) {
    int minI = Math.max(GMath.floor(r.x / Tile.SIZE), 0);
    int minJ = Math.max(GMath.floor(r.y / Tile.SIZE), 0);
    int maxI = Math.min(GMath.floor(r.maxX() / Tile.SIZE), this.width - 1);
    int maxJ = Math.min(GMath.floor(r.maxY() / Tile.SIZE), this.height - 1);

    List<TileLoc> ret = Lists.newArrayList();

    TileLoc loc = new TileLoc();
    for (int i = minI; i <= maxI; i++) {
      for (int j = minJ; j <= maxJ; j++) {
        loc.i = i;
        loc.j = j;
        loc.tile = tiles[i][j];
        if (filter.test(loc)) {
          ret.add(loc);
          loc = new TileLoc();
        }
      }
    }

    return ret;
  }

  public Json toJson() {
    return Json.object()
        .with("width", width)
        .with("height", height)
        .with("tiles", serializeTiles());
  }

  public boolean isWalkable(int i, int j) {
    if (i < 0 || j < 0 || i >= width || j >= height) {
      return false;
    }
    Tile tile = tiles[i][j];
    return tile.isWalkable();
  }

  private Json serializeTiles() {
    Json ret = Json.array();
    for (int i = 0; i < width; i++) {
      Json col = Json.array();
      for (int j = 0; j < height; j++) {
        col.add(tiles[i][j].ordinal());
      }
      ret.add(col);
    }
    return ret;
  }

  public static class TileLoc {
    public Tile tile;
    public int i, j;
  }

}
