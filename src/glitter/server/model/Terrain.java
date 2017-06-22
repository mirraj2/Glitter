package glitter.server.model;

import ox.Json;

public class Terrain {

  public final int width, height;
  private final Tile[][] tiles;

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

  public static Terrain createLobby() {
    Terrain ret = new Terrain(8, 8);
    for (int i = 0; i < ret.width; i++) {
      for (int j = 0; j < ret.height; j++) {
        ret.tiles[i][j] = Tile.GRASS;
      }
    }
    return ret;
  }

}
