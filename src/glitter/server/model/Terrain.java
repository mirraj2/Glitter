package glitter.server.model;

import ox.Json;

public class Terrain {

  public final int width, height;
  public final Tile[][] tiles;

  private Terrain(int width, int height) {
    this.width = width;
    this.height = height;
    tiles = new Tile[width][height];
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

  public static Terrain createRealWorld() {
    Terrain ret = new Terrain(4, 4);
    for (int i = 0; i < ret.width; i++) {
      for (int j = 0; j < ret.height; j++) {
        ret.tiles[i][j] = Tile.GRASS;
      }
    }
    return ret;
  }

  public static Terrain createLobby() {
    Terrain ret = new Terrain(16, 8);
    for (int i = 0; i < ret.width; i++) {
      for (int j = 0; j < ret.height; j++) {
        ret.tiles[i][j] = Tile.GRASS;
      }
    }
    for (int i = 3; i <= 4; i++) {
      for (int j = 2; j <= 4; j++) {
        ret.tiles[i][j] = Tile.WATER;
      }
    }
    for (int i = 3; i <= 6; i++) {
      for (int j = 5; j <= 5; j++) {
        ret.tiles[i][j] = Tile.WATER;
      }
    }
    ret.tiles[5][4] = Tile.WATER;
    ret.tiles[3][5] = Tile.GRASS;
    return ret;
  }

}
