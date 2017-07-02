package glitter.server;

import java.util.List;
import java.util.concurrent.TimeUnit;
import com.google.common.collect.ImmutableList;
import bowser.websocket.ClientSocket;
import glitter.server.arch.SwappingQueue;
import glitter.server.gen.world.WorldGen;
import glitter.server.model.Match;
import glitter.server.model.Player;
import glitter.server.model.World;
import ox.Json;
import ox.Log;
import ox.Threads;

public class Lobby {

  /**
   * Once this number of players enters the lobby, the countdown timer will begin.
   */
  private static final int MIN_PLAYERS = 4;

  private static final long COUNTDOWN_TIME = TimeUnit.SECONDS.toMillis(60);

  private final World world = createLobbyWorld();

  /**
   * The time that the next game will begin.
   */
  private Long nextGameStartTime;

  /**
   * Checks to see if we should spawn off a new game.
   */
  private synchronized void checkStart() {
    if (nextGameStartTime == null || System.currentTimeMillis() < nextGameStartTime) {
      return;
    }

    Log.info("Starting a new game!");

    List<Player> players = ImmutableList.copyOf(((SwappingQueue<Player>) world.players).swap());
    nextGameStartTime = null;

    Match.start(players);
  }

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
    socket.onClose(() -> {
      world.removePlayer(player);
    });

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

  private World createLobbyWorld() {
    World world = WorldGen.generateFor(2);
    world.players = new SwappingQueue<Player>();
    return world;
    // Terrain ret = new Terrain(16, 8);
    // for (int i = 0; i < ret.width; i++) {
    // for (int j = 0; j < ret.height; j++) {
    // ret.tiles[i][j] = Tile.GRASS;
    // }
    // }
    // for (int i = 3; i <= 4; i++) {
    // for (int j = 2; j <= 4; j++) {
    // ret.tiles[i][j] = Tile.WATER;
    // }
    // }
    // for (int i = 3; i <= 6; i++) {
    // for (int j = 5; j <= 5; j++) {
    // ret.tiles[i][j] = Tile.WATER;
    // }
    // }
    // ret.tiles[5][4] = Tile.WATER;
    // ret.tiles[3][5] = Tile.GRASS;
    // return ret;
  }

  private Lobby() {
    world.start();

    Threads.every(100, TimeUnit.MILLISECONDS).run(this::checkStart);
  }

  private static final Lobby instance = new Lobby();

  public static Lobby get() {
    return instance;
  }

}
