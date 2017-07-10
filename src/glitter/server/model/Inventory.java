package glitter.server.model;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static ox.util.Utils.last;
import java.util.List;
import java.util.Map;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import glitter.server.model.item.Item;
import glitter.server.model.item.armor.Armor;
import glitter.server.model.item.spell.Spell;
import ox.Json;
import ox.Log;

public class Inventory {

  private final Player player;

  /**
   * This map is used internally to lookup items that this player is holding.
   */
  public final Map<Long, Item> idItemMap = Maps.newConcurrentMap();

  private final Multimap<Armor.Part, Armor> armorMap = Multimaps.synchronizedMultimap(ArrayListMultimap.create());

  private final List<Spell> actionBar = Lists.newArrayListWithCapacity(10);
  private int numSpellSlots = 2;

  private final List<Item> inventory = Lists.newArrayList();
  private int numBagSlots = 6;

  public Inventory(Player player) {
    this.player = player;
  }

  public Iterable<Item> getAllItems() {
    return Iterables.concat(actionBar, armorMap.values(), inventory);
  }

  public Spell getSpellInActionBar(long id) {
    Spell spell = (Spell) idItemMap.get(id);
    checkState(actionBar.contains(spell), "Could not find spell in action bar!");
    return spell;
  }

  public void loot(Item item) {
    Log.info("%s just looted %s", player, item);

    item.owner = player;
    idItemMap.put(item.id, item);

    inventory.add(item);
    autoEquip(item);

    if (inventory.size() > numBagSlots) {
      Log.info("Inventory too full! Dropping item.");
      dropItem(last(inventory).id);
    }
  }

  public boolean hasSpaceFor(Item item) {
    if (inventory.size() < numBagSlots) {
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
        inventory.remove(item);
      }
    } else if (item instanceof Armor) {
      Armor armor = (Armor) item;
      int numEquipped = armorMap.get(armor.part).size();
      int maxEquipped = armor.part == Armor.Part.RING ? 2 : 1;
      if (numEquipped < maxEquipped) {
        equip(armor);
        player.broadcastStats();
      }
    }
  }

  private void equip(Armor armor) {
    checkState(inventory.remove(armor));

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
  }

  public void unequip(Armor armor) {
    checkState(armorMap.remove(armor.part, armor));
    inventory.add(armor);

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

    if (!inventory.remove(item)) {
      if (item instanceof Spell) {
        checkState(actionBar.remove(item));
      } else if (item instanceof Armor) {
        checkState(armorMap.remove(((Armor) item).part, item));
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
        inventory.add(spell);
        if (itemBId != null) {
          Spell toEquip = (Spell) idItemMap.get(itemBId);
          actionBar.add(toEquip);
          checkState(inventory.remove(toEquip));
        }
      } else {
        actionBar.add(spell);
        if (itemBId != null) {
          Spell toUnequip = (Spell) idItemMap.get(itemBId);
          checkState(actionBar.remove(toUnequip));
          inventory.add(toUnequip);
        }
      }
    } else {
      Armor armor = (Armor) item;
      if (armorMap.containsEntry(armor.part, armor)) {
        unequip(armor);
        if (itemBId != null) {
          equip((Armor) idItemMap.get(itemBId));
        }
      } else {
        equip(armor);
        if (itemBId != null) {
          unequip((Armor) idItemMap.get(itemBId));
        }
      }
      player.broadcastStats();
    }
  }

}
