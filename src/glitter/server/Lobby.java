package glitter.server;

import java.util.List;
import java.util.concurrent.TimeUnit;
import com.google.common.collect.ImmutableList;
import bowser.websocket.ClientSocket;
import glitter.server.arch.GRandom;
import glitter.server.arch.SwappingQueue;
import glitter.server.model.Match;
import glitter.server.model.Player;
import glitter.server.model.Terrain;
import glitter.server.model.Tile;
import glitter.server.model.World;
import ox.Json;
import ox.Log;
import ox.Threads;

public class Lobby {

  /**
   * Once this number of players enters the lobby, the countdown timer will begin.
   */
  private static final int MIN_PLAYERS = 2;

  private static final long COUNTDOWN_TIME = TimeUnit.SECONDS.toMillis(60);

  private final World world = createLobbyWorld();

  /**
   * The time that the next game will begin.
   */
  private Long nextGameStartTime;

  /**
   * Set to true if an admin forced a game to start.
   */
  private boolean forceStart = false;

  /**
   * Checks to see if we should spawn off a new game.
   */
  private synchronized void checkStart() {
    if (nextGameStartTime == null || System.currentTimeMillis() < nextGameStartTime) {
      return;
    }

    if (world.players.size() < MIN_PLAYERS && !forceStart) {
      Log.info("Player(s) left before the countdown completed.");
      nextGameStartTime = null;
      world.sendToAll(Json.object()
          .with("command", "countdown")
          .with("millisLeft", -1));
      return;
    }

    Log.info("Starting a new game!");

    List<Player> players = ImmutableList.copyOf(((SwappingQueue<Player>) world.players).swap());

    synchronized (world) {
      for (Player player : players) {
        world.idEntities.remove(player.id);
      }
    }

    nextGameStartTime = null;

    Match.start(players);

    forceStart = false;
  }

  public void startGameIn(int seconds) {
    final int millis = seconds * 1000;
    forceStart = true;
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

  private World createLobbyWorld() {
    // World world = WorldGen.generateFor(2);
    // world.players = new SwappingQueue<Player>();
    // return world;
    Terrain t = new Terrain(18, 10);
    for (int i = 0; i < t.width; i++) {
      for (int j = 0; j < t.height; j++) {
        if (i == 0 || j == 0 || i >= t.width - 1 || j >= t.height - 1) {
          t.tiles[i][j] = Tile.VOID;
        } else {
          t.tiles[i][j] = Tile.GRASS;
        }
      }
    }
    for (int i = 4; i <= 5; i++) {
      for (int j = 3; j <= 5; j++) {
        t.tiles[i][j] = Tile.VOID;
      }
    }
    for (int i = 4; i <= 7; i++) {
      for (int j = 6; j <= 6; j++) {
        t.tiles[i][j] = Tile.VOID;
      }
    }
    t.tiles[6][5] = Tile.VOID;
    t.tiles[4][6] = Tile.GRASS;

    World world = new World(new GRandom(), t, false);
    world.players = new SwappingQueue<>();

    world.onDisconnectCallback = p -> {
      world.removePlayer(p);
    };

    return world;
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
