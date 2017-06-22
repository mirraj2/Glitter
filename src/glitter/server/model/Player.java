package glitter.server.model;

import bowser.websocket.ClientSocket;
import ox.Json;

public class Player {

  private final ClientSocket socket;

  public Player(ClientSocket socket) {
    this.socket = socket;
  }

  public void send(Json json) {
    socket.send(json);
  }

}
