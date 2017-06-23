package glitter.server;

import java.util.concurrent.TimeUnit;
import bowser.websocket.ClientSocket;
import glitter.server.model.Player;
import glitter.server.model.World;
import ox.Json;

public class Lobby {

  /**
   * Once this number of players enters the lobby, the countdown timer will begin.
   */
  private static final int MIN_PLAYERS = 2;

  private static final long COUNTDOWN_TIME = TimeUnit.SECONDS.toMillis(60);

  private final World world = new World();

  /**
   * The time that the next game will begin.
   */
  private Long nextGameStartTime;

  public void startGameIn3Seconds() {
    final int millis = 3000;
    nextGameStartTime = System.currentTimeMillis() + millis;
    world.sendToAll(Json.object()
        .with("command", "countdown")
        .with("millisLeft", millis));
  }

  public void accept(ClientSocket socket) {
    Player player = new Player(socket);
    world.addPlayer(player);

    if (nextGameStartTime == null) {
      if (world.players.size() >= MIN_PLAYERS) {
        nextGameStartTime = System.currentTimeMillis() + COUNTDOWN_TIME;
        world.sendToAll(Json.object()
            .with("command", "countdown")
            .with("millisLeft", COUNTDOWN_TIME));
      }
    } else {
      player.send(Json.object()
          .with("command", "countdown")
          .with("millisLeft", nextGameStartTime - System.currentTimeMillis()));
    }
  }

  private Lobby() {
    world.start();
  }

  private static final Lobby instance = new Lobby();

  public static Lobby get() {
    return instance;
  }

}
