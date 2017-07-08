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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import bowser.websocket.ClientSocket;
import glitter.server.arch.SwappingQueue;
import glitter.server.model.Terrain.TileLoc;
import glitter.server.model.item.Item;
import glitter.server.model.item.armor.Armor;
import glitter.server.model.item.spell.Spell;
import glitter.server.service.Spells;
import ox.Json;
import ox.Log;
import ox.Rect;

public class Player extends Entity {

  private static final double BASE_HEALTH = 100, BASE_MANA = 100;

  private static final Set<String> movementKeys = ImmutableSet.of("w", "a", "s", "d");

  private final Map<Long, Item> idItemHash = Maps.newConcurrentMap();

  public final ClientSocket socket;
  public World world;

  public double speed = 3;

  private final Map<Stat, Double> stats = Maps.newConcurrentMap();

  public double health, mana;
  public double healthRegenPerSecond = 1, manaRegenPerSecond = 5;

  private final SwappingQueue<Json> outboundMessageBuffer = new SwappingQueue<>();

  private Set<String> keys = ImmutableSet.of();

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
    health = Math.min(getMaxHealth(), health + healthRegenPerSecond * millis / 1000.0);
    mana = Math.min(getMaxMana(), mana + manaRegenPerSecond * millis / 1000.0);

    double distance = speed * Tile.SIZE * millis / 1000;

    double dx = 0, dy = 0;

    if (keys.contains("w")) {
      dy -= distance;
    }
    if (keys.contains("a")) {
      dx -= distance;
    }
    if (keys.contains("s")) {
      dy += distance;
    }
    if (keys.contains("d")) {
      dx += distance;
    }

    if (dx != 0 && dy != 0) {
      dx /= 1.4142135; // divide by sqrt(2)
      dy /= 1.4142135; // divide by sqrt(2)
    }

    if (dx != 0) {
      move(dx, 0);
    }
    if (dy != 0) {
      move(0, dy);
    }

    return false;
  }

  private void move(double dx, double dy) {
    Rect r = getCollisionRect();
    r.x += dx;
    r.y += dy;

    if (this.isCollision(r)) {
      return;
    }

    bounds.x += dx;
    bounds.y += dy;
  }

  public Rect getCollisionRect() {
    collisionRect.x = bounds.x + hitbox.x;
    collisionRect.y = bounds.y + hitbox.y;
    collisionRect.w = hitbox.w;
    collisionRect.h = hitbox.h;
    return collisionRect;
  }

  public boolean isCollision(Rect r) {
    List<TileLoc> collisions = world.terrain.getTilesIntersecting(r, t -> !world.terrain.isWalkable(t.i, t.j));

    if (!collisions.isEmpty()) {
      return true;
    }

    for (Entity e : world.idEntities.values()) {
      if (e.blocksWalking() && r.intersects(e.bounds)) {
        return true;
      }
    }

    return false;
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

  private void loot(Item item) {
    Log.info("%s just looted %s", this, item);

    idItemHash.put(item.id, item);

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

    Item item = idItemHash.get(itemAId);
    if (item instanceof Spell) {
      Spell spell = (Spell) item;

      if (actionBar.remove(spell)) {
        inventory.add(spell);
        if (itemBId != null) {
          Spell toEquip = (Spell) idItemHash.get(itemBId);
          actionBar.add(toEquip);
          checkState(inventory.remove(idItemHash.get(itemBId)));
        }
      } else {
        actionBar.add(spell);
        if (itemBId != null) {
          Spell toUnequip = (Spell) idItemHash.get(itemBId);
          checkState(actionBar.remove(toUnequip));
          inventory.add(toUnequip);
        }
      }
    } else {
      Armor armor = (Armor) item;
      if (armorMap.containsEntry(armor.part, armor)) {
        unequip(armor);
        if (itemBId != null) {
          equip((Armor) idItemHash.get(itemBId));
        }
      } else {
        equip(armor);
        if (itemBId != null) {
          unequip((Armor) idItemHash.get(itemBId));
        }
      }
      broadcastStats();
    }
  }

  private void interact(long entityId) {
    TreasureChest chest = (TreasureChest) world.idEntities.get(entityId);
    chest.open();

    this.lootChoices = world.lootMaster.generateChoices();
    send(Json.object()
        .with("command", "choose")
        .with("choices", Json.array(lootChoices, Item::toJson)));

    world.removeEntity(entityId);
  }

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
        array.add(message);
      }
      int n = ping == null ? array.size() : array.size() - 1;
      if (n > 0) {
        Log.debug("sending %d outbound messages", n);
      }
      socket.send(array);
    } catch (Exception e) {
      if ("Broken pipe".equals(Throwables.getRootCause(e).getMessage())) {
        // ignore
      } else {
        e.printStackTrace();
      }
    } finally {
      outboundMessageBuffer.clear();
    }
  }

  public void moveToTile(int i, int j) {
    bounds.x = i * Tile.SIZE;
    bounds.y = j * Tile.SIZE + Tile.SIZE - bounds.h;
  }

  private double getMaxHealth() {
    return stats.get(Stat.HEALTH);
  }

  private double getMaxMana() {
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
