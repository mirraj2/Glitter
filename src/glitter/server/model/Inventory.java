package glitter.server.model;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static ox.util.Utils.last;
import static ox.util.Utils.only;
import java.util.List;
import java.util.Map;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import glitter.server.model.Player.Stat;
import glitter.server.model.item.Item;
import glitter.server.model.item.SpellSlot;
import glitter.server.model.item.armor.Armor;
import glitter.server.model.item.armor.Armor.Part;
import glitter.server.model.item.spell.Spell;
import ox.Json;
import ox.Log;

public class Inventory {

  private static final int STARTING_BAG_SLOTS = 6;

  private final Player player;

  /**
   * This map is used internally to lookup items that this player is holding.
   */
  public final Map<Long, Item> idItemMap = Maps.newConcurrentMap();

  private final Multimap<Armor.Part, Armor> armorMap = Multimaps.synchronizedMultimap(ArrayListMultimap.create());

  private final List<Spell> actionBar = Lists.newArrayListWithCapacity(10);
  public int numSpellSlots = 2;

  private final List<Item> bagSlots = Lists.newArrayList();
  private int numBagSlots = STARTING_BAG_SLOTS;

  public Inventory(Player player) {
    this.player = player;
  }

  public Iterable<Item> getAllItems() {
    return Iterables.concat(actionBar, armorMap.values(), bagSlots);
  }

  public void syncBagToClient() {
    Armor bag = only(armorMap.get(Part.BAG));

    numBagSlots = STARTING_BAG_SLOTS + (bag == null ? 0 : bag.stats.get(Stat.SLOTS).intValue());

    while (bagSlots.size() > numBagSlots) {
      dropItem(last(bagSlots).id);
    }

    player.send(Json.object()
        .with("command", "bagUpdate")
        .with("numSlots", numBagSlots)
        .with("items", Json.array(bagSlots, Item::toJson)));
  }

  public Spell getSpellInActionBar(long id) {
    Spell spell = (Spell) idItemMap.get(id);
    checkState(actionBar.contains(spell), "Could not find spell in action bar!");
    return spell;
  }

  public void loot(Item item) {
    Log.info("%s just looted %s", player, item);
    
    if(item instanceof SpellSlot){
      idItemMap.remove(item.id);
      numSpellSlots = Math.min(numSpellSlots + 1, 10);
      return;
    }

    item.owner = player;
    idItemMap.put(item.id, item);

    bagSlots.add(item);
    autoEquip(item);

    if (bagSlots.size() > numBagSlots) {
      Log.info("Inventory too full! Dropping " + item);
      dropItem(last(bagSlots).id);
    }
  }

  public boolean hasSpaceFor(Item item) {
    if (bagSlots.size() < numBagSlots) {
      return true;
    }
    if (item instanceof Spell) {
      if (actionBar.size() < numSpellSlots) {
        return true;
      }
    } else if (item instanceof Armor) {
      Armor armor = (Armor) item;
      int numEquipped = armorMap.get(armor.part).size();
      int maxEquipped = armor.part == Armor.Part.RING ? 2 : 1;
      if (numEquipped < maxEquipped) {
        return true;
      }
    }
    return false;
  }

  private void autoEquip(Item item) {
    if (item instanceof Spell) {
      if (actionBar.size() < numSpellSlots) {
        actionBar.add((Spell) item);
        bagSlots.remove(item);
      }
    } else if (item instanceof Armor) {
      Armor armor = (Armor) item;
      int numEquipped = armorMap.get(armor.part).size();
      int maxEquipped = armor.part == Armor.Part.RING ? 2 : 1;
      if (numEquipped < maxEquipped) {
        equip(armor, true);
        player.broadcastStats();
      }
    }
  }

  private void equip(Armor armor, boolean handleBagChanges) {
    checkState(bagSlots.remove(armor));

    armorMap.put(armor.part, armor);

    double pHealth = player.health / player.getMaxHealth();
    double pMana = player.mana / player.getMaxMana();

    // add stats from the armor we're putting on
    armor.stats.forEach((k, v) -> {
      player.stats.compute(k, (stat, value) -> {
        return value + v;
      });
    });

    // adjust our current health and mana to be the same percentage as they were before
    player.health = player.getMaxHealth() * pHealth;
    player.mana = player.getMaxMana() * pMana;

    if (handleBagChanges && armor.part == Armor.Part.BAG) {
      syncBagToClient();
    }
  }

  private void unequip(Armor armor) {
    checkState(armorMap.remove(armor.part, armor));
    bagSlots.add(armor);

    double pHealth = player.health / player.getMaxHealth();
    double pMana = player.mana / player.getMaxMana();

    // remove stats from the armor we're taking off.
    armor.stats.forEach((k, v) -> {
      player.stats.compute(k, (stat, value) -> {
        return value - v;
      });
    });

    // adjust our current health and mana to be the same percentage as they were before
    player.health = player.getMaxHealth() * pHealth;
    player.mana = player.getMaxMana() * pMana;
  }

  public void dropItem(long itemId) {
    Item item = idItemMap.remove(itemId);
    checkNotNull(item);

    if (!bagSlots.remove(item)) {
      if (item instanceof Spell) {
        checkState(actionBar.remove(item));
      } else if (item instanceof Armor) {
        unequip((Armor) item);
        checkState(bagSlots.remove(item));

        if (((Armor) item).part == Armor.Part.BAG) {
          syncBagToClient();
        }
        player.broadcastStats();
      }
    }

    item.owner = null;
    item.bounds.centerOn(player.bounds.centerX(), player.bounds.centerY());
    player.world.addEntity(item);

    player.world.sendToAll(Json.object()
        .with("command", "itemDropped")
        .with("x", item.bounds.centerX())
        .with("y", item.bounds.centerY())
        .with("item", item.toJson()));
  }

  public void swapItems(Long itemAId, Long itemBId) {
    checkNotNull(itemAId);

    Item item = idItemMap.get(itemAId);
    checkNotNull(item, "Could not find item with id: " + itemAId);

    if (item instanceof Spell) {
      Spell spell = (Spell) item;

      if (actionBar.remove(spell)) {
        bagSlots.add(spell);
        if (itemBId != null) {
          Spell toEquip = (Spell) idItemMap.get(itemBId);
          actionBar.add(toEquip);
          checkState(bagSlots.remove(toEquip));
        }
      } else {
        actionBar.add(spell);
        if (itemBId != null) {
          Spell toUnequip = (Spell) idItemMap.get(itemBId);
          checkState(actionBar.remove(toUnequip));
          bagSlots.add(toUnequip);
        }
      }
    } else {
      Armor armor = (Armor) item;
      if (armorMap.containsEntry(armor.part, armor)) {
        unequip(armor);
        if (itemBId != null) {
          equip((Armor) idItemMap.get(itemBId), false);
        }
      } else {
        equip(armor, false);
        if (itemBId != null) {
          unequip((Armor) idItemMap.get(itemBId));
        }
      }
      if (armor.part == Armor.Part.BAG) {
        syncBagToClient();
      }
      player.broadcastStats();
    }
  }

}
