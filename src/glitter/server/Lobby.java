package glitter.server;

import bowser.websocket.ClientSocket;
import glitter.server.model.Player;
import glitter.server.model.World;

public class Lobby {

  private final World world = new World();

  public void accept(ClientSocket socket) {
    Player player = new Player(socket);
    world.addPlayer(player);
  }

}
