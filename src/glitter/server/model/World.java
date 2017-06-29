package glitter.server.model;

import static com.google.common.base.Preconditions.checkNotNull;
import static ox.util.Utils.random;
import java.util.Collection;
import java.util.Map;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import glitter.server.arch.GRandom;
import glitter.server.service.LootMaster;
import ox.Json;
import ox.Log;

public class World {

  public final GRandom rand;
  public final Terrain terrain;
  public final LootMaster lootMaster;
  public Collection<Player> players = Lists.newCopyOnWriteArrayList();
  public final Map<Long, Entity> idEntities = Maps.newConcurrentMap();
  public final AdminConsole console = new AdminConsole(this);

  public World(GRandom rand, Terrain terrain) {
    this.rand = rand;
    this.terrain = terrain;
    this.lootMaster = new LootMaster(rand);
  }

  public void start() {
    new GameLoop(this::update);
  }

  private void update(double t) {
    for (Player player : players) {
      player.update(t);
    }

    for (Player player : players) {
      player.flushMessages();
    }
  }

  public void addPlayer(Player player) {
    players.add(player);
    player.world = this;
    spawnInRandomLocation(player);

    Log.debug("player connected. (%d players in world)", players.size());

    player.send(Json.object()
        .with("command", "enterWorld")
        .with("world", this.toJson()));

    sendToAll(createAddPlayerJson(player));

    player.send(Json.object()
        .with("command", "takeControl")
        .with("id", player.id));

    for (Player p : players) {
      if (p != player) {
        player.send(createAddPlayerJson(p));
      }
    }
  }

  public void removePlayer(Player player) {
    Log.debug("player disconnected. (%d players in world)", players.size());
    players.remove(player);

    sendToAll(Json.object()
        .with("command", "removePlayer")
        .with("id", player.id));
  }

  public void removeEntity(long entityId) {
    Entity removed = idEntities.remove(entityId);
    checkNotNull(removed, "Could not find entity: " + entityId);

    sendToAll(Json.object()
        .with("command", "removeEntity")
        .with("id", entityId));
  }

  private Json createAddPlayerJson(Player p) {
    return Json.object()
        .with("command", "addPlayer")
        .with("player", p.toJson());
  }

  private void spawnInRandomLocation(Player player) {
    while (true) {
      int i = random(terrain.width);
      int j = random(terrain.height);
      if (terrain.tiles[i][j].isWalkable()) {
        player.moveToTile(i, j);
        if (!player.isCollision(player.getCollisionRect())) {
          return;
        }
      }
    }
  }

  public void sendToAll(Json json) {
    sendToAll(json, null);
  }

  public void sendToAll(Json json, Player exception) {
    for (Player player : players) {
      if (player != exception) {
        player.send(json);
      }
    }
  }

  public Json toJson() {
    return Json.object()
        .with("terrain", terrain.toJson())
        .with("chests", Json.array(idEntities.values(), Entity::toJson));
  }

}
