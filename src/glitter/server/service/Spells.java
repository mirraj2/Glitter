package glitter.server.service;

import glitter.server.model.Player;
import ox.Json;

public class Spells {

  public static void cast(Player p, Json json) {
    json.with("casterId", p.id);
    p.world.sendToAll(json, p);
  }

}
