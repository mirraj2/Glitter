package glitter.server.model;

import ox.Json;

public class World {

  public final Terrain terrain;

  public World() {
    terrain = Terrain.createLobby();
  }

  public Json toJson() {
    return Json.object()
        .with("terrain", terrain.toJson());
  }

}
