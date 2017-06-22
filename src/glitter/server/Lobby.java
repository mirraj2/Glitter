package glitter.server;

import static ox.util.Utils.random;
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
    spawnInRandomLocation(world, player);
    players.add(player);
    socket.onClose(() -> {
      players.remove(player);
      Log.info("Lobby has %d players.", players.size());

      sendToAll(Json.object()
          .with("command", "removePlayer")
          .with("id", player.id));
    });
    Log.info("Lobby has %d players.", players.size());

    player.send(Json.object()
        .with("command", "enterLobby")
        .with("world", world.toJson()));

    sendToAll(createAddPlayerJson(player));

    player.send(Json.object()
        .with("command", "takeControl")
        .with("id", player.id));

    Json commands = Json.array();
    for (Player p : players) {
      if (p != player) {
        commands.add(createAddPlayerJson(p));
      }
    }
    player.send(commands);
  }

  private Json createAddPlayerJson(Player p) {
    return Json.object()
        .with("command", "addPlayer")
        .with("player", Json.object()
            .with("id", p.id)
            .with("x", p.x)
            .with("y", p.y));
  }

  private void spawnInRandomLocation(World world, Player player) {
    while (true) {
      int i = random(world.terrain.width);
      int j = random(world.terrain.height);
      if (world.terrain.tiles[i][j].isWalkable()) {
        player.moveToTile(i, j);
        return;
      }
    }
  }

  private void sendToAll(Json json) {
    for (Player player : players) {
      player.send(json);
    }
  }

}
