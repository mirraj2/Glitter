package glitter.server.model.item.spell;

import java.util.List;
import com.google.common.collect.ImmutableList;
import glitter.server.model.Entity;
import glitter.server.model.Player;
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
    return ImmutableList.of();
  }

  @Override
  public Json toJson() {
    return super.toJson()
        .with("range", range);
  }

}
