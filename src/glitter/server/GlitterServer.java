package glitter.server;

import java.io.File;
import bowser.websocket.WebSocketServer;
import glitter.client.WebContentServer;
import ox.Log;
import ox.OS;

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
    Log.logToFolder(new File(OS.getHomeFolder(), "log"));

    new GlitterServer().run();
  }

}
