package glitter.server.logic;

import static ox.util.Utils.propagate;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import glitter.server.arch.GRandom;
import glitter.server.model.item.Item;
import glitter.server.model.item.Item.Rarity;
import glitter.server.model.item.armor.Armor;
import glitter.server.model.item.spell.Fireball;
import glitter.server.model.item.spell.Spell;
import ox.IO;
import ox.Json;
import ox.Log;

public class LootMaster {

  private static final Map<Class<?>, Constructor<? extends Item>> constructorCache = Maps.newConcurrentMap();

  private static final Multimap<Rarity, Spell> raritySpells;
  private static final Multimap<Rarity, Armor> rarityArmors;

  private final GRandom rand;

  public LootMaster(GRandom rand) {
    this.rand = rand;
  }

  public List<Item> generateChoices() {
    Rarity rarity = randomRarity();
    rarity = Rarity.COMMON; // right now we only have common items

    int nChoices = 1;
    if (rarity == Rarity.COMMON) {
      nChoices = 3;
    } else if (rarity == Rarity.RARE) {
      nChoices = 2;
    }

    List<Item> ret = Lists.newArrayListWithCapacity(nChoices);
    while (ret.size() < nChoices) {
      Item item = generateItem(rarity);
      if (!ret.contains(item)) {
        ret.add(item);
      }
    }

    Log.info("Generated %d %s items :: %s", nChoices, rarity, Joiner.on(", ").join(ret));

    return ret;
  }

  public Item generateItem(Rarity rarity) {
    if (rand.nextBoolean()) {
      Spell ret = rand.random(raritySpells.get(rarity));
      return newInstance(ret.getClass(), rand);
    } else {
      Armor ret = rand.random(rarityArmors.get(rarity));
      return ret.copy();
    }
  }

  private Rarity randomRarity() {
    double d = rand.nextDouble();
    if (d < .001) {
      return Rarity.LEGENDARY;
    } else if (d < .01) {
      return Rarity.EPIC;
    } else if (d < .1) {
      return Rarity.RARE;
    } else {
      return Rarity.COMMON;
    }
  }

  private static Item newInstance(Class<? extends Item> itemClass, GRandom rand) {
    try {
      Constructor<? extends Item> c = constructorCache.computeIfAbsent(itemClass, i -> {
        try {
          return itemClass.getConstructor(GRandom.class);
        } catch (Exception e) {
          try {
            return itemClass.getConstructor();
          } catch (Exception e2) {
            throw propagate(e2);
          }
        }
      });
      if (c.getParameterCount() == 0) {
        return c.newInstance();
      } else {
        return c.newInstance(rand);
      }
    } catch (Exception e) {
      throw propagate(e);
    }
  }

  static {
    List<Spell> spells = Lists.newArrayList(new Fireball());
    List<Armor> armors = Lists.newArrayList();

    for (Json j : IO.from(Armor.class, "armor.json").toJson().asJsonArray()) {
      armors.add(new Armor(j));
    }

    raritySpells = Multimaps.index(spells, s -> s.rarity);
    rarityArmors = Multimaps.index(armors, a -> a.rarity);
  }

}
