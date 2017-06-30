package server.model;

import java.util.List;
import server.gen.world.WorldGen;

public class Match {

  private final World world;

  private Match(List<Player> players) {
    this.world = WorldGen.generateFor(players.size());
    for (Player player : players) {
      this.world.addPlayer(player);
    }
    this.world.start();
  }

  public static void start(List<Player> players) {
    new Match(players);
  }

}
