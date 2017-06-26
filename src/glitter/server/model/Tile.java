package glitter.server.model;

public enum Tile {

  VOID, GRASS, BRIDGE, WATER, LAVA, CHEST;

  public static final int SIZE = 48;

  public boolean isWalkable() {
    return this == GRASS || this == BRIDGE;
  }

}
