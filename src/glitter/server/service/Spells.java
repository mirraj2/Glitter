package glitter.server.service;

import glitter.server.model.Player;
import glitter.server.model.item.spell.Spell;
import ox.Json;

public class Spells {

  public static void cast(Player p, Json json) {
    Spell spell = p.getSpell(json.getLong("spellId"));
    json.remove("spellId")
        .with("spell", spell.toJson());

    json.with("casterId", p.id);
    p.world.sendToAll(json, p);
  }

}
