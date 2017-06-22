package glitter.server;

import bowser.websocket.ClientSocket;
import glitter.server.arch.SwappingQueue;
import glitter.server.model.Player;
import glitter.server.model.World;
import ox.Json;
import ox.Log;

public class Lobby {

  private final SwappingQueue<Player> players = new SwappingQueue<>();
  private final World world = new World();

  public void accept(ClientSocket socket) {
    Player player = new Player(socket);
    players.add(player);
    socket.onClose(() -> {
      players.remove(player);
      Log.info("Lobby has %d players.", players.size());
    });
    Log.info("Lobby has %d players.", players.size());

    player.send(Json.object()
        .with("command", "enterLobby")
        .with("world", world.toJson()));
  }

}
