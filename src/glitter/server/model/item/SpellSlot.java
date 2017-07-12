package glitter.server.model.item;

import ox.Json;

public class SpellSlot extends Item {

  public SpellSlot() {
    super("+1 Spell Slot", Rarity.COMMON);
  }

  @Override
  public Json toJson() {
    return super.toJson()
        .with("type", "spell-slot")
        .with("imageUrl", "/spell-slot.png");
  }

}
