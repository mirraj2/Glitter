package glitter.server.model;

import java.util.List;
import com.google.common.collect.Lists;
import glitter.server.gen.terrain.TerrainGen;

public class Match {

  private final World world;

  private Match(List<Player> players) {
    Terrain terrain = TerrainGen.generateFor(players.size());
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
