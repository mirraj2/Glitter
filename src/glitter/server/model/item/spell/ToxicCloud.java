package glitter.server.model.item.spell;

import java.util.List;
import com.google.common.collect.ImmutableList;
import glitter.server.model.Entity;
import glitter.server.model.Player;
import glitter.server.model.Projectile;
import glitter.server.model.StatusEffect;
import ox.Json;

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

      hit.addStatusEffect(new Poison(duration * 1000));

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

    public Poison(double durationMillis) {
      super("Toxic Cloud", durationMillis);
    }

    @Override
    public void onStart(Player player) {
      player.world.sendToAll(Json.object()
          .with("command", "addStatusEffect")
          .with("playerId", player.id)
          .with("name", name));
    }

  }

}
