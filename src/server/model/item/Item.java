package server.model.item;

import ox.Json;
import server.model.Entity;
import server.model.Tile;

public abstract class Item extends Entity {

  public final String name;
  public final Rarity rarity;
  public final String flavorText = "";

  public Item(String name, Rarity rarity) {
    super(Tile.SIZE, Tile.SIZE);

    this.name = name;
    this.rarity = rarity;
  }

  @Override
  public Json toJson() {
    Json ret = Json.object()
        .with("id", id)
        .with("name", name)
        .with("rarity", rarity);
    if (!flavorText.isEmpty()) {
      ret.with("flavorText", flavorText);
    }
    return ret;
  }

  @Override
  public String toString() {
    return name;
  }

  public static enum Rarity {
    COMMON, RARE, EPIC, LEGENDARY;
  }

}
