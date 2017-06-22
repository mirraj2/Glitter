package glitter.server.model;

public enum Tile {

  VOID, GRASS, STONE, WATER, LAVA;

  public static final int SIZE = 48;

  public boolean isWalkable() {
    return this == GRASS || this == STONE;
  }

}
