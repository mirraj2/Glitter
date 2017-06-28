package glitter.server.service;

import static ox.util.Utils.propagate;
import java.lang.reflect.Constructor;
import java.util.List;
import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import glitter.server.arch.ClasspathScanner;
import glitter.server.arch.GRandom;
import glitter.server.model.item.Item;
import glitter.server.model.item.Item.Rarity;
import glitter.server.model.item.spell.Spell;
import ox.Log;

public class LootMaster {

  private static final Multimap<Rarity, Spell> raritySpells = ArrayListMultimap.create();

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
    return newInstance(ret.getClass());
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

  private static Item newInstance(Class<? extends Item> itemClass) {
    try {
      Constructor<? extends Item> c = itemClass.getConstructor();
      return c.newInstance();
    } catch (Exception e) {
      throw propagate(e);
    }
  }

  static {
    Stopwatch watch = Stopwatch.createStarted();
    int numItems = 0;
    for (Class<? extends Item> c : ClasspathScanner.findSubclasses(Item.class)) {
      Item item = newInstance(c);
      if (item instanceof Spell) {
        raritySpells.put(item.rarity, (Spell) item);
      }
      numItems++;
    }
    Log.debug("Loaded %d items. (%s)", numItems, watch);
  }

}
