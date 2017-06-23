package glitter.server.model;

import static ox.util.Utils.random;
import java.util.List;
import com.google.common.collect.Lists;
import ox.Json;
import ox.Log;

public class World {

  public final Terrain terrain;
  public final List<Player> players = Lists.newArrayList();

  public World() {
    terrain = Terrain.createLobby();
  }

  public void addPlayer(Player player) {
    players.add(player);
    spawnInRandomLocation(player);

    Log.debug("player connected. (%d players in world)", players.size());
    player.socket.onClose(() -> {
      Log.debug("player disconnected. (%d players in world)", players.size());
      players.remove(player);

      sendToAll(Json.object()
          .with("command", "removePlayer")
          .with("id", player.id));
    });

    player.send(Json.object()
        .with("command", "enterWorld")
        .with("world", this.toJson()));

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

  private void spawnInRandomLocation(Player player) {
    while (true) {
      int i = random(terrain.width);
      int j = random(terrain.height);
      if (terrain.tiles[i][j].isWalkable()) {
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

  public Json toJson() {
    return Json.object()
        .with("terrain", terrain.toJson());
  }

}
