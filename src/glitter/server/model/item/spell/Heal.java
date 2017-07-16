package glitter.server.model.item.spell;

import java.util.List;
import com.google.common.collect.ImmutableList;
import glitter.server.model.Entity;
import glitter.server.model.Player;
import glitter.server.model.Player.Stat;
import ox.Json;

public class Heal extends Spell {

  private final int minHeal, maxHeal;

  // the number of tiles the projectile will travel
  public final double range = 50;

  public Heal() {
    super("Heal");

    this.manaCost = 28;

    this.minHeal = 25;
    this.maxHeal = 50;
    this.description = String.format("Heal yourself or an ally for %d to %d", minHeal, maxHeal);
  }

  @Override
  public List<Entity> cast(Player caster, Json locs) {
    Player target = caster.world.getEntity(locs.getLong("targetId"));

    double healAmount = minHeal + Math.random() * (maxHeal - minHeal);
    healAmount *= 1 + caster.stats.get(Stat.HOLY) / 100;

    target.health = Math.min(target.health + healAmount, target.getMaxHealth());

    caster.world.sendToAll(Json.object()
        .with("command", "onHit")
        .with("spell", name.toLowerCase())
        .with("casterId", caster.id)
        .with("targetId", target.id)
        .with("damage", -healAmount)
        .with("currentHealth", target.health));

    return ImmutableList.of();
  }

  @Override
  public Json toJson() {
    return super.toJson()
        .with("range", range);
  }

}
