package glitter.server.model;

import java.util.function.Predicate;
import glitter.server.arch.Rect;

public class Projectile extends Entity {

  private final World world;

  public double vx, vy;
  public double life; // millis til this projectile expires
  public double radius;

  private Predicate<Player> onHit = player -> false;

  /**
   * If non-null, this projectile will home towards the target. Homing projectiles ignore 'vx' and 'vy'.
   */
  private Player homingTarget = null;

  private double homingSpeed; // in tiles per second

  public Projectile(World world, double x, double y, double radius) {
    super(x - radius, y - radius, radius * 2, radius * 2);

    this.world = world;
    this.radius = radius;
  }

  @Override
  public boolean update(double millis) {
    life -= millis;

    if (homingTarget == null) {
      this.bounds.x += vx * millis / 1000;
      this.bounds.y += vy * millis / 1000;
    } else {
      double dx = homingTarget.bounds.centerX() - this.bounds.x;
      double dy = homingTarget.bounds.centerY() - this.bounds.y;

      double speed = this.homingSpeed * Tile.SIZE * millis / 1000;

      double norm = speed / Math.sqrt(dx * dx + dy * dy);
      dx *= norm;
      dy *= norm;
      
      bounds.x += dx;
      bounds.y += dy;
    }

    boolean finished = false;

    for (Player p : world.getAlivePlayers()) {
      if (p.alive && intersects(p.bounds)) {
        finished |= onHit.test(p);
      }
    }

    return !finished && life > 0;
  }

  private boolean intersects(Rect r) {
    double cx = this.bounds.centerX();
    double cy = this.bounds.centerY();
    
    double closestX = cx < r.x ? r.x : (cx > r.maxX() ? r.maxX() : cx);
    double closestY = cy < r.y ? r.y : (cy > r.maxY() ? r.maxY() : cy);

    double dx = closestX - cx;
    double dy = closestY - cy;

    return (dx * dx + dy * dy) <= radius * radius;
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

  public Projectile homeInOn(Player target, double homingSpeed) {
    this.homingTarget = target;
    this.homingSpeed = homingSpeed;
    return this;
  }

  public Projectile onHit(Predicate<Player> callback) {
    this.onHit = callback;
    return this;
  }

}
