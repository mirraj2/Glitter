package glitter.server.logic;

import java.util.List;

import glitter.server.arch.Rect;
import glitter.server.model.Entity;
import glitter.server.model.Player;
import glitter.server.model.Terrain.TileLoc;
import glitter.server.model.Tile;

public class PlayerMovement {

  private final Player p;

  public PlayerMovement(Player player) {
    this.p = player;
  }

  public void update(double millis) {
    if (p.isStunned()) {
      return;
    }

    double distance = p.getMovementSpeed() * Tile.SIZE * millis / 1000;

    double dx = 0, dy = 0;

    if (p.keys.contains("w")) {
      dy -= distance;
    }
    if (p.keys.contains("a")) {
      dx -= distance;
    }
    if (p.keys.contains("s")) {
      dy += distance;
    }
    if (p.keys.contains("d")) {
      dx += distance;
    }

    if (dx != 0 && dy != 0) {
      dx /= 1.4142135; // divide by sqrt(2)
      dy /= 1.4142135; // divide by sqrt(2)
    }

    if (dx != 0) {
      move(dx, 0);
    }
    if (dy != 0) {
      move(0, dy);
    }
  }

  private void move(double dx, double dy) {
    Rect r = p.getCollisionRect();
    r.x += dx;
    r.y += dy;

    if (isCollision(r)) {
      return;
    }

    p.bounds.x += dx;
    p.bounds.y += dy;
  }

  public boolean isCollision(Rect r) {
    List<TileLoc> collisions = p.world.terrain.getTilesIntersecting(r,
        t -> !p.world.terrain.isWalkable(t.i, t.j));

    if (!collisions.isEmpty()) {
      return true;
    }

    for (Entity e : p.world.idEntities.values()) {
      if (e.blocksWalking() && r.intersects(e.bounds)) {
        return true;
      }
    }

    return false;
  }

}
