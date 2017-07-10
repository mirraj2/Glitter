package glitter.server.model;

import java.util.function.Predicate;
import glitter.server.arch.Rect;

public class Projectile extends Entity {

  private final World world;

  public double vx, vy;
  public double life; // millis til this projectile expires
  public double radius;

  private Predicate<Player> onHit = player -> false;

  public Projectile(World world, double x, double y, double radius) {
    super(x - radius, y - radius, radius * 2, radius * 2);

    this.world = world;
    this.radius = radius;
  }

  @Override
  public boolean update(double millis) {
    life -= millis;
    this.bounds.x += vx * millis / 1000;
    this.bounds.y += vy * millis / 1000;

    // boolean ret = life > 0;

    boolean finished = false;

    for (Player p : world.getAlivePlayers()) {
      if (p.alive && intersects(p.bounds)) {
        finished |= onHit.test(p);
      }
    }

    return !finished && life > 0;
  }

  private boolean intersects(Rect r) {
    double dx = Math.abs(this.bounds.x - r.x);
    double dy = Math.abs(this.bounds.y - r.y);

    if (dx > r.w / 2 + radius || dy > r.h / 2 + radius) {
      return false;
    }

    if (dx <= r.w / 2 || dy <= r.h / 2) {
      return true;
    }

    double cornerDistanceSq = Math.pow(dx - r.w / 2, 2) + Math.pow(dy - r.h / 2, 2);

    return cornerDistanceSq <= radius * radius;
  }

  public Projectile velocity(double vx, double vy) {
    this.vx = vx;
    this.vy = vy;
    return this;
  }

  public Projectile life(double life) {
    this.life = life;
    return this;
  }

  public Projectile onHit(Predicate<Player> callback) {
    this.onHit = callback;
    return this;
  }

}
