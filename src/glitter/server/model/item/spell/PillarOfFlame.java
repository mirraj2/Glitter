package glitter.server.model.item.spell;

import java.util.List;
import com.google.common.collect.ImmutableList;
import glitter.server.model.Entity;
import glitter.server.model.Player;
import ox.Json;

public class PillarOfFlame extends Spell {

  private final int minDamage = 14, maxDamage = 18;
  private final double stunDuration = 1.2;
  private final int radius = 120;

  public PillarOfFlame() {
    super("Pillar of Flame");

    this.manaCost = 22;

    this.description = String.format("Target an area to summon a pillar of fire, dealing"
        + " %d to %d damage and stunning all enemies caught for %s seconds.", minDamage, maxDamage, stunDuration);
  }

  @Override
  public List<Entity> cast(Player caster, Json locs) {
    return ImmutableList.of();
  }

}
