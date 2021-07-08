package glitter.server;

import bowser.websocket.WebSocketServer;
import glitter.client.WebContentServer;
import ox.Log;

public class GlitterServer {

  public static final String ip = "playglitter.com";
  public static final int port = 8081;

  private final Lobby lobby = Lobby.get();

  public void run() {
    new WebContentServer().run();
    new WebSocketServer(port)
        .onOpen(socket -> {
          lobby.accept(socket);
        }).start();
  }

  public static void main(String[] args) {
    Log.logToFolder("glitter");

    new GlitterServer().run();
  }

}
