package glitter.server.model;

import static ox.util.Functions.toSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import bowser.websocket.ClientSocket;
import ox.Json;
import ox.Log;
import ox.Rect;

public class Player {

  private static final AtomicLong idCounter = new AtomicLong();
  private static final Set<String> movementKeys = ImmutableSet.of("w", "a", "s", "d");

  public final ClientSocket socket;
  public World world;

  public final long id = idCounter.getAndIncrement();

  public double x, y, width = 48, height = 64;

  public double speed = 3;

  private final Json outboundMessageBuffer = Json.array();

  private Set<String> keys = ImmutableSet.of();

  private final Rect hitbox = new Rect(12, 48, 24, 16);

  // used for collision detection
  private final Rect collisionRect = new Rect();

  public Player(ClientSocket socket) {
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

    if (dx != 0) {
      move(dx, 0);
    }
    if (dy != 0) {
      move(0, dy);
    }
  }

  private void move(double dx, double dy) {
    collisionRect.x = x + dx + hitbox.x;
    collisionRect.y = y + dy + hitbox.y;
    collisionRect.w = hitbox.w;
    collisionRect.h = hitbox.h;

    if (this.intersectsBadTerrain(collisionRect)) {
      return;
    }

    x += dx;
    y += dy;
  }

  private boolean intersectsBadTerrain(Rect r) {
    int minI = (int) Math.floor(r.x / Tile.SIZE);
    int minJ = (int) Math.floor(r.y / Tile.SIZE);
    int maxI = (int) Math.floor((r.x + r.w) / Tile.SIZE);
    int maxJ = (int) Math.floor((r.y + r.h) / Tile.SIZE);

    for (int i = minI; i <= maxI; i++) {
      for (int j = minJ; j <= maxJ; j++) {
        if (!world.terrain.isWalkable(i, j)) {
          return true;
        }
      }
    }
    return false;
  }

  private void handleMessage(String msg) {
    Json json = new Json(msg);
    String command = json.get("command");
    if (command.equals("myState")) {
      x = json.getDouble("x");
      y = json.getDouble("y");
      keys = toSet(json.getJson("keys").asStringArray(), s -> s.toLowerCase());
      Set<String> keysToTransmit = Sets.intersection(keys, movementKeys);
      world.sendToAll(Json.object()
          .with("command", "playerState")
          .with("id", id)
          .with("x", x)
          .with("y", y)
          .with("keys", Json.array(keysToTransmit)), this);
    } else {
      Log.error("Don't know how to handle command: " + command);
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
      e.printStackTrace();
    } finally {
      outboundMessageBuffer.clear();
    }
  }

  public void moveToTile(int i, int j) {
    x = i * Tile.SIZE;
    y = j * Tile.SIZE + Tile.SIZE - height;
  }

}
