package glitter.server.model;

import ox.Json;

public class TreasureChest extends Entity {

  public static final int WIDTH = Tile.SIZE, HEIGHT = Tile.SIZE;

  public final double x, y;
  public final int width = WIDTH, height = HEIGHT;

  public TreasureChest(double x, double y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public Json toJson() {
    return super.toJson()
        .with("x", x)
        .with("y", y)
        .with("width", width)
        .with("height", height);
  }

}
