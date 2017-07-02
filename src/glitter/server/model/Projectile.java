package glitter.server.model;

public class Projectile extends Entity {

  public double vx, vy;
  public double life; // millis til this projectile expires

  public Projectile(double x, double y, double radius) {
    super(x - radius, y - radius, radius * 2, radius * 2);
  }

  @Override
  public boolean update(double millis) {
    life -= millis;
    this.bounds.x += vx * millis / 1000;
    this.bounds.y += vy * millis / 1000;
    return life > 0;
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

}
