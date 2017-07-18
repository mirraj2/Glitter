package glitter.server.model.item.spell;

import java.util.List;
import com.google.common.collect.ImmutableList;
import glitter.server.arch.GMath;
import glitter.server.model.Entity;
import glitter.server.model.Player;
import glitter.server.model.Player.Stat;
import glitter.server.model.Projectile;
import glitter.server.model.StatusEffect;
import ox.Json;
import ox.Log;

public class ToxicCloud extends Spell {

  // the projectile's speed in tiles per second
  public final double speed = 30;

  public final int damage = 45;

  public final int duration = 5;

  public ToxicCloud() {
    super("Toxic Cloud");

    this.manaCost = 45;
    this.description = String.format("Target an enemy to send a cloud of toxic gas which surrounds"
        + " them and does %d damage over %d seconds.", damage, duration);
  }

  @Override
  public List<Entity> cast(Player caster, Json locs) {
    Player target = caster.world.getEntity(locs.getLong("targetId"));
    Projectile p = new Projectile(caster.world, locs.getDouble("fromX"), locs.getDouble("fromY"), 6).life(60 * 1000)
        .homeInOn(target, speed);
    p.update(caster.latency);

    p.onHit(hit -> {
      if (hit != target) {
        return false;
      }

      caster.world.sendToAll(Json.object()
          .with("command", "onHit")
          .with("spell", name.toLowerCase().replace(' ', '_'))
          .with("projectileId", p.id)
          .with("targetId", target.id));

      double damage = this.damage;
      damage *= 1 + caster.stats.get(Stat.UNHOLY) / 100;

      hit.addStatusEffect(new Poison(hit, duration * 1000, damage));

      return true;
    });

    return ImmutableList.of(p);
  }

  @Override
  public Json toJson() {
    return super.toJson()
        .with("speed", speed)
        .with("duration", duration);
  }

  public static class Poison extends StatusEffect {

    private Player target;
    private final double damage;
    private final int totalTicks = 5;

    private int tickIndex = 0;

    public Poison(Player target, double durationMillis, double damage) {
      super("Toxic Cloud", durationMillis);
      this.target = target;
      this.damage = damage;
    }

    @Override
    public void onStart(Player player) {
      player.world.sendToAll(Json.object()
          .with("command", "addStatusEffect")
          .with("playerId", player.id)
          .with("name", name));
    }

    @Override
    public void onEnd(Player player) {
      player.world.sendToAll(Json.object()
          .with("command", "removeStatusEffect")
          .with("playerId", player.id)
          .with("name", name));
    }

    private void tick() {
      double damage = this.damage / totalTicks;

      target.health = Math.max(0, target.health - damage);
      if (target.health == 0) {
        Log.info("FATAL HIT!");
        target.alive = false;
      }

      target.world.sendToAll(Json.object()
          .with("command", "onHit")
          .with("targetId", target.id)
          .with("currentHealth", target.health)
          .with("damage", damage)
          .with("fatal", !target.alive));

      if (!target.alive) {
        target.onDeath();
      }
    }

    @Override
    public boolean update(double millis) {
      this.millisLeft = Math.max(0, this.millisLeft - millis);

      int newTickIndex = GMath.floor((1 - millisLeft / durationMillis) * totalTicks);
      for (; tickIndex < newTickIndex; tickIndex++) {
        tick();
      }

      return this.tickIndex < totalTicks;
    }

    @Override
    public void refresh() {
      tickIndex = 0;
      super.refresh();
    }

  }

}
