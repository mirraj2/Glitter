package glitter.server.model;

import java.util.List;
import com.google.common.collect.Lists;

public class Match {

  private final World world;

  private Match(List<Player> players) {
    Terrain terrain = Terrain.createRealWorld();
    this.world = new World(terrain, Lists.newCopyOnWriteArrayList());
    for (Player player : players) {
      this.world.addPlayer(player);
    }
    this.world.start();
  }

  public static void start(List<Player> players) {
    new Match(players);
  }

}
