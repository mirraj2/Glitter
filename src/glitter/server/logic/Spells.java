package glitter.server.logic;

import static com.google.common.base.Preconditions.checkState;
import static ox.util.Functions.splice;
import java.util.List;
import glitter.server.model.Entity;
import glitter.server.model.Player;
import glitter.server.model.item.spell.Spell;
import ox.Json;

public class Spells {

  public static void cast(Player p, Json json) {
    checkState(!p.isStunned(), "You can't cast spells when you're stunned.");

    Spell spell = p.inventory.getSpellInActionBar(json.getLong("spellId"));

    checkState(p.mana >= spell.manaCost, "Not enough mana! %s vs %s", p.mana, spell.manaCost);
    p.mana -= spell.manaCost;

    List<Entity> entities = spell.cast(p, json.getJson("locs"));
    Json entityIds = Json.array(entities, e -> e.id);

    if (!entities.isEmpty() && json.hasKey("tempIds")) {
      Json castEffects = Json.object()
          .with("command", "castEffects");

      Json idMapping = Json.array();
      List<Long> tempIds = json.getJson("tempIds").asLongArray();
      splice(tempIds, entities, (tempId, entity) -> {
        idMapping.add(Json.array(tempId, entity.id));
      });
      castEffects.with("idMapping", idMapping);
      p.send(castEffects);
    }

    p.world.addEntities(entities);

    json.remove("spellId")
        .remove("castId")
        .with("spell", spell.toJson());
    json.with("casterId", p.id);
    json.with("casterMana", p.mana);
    json.with("entityIds", entityIds);
    for (Player player : p.world.players) {
      if (player != p) {
        player.send(json.copy()
            .with("latency", p.latency + player.latency));
      }
    }
  }

}
