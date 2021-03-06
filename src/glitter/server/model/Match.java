package glitter.server.model;

import static com.google.common.collect.Iterables.filter;
import static ox.util.Utils.only;

import java.util.List;

import com.google.common.collect.ImmutableList;

import glitter.server.gen.WorldGen;
import ox.Log;

public class Match {

  private final World world;

  private Player winner = null;

  private Match(List<Player> players) {
    this.world = WorldGen.generateFor(players.size());
    for (Player player : players) {
      this.world.addPlayer(player);
    }

    this.world.onDeathCallback = this::onDeath;
    this.world.onDisconnectCallback = this::onDisconnect;

    this.world.start();
  }

  private void onDisconnect(Player player) {
    ImmutableList<Player> connectedPlayers = ImmutableList.copyOf(filter(world.getAlivePlayers(), p -> p.connected));

    if (connectedPlayers.isEmpty()) {
      Log.info("All players disconnected, ending match.");
      world.destroy();
    }
  }

  private void onDeath() {
    if (winner != null) {
      return;
    }

    List<Player> alivePlayers = ImmutableList.copyOf(world.getAlivePlayers());

    if (alivePlayers.size() > 1) {
      return;
    }

    winner = only(alivePlayers);

    Log.info("The winner is " + winner + "!");

    world.destroy();
  }

  public static void start(List<Player> players) {
    new Match(players);
  }

}
