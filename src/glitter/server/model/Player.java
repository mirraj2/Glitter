package glitter.server.model;

import static ox.util.Functions.toSet;
import java.util.List;
import java.util.Set;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import bowser.websocket.ClientSocket;
import glitter.server.model.Terrain.TileLoc;
import ox.Json;
import ox.Log;
import ox.Rect;

public class Player extends Entity {

  private static final Set<String> movementKeys = ImmutableSet.of("w", "a", "s", "d");

  public final ClientSocket socket;
  public World world;

  public double speed = 3;

  private final Json outboundMessageBuffer = Json.array();

  private Set<String> keys = ImmutableSet.of();

  private final Rect hitbox = new Rect(12, 48, 24, 16);

  // used for collision detection
  private final Rect collisionRect = new Rect();

  public Player(ClientSocket socket) {
    super(48, 64);

    this.socket = socket;

    socket.onMessage(this::handleMessage);
  }

  public void update(double t) {
    double distance = speed * Tile.SIZE * t / 1000;

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
  }

  private void move(double dx, double dy) {
    collisionRect.x = bounds.x + dx + hitbox.x;
    collisionRect.y = bounds.y + dy + hitbox.y;
    collisionRect.w = hitbox.w;
    collisionRect.h = hitbox.h;

    if (this.isCollision(collisionRect)) {
      return;
    }

    bounds.x += dx;
    bounds.y += dy;
  }

  private boolean isCollision(Rect r) {
    List<TileLoc> collisions = world.terrain.getTilesIntersecting(r, t -> !world.terrain.isWalkable(t.i, t.j));

    if (!collisions.isEmpty()) {
      return true;
    }

    for (Entity e : world.idEntities.values()) {
      if (r.intersects(e.bounds)) {
        return true;
      }
    }

    return false;
  }

  private void interact(long entityId) {
    TreasureChest chest = (TreasureChest) world.idEntities.get(entityId);
    chest.open();

    world.removeEntity(entityId);
  }

  private void handleMessage(String msg) {
    Json json = new Json(msg);
    String command = json.get("command");
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
    } else if (command.equals("interact")) {
      long entityId = json.getLong("entityId");
      interact(entityId);
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
    if (outboundMessageBuffer.isEmpty()) {
      return;
    }
    try {
      socket.send(outboundMessageBuffer);
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

}
