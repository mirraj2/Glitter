package glitter.server.model;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static ox.util.Functions.toSet;
import static ox.util.Utils.last;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import bowser.websocket.ClientSocket;
import glitter.server.arch.Rect;
import glitter.server.arch.SwappingQueue;
import glitter.server.gen.world.Point;
import glitter.server.logic.PlayerMovement;
import glitter.server.logic.Spells;
import glitter.server.model.item.Item;
import glitter.server.model.status.StatusEffect;
import ox.Json;
import ox.Log;

public class Player extends Entity {

  private static final Set<String> movementKeys = ImmutableSet.of("w", "a", "s", "d");

  public final ClientSocket socket;
  public World world;

  public final Map<Stat, Double> stats = Maps.newConcurrentMap();

  public double health, mana;

  private final SwappingQueue<Json> outboundMessageBuffer = new SwappingQueue<>();

  public Set<String> keys = ImmutableSet.of();

  public final Inventory inventory = new Inventory(this);

  private final Rect hitbox = new Rect(12, 48, 24, 16);

  // used for collision detection
  private final Rect collisionRect = new Rect();

  /**
   * If we are currently looting a chest, this will have the choices this player is choosing between.
   */
  private List<Item> lootChoices = null;

  public boolean alive = true;

  private long lastPingRequestTime = 0;
  public double latency = 0;

  public final PlayerMovement movement = new PlayerMovement(this);

  private final Multimap<String, StatusEffect> statusEffects = LinkedListMultimap.create();

  public Player(ClientSocket socket) {
    super(48, 64);

    for (Stat stat : Stat.values()) {
      this.stats.put(stat, 0d);
    }

    this.stats.put(Stat.HEALTH, 100.0);
    this.stats.put(Stat.MANA, 100.0);
    this.stats.put(Stat.HEALTH_REGEN, 1.0);
    this.stats.put(Stat.MANA_REGEN, 5.0);
    this.stats.put(Stat.MOVEMENT, 0.0);
    this.stats.put(Stat.LUCK, 0.0);

    this.health = getMaxHealth();
    this.mana = getMaxMana();

    this.socket = socket;

    socket.onMessage(this::handleMessage);
  }

  public void addStatusEffect(StatusEffect effect) {
    checkNotNull(effect);

    Log.debug("Adding " + effect + " to " + this);

    Collection<StatusEffect> currentEffects = statusEffects.get(effect.name);
    if (currentEffects.size() >= effect.getMaxStacks()) {
      // Because we've reached the maximum stacks, just refresh the last effect we already have
      last(currentEffects).merge(effect);
    } else {
      currentEffects.add(effect);
      effect.onStart(this);
    }
  }

  /**
   * Called when this player has died. We will explode all of our items around us for other players to loot.
   */
  public void onDeath() {
    Json loot = Json.array();
    Json itemExplosion = Json.object()
        .with("command", "itemExplosion")
        .with("centerX", bounds.centerX())
        .with("centerY", bounds.centerY())
        .with("loot", loot);

    int radius = 3;
    Point p = new Point();
    for (Item item : inventory.getAllItems()) {
      item.owner = null;

      p.x = (int) bounds.centerX();
      p.y = (int) bounds.centerY();

      // choose a random direction
      double angle = world.rand.nextDouble() * Math.PI * 2;
      double power = world.rand.gauss(radius, 1);
      traceProjectile(p, Math.cos(angle), Math.sin(angle), power * Tile.SIZE);
      item.bounds.location(p.x, p.y);

      loot.add(Json.object()
          .with("x", p.x)
          .with("y", p.y)
          .with("item", item.toJson()));

      world.addEntity(item);
    }

    world.sendToAll(itemExplosion);
    inventory.idItemMap.clear();
  }

  private void traceProjectile(Point p, double dx, double dy, double maxDistance) {
    double tickLength = Tile.SIZE / 2;

    for (double t = 0; t <= maxDistance; t += tickLength) {
      p.x += dx * tickLength;
      p.y += dy * tickLength;
      if (!world.terrain.isWalkable(p.x / Tile.SIZE, p.y / Tile.SIZE)) {
        p.x -= dx * tickLength;
        p.y -= dy * tickLength;
        return;
      }
    }
  }

  @Override
  public boolean update(double millis) {
    health = Math.min(getMaxHealth(), health + getStat(Stat.HEALTH_REGEN) * millis / 1000.0);
    mana = Math.min(getMaxMana(), mana + getStat(Stat.MANA_REGEN) * millis / 1000.0);

    // handle status effects
    {
      List<StatusEffect> toRemove = Lists.newArrayListWithCapacity(0);
      for (StatusEffect effect : statusEffects.values()) {
        if (!effect.update(millis)) {
          toRemove.add(effect);
        }
      }

      for (StatusEffect effect : toRemove) {
        Log.debug("Removing " + effect + " from " + this);
        statusEffects.remove(effect.name, effect);
        effect.onEnd(this);
      }
    }

    movement.update(millis);

    return true;
  }

  public Rect getCollisionRect() {
    collisionRect.x = bounds.x + hitbox.x;
    collisionRect.y = bounds.y + hitbox.y;
    collisionRect.w = hitbox.w;
    collisionRect.h = hitbox.h;
    return collisionRect;
  }

  private void chooseLoot(long itemId) {
    synchronized (lootChoices) {
      for (Item item : lootChoices) {
        if (item.id == itemId) {
          inventory.loot(item);
          lootChoices = null;
          return;
        }
      }
      throw new RuntimeException("Invalid item id: " + itemId);
    }
  }

  public void gift(Item item) {
    send(Json.object()
        .with("command", "receiveItem")
        .with("item", item.toJson()));
    inventory.loot(item);
  }

  public void broadcastStats() {
    world.sendToAll(Json.object()
        .with("command", "stats")
        .with("playerId", this.id)
        .with("stats", getStats()));
  }

  private void interact(long entityId) {
    Entity entity = world.idEntities.get(entityId);

    if (entity instanceof TreasureChest) {
      TreasureChest chest = (TreasureChest) world.idEntities.get(entityId);
      chest.open();

      this.lootChoices = world.lootMaster.generateChoices(this);
      send(Json.object()
          .with("command", "choose")
          .with("choices", Json.array(lootChoices, Item::toJson)));

      world.removeEntity(entityId);
    } else if (entity instanceof Item) {
      Item item = (Item) entity;

      if (!inventory.hasSpaceFor(item)) {
        send(Json.object()
            .with("command", "error")
            .with("text", "You don't have any more space in your inventory."));
        return;
      }

      synchronized (item) {
        checkState(item.owner == null, "This item was already picked up!");
        item.owner = this;
      }
      send(Json.object()
          .with("command", "receiveItem")
          .with("item", item.toJson()));
      world.removeEntity(item.id);
      inventory.loot(item);
    } else {
      throw new RuntimeException("Don't know how to interact with " + entity);
    }
  }

  /**
   * TODO these messages are handled whenever they are received, but there is some code here that we might want to run
   * synchronized or on a per-tick basis.
   */
  private void handleMessage(String msg) {
    Json json = new Json(msg);
    String command = json.get("command");

    if (command.equals("pong")) {
      long t1 = json.getLong("time");
      long t2 = System.nanoTime();
      double newLatency = (t2 - t1) / 2.0 / 1_000_000.0;
      latency = latency * .7 + newLatency * .3;
      return;
    }

    if (!alive) {
      Log.debug(json);
      throw new RuntimeException("Can't do that when you're dead!");
    }

    if (command.equals("myState")) {
      bounds.x = json.getDouble("x");
      bounds.y = json.getDouble("y");
      keys = toSet(json.getJson("keys").asStringArray(), s -> s.toLowerCase());
      Set<String> keysToTransmit = Sets.intersection(keys, movementKeys);
      world.sendToAll(Json.object()
          .with("command", "playerState")
          .with("id", id)
          .with("x", bounds.x)
          .with("y", bounds.y)
          .with("keys", Json.array(keysToTransmit)), this);
    } else if (command.equals("cast")) {
      Spells.cast(this, json);
    } else if (command.equals("interact")) {
      long entityId = json.getLong("entityId");
      interact(entityId);
    } else if (command.equals("choose")) {
      chooseLoot(json.getLong("id"));
    } else if (command.equals("swap")) {
      inventory.swapItems(json.getLong("itemA"), json.getLong("itemB"));
    } else if (command.equals("drop")) {
      inventory.dropItem(json.getLong("id"));
    } else if (command.equals("consoleInput")) {
      world.console.handle(this, json.get("text"));
    } else {
      Log.debug(json);
      Log.error("Player.java: Don't know how to handle command: " + command);
    }
  }

  public void send(Json json) {
    if (json.isArray() && json.isEmpty()) {
      return;
    }
    outboundMessageBuffer.add(json);
  }

  public void flushMessages() {
    long now = System.nanoTime();
    int millisToWait = outboundMessageBuffer.isEmpty() ? 500 : 250;
    Json ping = null;
    if (now - lastPingRequestTime > TimeUnit.MILLISECONDS.toNanos(millisToWait)) {
      ping = Json.object()
          .with("command", "ping")
          .with("time", now);
      lastPingRequestTime = now;
    }
    if (ping == null && outboundMessageBuffer.isEmpty()) {
      return;
    }
    try {
      Json array = Json.array();
      if (ping != null) {
        array.add(ping);
      }
      for (Json message : outboundMessageBuffer.swap()) {
        // String command = message.get("command");
        // if (!command.equals("ping") && !command.equals("playerState")) {
        // Log.debug(command);
        // }
        array.add(message);
      }
      socket.send(array);
    } catch (Exception e) {
      if ("Broken pipe".equals(Throwables.getRootCause(e).getMessage())) {
        // ignore
      } else {
        e.printStackTrace();
      }
    }
  }

  public void moveToTile(int i, int j) {
    bounds.x = i * Tile.SIZE;
    bounds.y = j * Tile.SIZE + Tile.SIZE - bounds.h;
  }

  public double getMaxHealth() {
    return getStat(Stat.HEALTH);
  }

  public double getMaxMana() {
    return getStat(Stat.MANA);
  }

  /**
   * In tiles per second.
   */
  public double getMovementSpeed() {
    return 6.0 * (1 + getStat(Stat.MOVEMENT) / 100.0);
  }

  @Override
  public Json toJson() {
    return Json.object()
        .with("id", id)
        .with("x", bounds.x)
        .with("y", bounds.y)
        .with("stats", getStats());
  }

  public double getStat(Stat stat) {
    return stats.get(stat);
  }

  public double getLuck() {
    return stats.get(Stat.LUCK);
  }

  private Json getStats() {
    return Json.object()
        .with("health", health)
        .with("mana", mana)
        .with("maxHealth", getMaxHealth())
        .with("maxMana", getMaxMana())
        .with("healthRegen", getStat(Stat.HEALTH_REGEN))
        .with("manaRegen", getStat(Stat.MANA_REGEN))
        .with("speed", getMovementSpeed())
        .with("luck", getStat(Stat.LUCK));
  }

  @Override
  public String toString() {
    return "Player " + id;
  }

  public static enum Stat {
    HEALTH, MANA, SLOTS, HEALTH_REGEN, MANA_REGEN, MOVEMENT, LUCK, FIRE, ICE, HOLY, UNHOLY;
  }

}
