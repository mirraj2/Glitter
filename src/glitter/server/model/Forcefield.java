package glitter.server.model;

import java.util.List;

import glitter.server.arch.GMath;
import glitter.server.arch.GRandom;
import glitter.server.arch.Rect;
import glitter.server.model.Terrain.TileLoc;
import ox.Json;
import ox.Log;

public class Forcefield {

  private final World world;
  private final GRandom random;
  private final Terrain terrain;
  public double fromX, fromY, fromRadius, toX, toY, toRadius;
  public double x, y, radius;
  public double transitionSeconds = 60;
  private double t;
  private double millisTilDamage = 0;

  public Forcefield(World world, GRandom random, Terrain terrain) {
    this.world = world;
    this.random = random;
    this.terrain = terrain;

    x = terrain.width / 2.0 * Tile.SIZE;
    y = terrain.height / 2.0 * Tile.SIZE;
    radius = Math.max(terrain.width, terrain.height) / 2.0 * 1.4 * Tile.SIZE;
  }

  public void update(double millis) {
    this.t += millis;
    if (t >= this.transitionSeconds * 1000) {
      x = toX;
      y = toY;
      radius = toRadius;
      moveToRandomLocation();
      return;
    }

    double p = this.t / 1000 / this.transitionSeconds;
    this.x = this.fromX + p * (this.toX - this.fromX);
    this.y = this.fromY + p * (this.toY - this.fromY);
    this.radius = this.fromRadius + p * (this.toRadius - this.fromRadius);

    millisTilDamage -= millis;
    if (millisTilDamage <= 0) {
      damageTick();
      millisTilDamage = 1000;
    }
  }

  private void damageTick() {
    for (Player player : world.getAlivePlayers()) {
      double d = GMath.distSquared(player.bounds.centerX(), player.bounds.centerY(), x, y);
      if (d > radius * radius) {
        player.millisOutsideForcefield += 1000;

        // every 3 seconds spent outside the forcefield increases the damage per tick by 1% max health
        double damagePercent = .01 + player.millisOutsideForcefield / 1000 / 100 / 3;
        double damage = damagePercent * player.getMaxHealth();

        player.health = Math.max(0, player.health - damage);
        Log.info("Forcefield did %s damage to %s", damage, player);
        if (player.health == 0) {
          Log.info("FATAL HIT!");
          player.alive = false;
        }
        world.sendToAll(Json.object()
            .with("command", "onHit")
            .with("targetId", player.id)
            .with("damage", damage)
            .with("currentHealth", player.health));
        if (!player.alive) {
          player.onDeath();
        }
      } else {
        player.millisOutsideForcefield = 0;
      }
    }
  }

  public void moveToRandomLocation() {
    if (radius <= 0) {
      t = 0;
      return;
    }
    Log.info("Moving forcefield to random new location.");

    double x = 0, y = 0;

    // assumes a player can run at most 100 tiles per minute.
    // this formula makes it so that you can outrun the forcefield
    double changeInRadius = 100.0 * Tile.SIZE / 2.0 / Math.sqrt(2);

    double newRadius = Math.max(0, radius - changeInRadius);

    for (int i = 0; i < 100; i++) {
      x = this.x + random.nextInt((int) (radius * 2)) - radius;
      y = this.y + random.nextInt((int) (radius * 2)) - radius;
      if (isValidCircle(x, y, newRadius)) {
        break;
      }
    }

    moveTo(x, y, newRadius);
  }

  private boolean isValidCircle(double x, double y, double radius) {
    Rect r = new Rect(x - radius, y - radius, radius * 2, radius * 2);
    List<TileLoc> locs = terrain.getTilesIntersecting(r, loc -> {
      if (!loc.tile.isWalkable()) {
        return false;
      }
      double d = GMath.distSquared(x, y, loc.i * Tile.SIZE, loc.j * Tile.SIZE);
      return d < radius * radius;
    });
    return !locs.isEmpty();
  }

  private void moveTo(double x, double y, double radius) {
    this.fromX = this.x;
    this.fromY = this.y;
    this.fromRadius = this.radius;
    this.toX = x;
    this.toY = y;
    this.toRadius = radius;
    this.t = 0;

    world.sendToAll(Json.object()
        .with("command", "forcefield")
        .with("forcefield", this.toJson()));
  }

  public Json toJson() {
    return Json.object()
        .with("x", x)
        .with("y", y)
        .with("radius", radius)
        .with("fromX", fromX)
        .with("fromY", fromY)
        .with("fromRadius", fromRadius)
        .with("toX", toX)
        .with("toY", toY)
        .with("toRadius", toRadius)
        .with("transitionSeconds", transitionSeconds);
  }

}
