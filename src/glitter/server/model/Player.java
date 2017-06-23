package glitter.server.model;

import static ox.util.Functions.toSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import bowser.websocket.ClientSocket;
import ox.Json;
import ox.Log;

public class Player {

  private static final AtomicLong idCounter = new AtomicLong();
  private static final Set<String> movementKeys = ImmutableSet.of("w", "a", "s", "d");

  public final ClientSocket socket;
  public World world;

  public final long id = idCounter.getAndIncrement();

  public double x, y, width = 48, height = 64;

  private final Json outboundMessageBuffer = Json.array();

  public Player(ClientSocket socket) {
    this.socket = socket;

    socket.onMessage(this::handleMessage);
  }

  private void handleMessage(String msg) {
    Json json = new Json(msg);
    String command = json.get("command");
    if (command.equals("keys")) {
      Set<String> keys = toSet(json.getJson("keys").asStringArray(), s -> s.toLowerCase());
      Set<String> keysToTransmit = Sets.intersection(keys, movementKeys);
      world.sendToAll(Json.object()
          .with("command", "keys")
          .with("playerId", id)
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
