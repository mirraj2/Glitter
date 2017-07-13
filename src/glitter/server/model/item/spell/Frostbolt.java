package glitter.server.model.item.spell;

import java.util.List;
import com.google.common.collect.ImmutableList;
import glitter.server.model.Entity;
import glitter.server.model.Player;
import glitter.server.model.Player.Stat;
import glitter.server.model.Projectile;
import glitter.server.model.Tile;
import ox.Json;
import ox.Log;

public class Frostbolt extends Spell {

  private final int minDamage, maxDamage;

  // the projectile's speed in tiles per second
  public final double speed = 9;

  // the number of tiles the projectile will travel
  public final double range = 50;

  public Frostbolt() {
    super("Frostbolt");

    this.manaCost = 15;

    this.minDamage = 16;
    this.maxDamage = 22;
    this.description = String.format("Shoots a shard of ice, dealing %d to %d damage "
        + "and slowing anyone hit for 3 seconds.", minDamage, maxDamage);
  }

  @Override
  public List<Entity> cast(Player caster, Json locs) {
    double speed = this.speed * Tile.SIZE;
    Projectile p = new Projectile(caster.world, locs.getDouble("fromX"), locs.getDouble("fromY"), 12)
        .velocity(locs.getDouble("dx") * speed, locs.getDouble("dy") * speed).life(this.range / this.speed * 1000);

    p.update(caster.latency);

    p.onHit(hit -> {
      if (hit != caster) {
        double damage = minDamage + Math.random() * (maxDamage - minDamage);
        damage *= 1 + caster.stats.get(Stat.FIRE) / 100;

        hit.health = Math.max(0, hit.health - damage);
        Log.info("%s did %s damage to %s with %s", caster, damage, hit, this);
        if (hit.health == 0) {
          Log.info("FATAL HIT!");
          hit.alive = false;
        }
        caster.world.sendToAll(Json.object()
            .with("command", "onHit")
            .with("projectileId", p.id)
            .with("casterId", caster.id)
            .with("targetId", hit.id)
            .with("damage", damage)
            .with("fatal", !hit.alive));
        if (!hit.alive) {
          hit.onDeath();
        }
        return true;
      }
      return false;
    });

    return ImmutableList.of(p);
  }

  @Override
  public Json toJson() {
    return super.toJson()
        .with("speed", speed)
        .with("range", range);
  }


}
