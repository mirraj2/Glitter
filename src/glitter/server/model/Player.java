package glitter.server.model;

import java.util.concurrent.atomic.AtomicLong;
import bowser.websocket.ClientSocket;
import ox.Json;
import ox.Log;

public class Player {

  private static final AtomicLong idCounter = new AtomicLong();

  public final ClientSocket socket;

  public final long id = idCounter.getAndIncrement();

  public double x, y, width = 48, height = 64;

  public Player(ClientSocket socket) {
    this.socket = socket;

    socket.onMessage(this::handleMessage);
  }

  private void handleMessage(String msg) {
    Json json = new Json(msg);
    String command = json.get("command");
    if (command.equals("foo")) {

    } else {
      Log.error("Don't know how to handle command: " + command);
    }
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
