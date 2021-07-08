package glitter.server.model.item.spell;

import java.util.List;

import com.google.common.collect.ImmutableList;

import glitter.server.model.Entity;
import glitter.server.model.Player;
import glitter.server.model.Player.Stat;
import glitter.server.model.Tile;
import glitter.server.model.status.StunEffect;
import ox.Json;
import ox.Log;

public class PillarOfFlame extends Spell {

  private final int minDamage = 14, maxDamage = 18;
  private final double stunDuration = 1.0;
  private final double pillarRadius = 1.5;
  private final double range = 4.5;
  private final int delay = 1;

  public PillarOfFlame() {
    super("Pillar of Flame");

    this.manaCost = 22;

    this.description = String.format("Target an area to summon a pillar of fire, dealing"
        + " %d to %d damage and stunning all enemies caught for %s seconds", minDamage, maxDamage, stunDuration);
  }

  @Override
  public List<Entity> cast(Player caster, Json locs) {
    Pillar pillar = new Pillar(caster, this);
    pillar.bounds.centerOn(locs.getDouble("toX"), locs.getDouble("toY"));

    return ImmutableList.of(pillar);
  }

  @Override
  public Json toJson() {
    return super.toJson()
        .with("range", range)
        .with("pillarRadius", pillarRadius)
        .with("delay", delay);
  }

  private static class Pillar extends Entity {
    private final Player caster;
    private final PillarOfFlame spell;
    private double millisLeft;

    public Pillar(Player caster, PillarOfFlame spell) {
      this.caster = caster;
      this.spell = spell;
      this.millisLeft = spell.delay * 1000 - caster.latency;
    }

    @Override
    public boolean update(double millis) {
      millisLeft -= millis;
      if (millisLeft <= 0) {
        explode();
        return false;
      }
      return true;
    }

    private void explode() {
      double damage = spell.minDamage + Math.random() * (spell.maxDamage - spell.minDamage);
      damage *= 1 + caster.stats.get(Stat.FIRE) / 100;

      for (Player hit : caster.world.getPlayersInCircle(bounds.centerX(), bounds.centerY(),
          spell.pillarRadius * Tile.SIZE)) {
        if (hit != caster) {
          hit.health = Math.max(0, hit.health - damage);
          Log.info("%s did %s damage to %s with %s", caster, damage, hit, spell);
          if (hit.health == 0) {
            Log.info("FATAL HIT!");
            hit.alive = false;
          }
          caster.world.sendToAll(Json.object()
              .with("command", "onHit")
              .with("casterId", caster.id)
              .with("targetId", hit.id)
              .with("damage", damage)
              .with("currentHealth", hit.health));

          if (hit.alive) {
            hit.addStatusEffect(new StunEffect(spell.stunDuration * 1000));
          } else {
            hit.onDeath();
          }
        }
      }
    }
  }

}
