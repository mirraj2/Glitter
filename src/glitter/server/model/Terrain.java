package glitter.server.model;

import ox.Json;

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

}
