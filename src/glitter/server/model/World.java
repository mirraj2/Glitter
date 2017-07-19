package glitter.server.model;

import static com.google.common.base.Preconditions.checkNotNull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import glitter.server.arch.GMath;
import glitter.server.arch.GRandom;
import glitter.server.logic.LootMaster;
import glitter.server.model.item.Item;
import ox.Config;
import ox.Json;
import ox.Log;

public class World {

  private static final Config config = Config.load("glitter");

  public final GRandom rand;
  public final Terrain terrain;
  public final LootMaster lootMaster;
  public Collection<Player> players = Lists.newCopyOnWriteArrayList();
  public final Map<Long, Entity> idEntities = Maps.newConcurrentMap();
  public final AdminConsole console = new AdminConsole(this);

  private final List<Long> entitiesToRemove = Lists.newArrayList();

  public World(GRandom rand, Terrain terrain) {
    this.rand = rand;
    this.terrain = terrain;
    this.lootMaster = new LootMaster(rand);
  }

  public Iterable<Player> getAlivePlayers() {
    return Iterables.filter(this.players, p -> p.alive);
  }

  public Iterable<Player> getPlayersInCircle(double x, double y, double radius) {
    return Iterables.filter(getAlivePlayers(), p -> {
      return GMath.intersects(x, y, radius, p.bounds);
    });
  }

  public void start() {
    this.sendToAll(Json.object()
        .with("command", "start"));

    if (config.getBoolean("startWithLoot", false)) {
      for (Player player : getAlivePlayers()) {
        for (int i = 0; i < 50; i++) {
          List<Item> items = lootMaster.generateChoices(player);
          Collections.sort(items, (a, b) -> b.rarity.compareTo(a.rarity));
          player.gift(items.get(0));
        }
      }
    }

    new GameLoop(this::update);
  }

  private void update(double millis) {
    for (Entity e : idEntities.values()) {
      if (!e.update(millis)) {
        entitiesToRemove.add(e.id);
      }
    }
    for (Long id : entitiesToRemove) {
      removeEntity(id);
    }
    entitiesToRemove.clear();

    for (Player player : players) {
      player.flushMessages();
    }
  }

  public void addPlayer(Player player) {
    players.add(player);
    idEntities.put(player.id, player);

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

    player.inventory.syncBagToClient();

    for (Player p : players) {
      if (p != player) {
        player.send(createAddPlayerJson(p));
      }
    }
  }

  public void removePlayer(Player player) {
    Log.debug("player disconnected. (%d players in world)", players.size());
    players.remove(player);
    idEntities.remove(player.id);

    sendToAll(Json.object()
        .with("command", "removePlayer")
        .with("id", player.id));
  }

  public void addEntity(Entity entity) {
    idEntities.put(entity.id, entity);
  }

  public void addEntities(Collection<? extends Entity> entities) {
    entities.forEach(this::addEntity);
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
      int i = rand.random(terrain.width);
      int j = rand.random(terrain.height);
      if (terrain.tiles[i][j].isWalkable()) {
        player.moveToTile(i, j);
        if (!player.movement.isCollision(player.getCollisionRect())) {
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

  @SuppressWarnings("unchecked")
  public <T extends Entity> T getEntity(long id) {
    return (T) idEntities.get(id);
  }

  public Json toJson() {
    return Json.object()
        .with("terrain", terrain.toJson())
        .with("chests", Json.array(Iterables.filter(idEntities.values(), TreasureChest.class), Entity::toJson));
  }

}
