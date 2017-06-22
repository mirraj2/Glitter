package glitter.server.model;

import java.util.concurrent.atomic.AtomicLong;
import bowser.websocket.ClientSocket;
import ox.Json;

public class Player {

  private static final AtomicLong idCounter = new AtomicLong();

  private final ClientSocket socket;

  public final long id = idCounter.getAndIncrement();

  public double x, y, width = 48, height = 64;

  public Player(ClientSocket socket) {
    this.socket = socket;
  }

  public void send(Json json) {
    try {
      if (json.isArray() && json.isEmpty()) {
        return;
      }
      socket.send(json);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void moveToTile(int i, int j) {
    x = i * Tile.SIZE;
    y = j * Tile.SIZE + Tile.SIZE - height;
  }

}
