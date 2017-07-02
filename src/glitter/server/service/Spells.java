package glitter.server.service;

import static com.google.common.base.Preconditions.checkState;
import static ox.util.Functions.map;
import java.util.List;
import glitter.server.model.Entity;
import glitter.server.model.Player;
import glitter.server.model.item.spell.Spell;
import ox.Json;

public class Spells {

  public static void cast(Player p, Json json) {
    Spell spell = p.getSpell(json.getLong("spellId"));

    checkState(p.mana >= spell.manaCost, "Not enough mana! %d vs %d", p.mana, spell.manaCost);
    p.mana -= spell.manaCost;

    List<Entity> entities = spell.cast(p, json.getJson("locs"));
    if (!entities.isEmpty()) {
      p.send(Json.object()
          .with("command", "castEffects")
          .with("castId", json.getLong("castId"))
          .with("entityIds", map(entities, e -> e.id)));
    }

    p.world.addEntities(entities);

    json.remove("spellId")
        .remove("castId")
        .with("spell", spell.toJson());
    json.with("casterId", p.id);
    p.world.sendToAll(json, p);
  }

}
