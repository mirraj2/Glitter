package glitter.server.model;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static ox.util.Functions.toSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import com.google.common.base.Throwables;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import bowser.websocket.ClientSocket;
import glitter.server.arch.SwappingQueue;
import glitter.server.gen.world.Point;
import glitter.server.logic.PlayerMovement;
import glitter.server.logic.Spells;
import glitter.server.model.item.Item;
import glitter.server.model.item.armor.Armor;
import glitter.server.model.item.spell.Spell;
import ox.Json;
import ox.Log;
import ox.Rect;

public class Player extends Entity {

  private static final double BASE_HEALTH = 100, BASE_MANA = 100;

  private static final Set<String> movementKeys = ImmutableSet.of("w", "a", "s", "d");

  public final ClientSocket socket;
  public World world;

  public double speed = 3;

  public final Map<Stat, Double> stats = Maps.newConcurrentMap();

  public double health, mana;
  public double healthRegenPerSecond = 1, manaRegenPerSecond = 5;

  private final SwappingQueue<Json> outboundMessageBuffer = new SwappingQueue<>();

  public Set<String> keys = ImmutableSet.of();

  /**
   * This map is used internally to lookup items that this player is holding.
   */
  private final Map<Long, Item> idItemMap = Maps.newConcurrentMap();

  private final Multimap<Armor.Part, Armor> armorMap = Multimaps.synchronizedMultimap(ArrayListMultimap.create());

  private final List<Spell> actionBar = Lists.newArrayListWithCapacity(10);
  private int numSpellSlots = 2;

  private final List<Item> inventory = Lists.newArrayList();

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

  public Player(ClientSocket socket) {
    super(48, 64);

    for (Stat stat : Stat.values()) {
      this.stats.put(stat, 0d);
    }

    this.stats.put(Stat.HEALTH, BASE_HEALTH);
    this.stats.put(Stat.MANA, BASE_MANA);
    this.health = BASE_HEALTH;
    this.mana = BASE_MANA;

    this.socket = socket;

    socket.onMessage(this::handleMessage);
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
    for (Item item : getAllItems()) {
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
    this.idItemMap.clear();
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

  private Iterable<Item> getAllItems() {
    return Iterables.concat(actionBar, armorMap.values(), inventory);
  }

  public Spell getSpell(long id) {
    for (int i = 0; i < numSpellSlots; i++) {
      Spell spell = actionBar.get(i);
      if (spell.id == id) {
        return spell;
      }
    }
    return null;
  }

  @Override
  public boolean update(double millis) {
    return movement.update(millis);
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
          loot(item);
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
    loot(item);
  }

  private void loot(Item item) {
    Log.info("%s just looted %s", this, item);

    item.owner = this;
    this.idItemMap.put(item.id, item);

    inventory.add(item);
    autoEquip(item);
  }

  private void autoEquip(Item item) {
    if (item instanceof Spell) {
      if (actionBar.size() < numSpellSlots) {
        actionBar.add((Spell) item);
        inventory.remove(item);
      }
    } else if (item instanceof Armor) {
      Armor armor = (Armor) item;
      int numEquipped = armorMap.get(armor.part).size();
      int maxEquipped = armor.part == Armor.Part.RING ? 2 : 1;
      if (numEquipped < maxEquipped) {
        equip(armor);
        broadcastStats();
      }
    }
  }

  private void equip(Armor armor) {
    checkState(inventory.remove(armor));

    armorMap.put(armor.part, armor);

    double pHealth = this.health / this.getMaxHealth();
    double pMana = this.mana / this.getMaxMana();

    // add stats from the armor we're putting on
    armor.stats.forEach((k, v) -> {
      this.stats.compute(k, (stat, value) -> {
        return value + v;
      });
    });

    // adjust our current health and mana to be the same percentage as they were before
    this.health = this.getMaxHealth() * pHealth;
    this.mana = this.getMaxMana() * pMana;
  }

  private void unequip(Armor armor) {
    checkState(armorMap.remove(armor.part, armor));
    inventory.add(armor);

    double pHealth = this.health / this.getMaxHealth();
    double pMana = this.mana / this.getMaxMana();

    // remove stats from the armor we're taking off.
    armor.stats.forEach((k, v) -> {
      this.stats.compute(k, (stat, value) -> {
        return value - v;
      });
    });

    // adjust our current health and mana to be the same percentage as they were before
    this.health = this.getMaxHealth() * pHealth;
    this.mana = this.getMaxMana() * pMana;
  }

  private void broadcastStats() {
    send(Json.object()
        .with("command", "stats")
        .with("health", health)
        .with("mana", mana)
        .with("maxHealth", getMaxHealth())
        .with("maxMana", getMaxMana()));
  }

  private void swapItems(Long itemAId, Long itemBId) {
    checkNotNull(itemAId);

    Item item = idItemMap.get(itemAId);
    checkNotNull(item, "Could not find item with id: " + itemAId);

    if (item instanceof Spell) {
      Spell spell = (Spell) item;

      if (actionBar.remove(spell)) {
        inventory.add(spell);
        if (itemBId != null) {
          Spell toEquip = (Spell) idItemMap.get(itemBId);
          actionBar.add(toEquip);
          checkState(inventory.remove(toEquip));
        }
      } else {
        actionBar.add(spell);
        if (itemBId != null) {
          Spell toUnequip = (Spell) idItemMap.get(itemBId);
          checkState(actionBar.remove(toUnequip));
          inventory.add(toUnequip);
        }
      }
    } else {
      Armor armor = (Armor) item;
      if (armorMap.containsEntry(armor.part, armor)) {
        unequip(armor);
        if (itemBId != null) {
          equip((Armor) idItemMap.get(itemBId));
        }
      } else {
        equip(armor);
        if (itemBId != null) {
          unequip((Armor) idItemMap.get(itemBId));
        }
      }
      broadcastStats();
    }
  }

  private void interact(long entityId) {
    Entity entity = world.idEntities.get(entityId);

    if (entity instanceof TreasureChest) {
      TreasureChest chest = (TreasureChest) world.idEntities.get(entityId);
      chest.open();

      this.lootChoices = world.lootMaster.generateChoices();
      send(Json.object()
          .with("command", "choose")
          .with("choices", Json.array(lootChoices, Item::toJson)));

      world.removeEntity(entityId);
    } else if (entity instanceof Item) {
      Item item = (Item) entity;
      checkState(item.owner == null, "This item was already picked up!");
      send(Json.object()
          .with("command", "receiveItem")
          .with("item", item.toJson()));
      this.loot(item);
      world.removeEntity(item.id);
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
      swapItems(json.getLong("itemA"), json.getLong("itemB"));
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
        String command = message.get("command");
        if (!command.equals("ping") && !command.equals("playerState")) {
          Log.debug(command);
        }
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
    return stats.get(Stat.HEALTH);
  }

  public double getMaxMana() {
    return stats.get(Stat.MANA);
  }

  @Override
  public Json toJson() {
    return Json.object()
        .with("id", id)
        .with("x", bounds.x)
        .with("y", bounds.y)
        .with("health", health)
        .with("mana", mana)
        .with("maxHealth", getMaxHealth())
        .with("maxMana", getMaxMana())
        .with("healthRegen", healthRegenPerSecond)
        .with("manaRegen", manaRegenPerSecond);
  }

  @Override
  public String toString() {
    return "Player " + id;
  }

  public static enum Stat {
    HEALTH, MANA, FIRE, ICE, HOLY, UNHOLY;
  }

}
