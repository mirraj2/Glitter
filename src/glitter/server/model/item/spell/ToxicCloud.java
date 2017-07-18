package glitter.server.model.item.spell;

import java.util.List;
import com.google.common.collect.ImmutableList;
import glitter.server.model.Entity;
import glitter.server.model.Player;
import ox.Json;

public class ToxicCloud extends Spell {

  public ToxicCloud() {
    super("Toxic Cloud");
  }

  @Override
  public List<Entity> cast(Player caster, Json locs) {
    return ImmutableList.of();
  }

}
