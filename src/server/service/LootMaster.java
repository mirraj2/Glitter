package server.service;

import static ox.util.Utils.propagate;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import ox.Log;
import server.arch.ClasspathScanner;
import server.arch.GRandom;
import server.model.item.Item;
import server.model.item.Item.Rarity;
import server.model.item.spell.Spell;

public class LootMaster {

  private static final Multimap<Rarity, Spell> raritySpells = ArrayListMultimap.create();
  private static final Map<Class<?>, Constructor<? extends Item>> constructorCache = Maps.newConcurrentMap();

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
    for (int i = 0; i < nChoices; i++) {
      ret.add(generateItem(rarity));
    }

    Log.info("Generated %d %s items :: %s", nChoices, rarity, Joiner.on(", ").join(ret));

    return ret;
  }

  private Item generateItem(Rarity rarity) {
    Spell ret = rand.random(raritySpells.get(rarity));
    return newInstance(ret.getClass(), rand);
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
      return c.newInstance(rand);
    } catch (Exception e) {
      throw propagate(e);
    }
  }

  static {
    Stopwatch watch = Stopwatch.createStarted();
    int numItems = 0;

    // this one is just used to create the root instances. The random numbers don't actually matter for these b/c these
    // instances should never be cast or used in a game.
    GRandom rand = new GRandom(123);

    for (Class<? extends Item> c : ClasspathScanner.findSubclasses(Item.class)) {
      Item item = newInstance(c, rand);
      if (item instanceof Spell) {
        raritySpells.put(item.rarity, (Spell) item);
      }
      numItems++;
    }
    Log.debug("Loaded %d items. (%s)", numItems, watch);
  }

}
