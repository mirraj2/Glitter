package glitter.client;

import bowser.model.Controller;
import bowser.model.Handler;

public class WorldData extends Controller {

  @Override
  public void init() {
    // route("GET", "/world/*/minimap").to(getMinimap);
  }

  private final Handler getMinimap = (request, response) -> {
    // long id = request.getInt(1);
    // World world = World.idWorlds.get(id);
    // IO.from(world.minimapImage).to(response.getOutputStream());
  };

}
