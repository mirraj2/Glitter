package glitter.server.model.item.armor;

import static java.lang.Double.parseDouble;
import static ox.util.Utils.normalize;
import static ox.util.Utils.parseEnum;
import java.util.Map;
import com.google.common.collect.Maps;
import glitter.server.model.Player.Stat;
import glitter.server.model.item.Item;
import ox.Json;

public class Armor extends Item {

  public final Json originalJson;

  public final Part part;
  public final String imageUrl;

  public final Map<Stat, Double> stats = Maps.newLinkedHashMap();

  public Armor(Json json) {
    super(json.get("name"), json.getEnum("rarity", Rarity.class));

    this.originalJson = json;

    this.part = json.getEnum("part", Part.class);
    this.imageUrl = json.get("image");
    this.flavorText = normalize(json.get("flavor"));

    Json statsJson = json.getJson("stats");
    if (statsJson != null) {
      for (String key : statsJson) {
        stats.put(parseEnum(key, Stat.class), parseDouble(statsJson.get(key)));
      }
    }
  }

  @Override
  public Json toJson() {
    Json ret = super.toJson()
        .with("type", "armor")
        .with("part", part)
        .with("imageUrl", imageUrl);
    Json statsJson = Json.array();
    stats.forEach((k, v) -> {
      statsJson.add(Json.object()
          .with("stat", k)
          .with("value", v));
    });
    ret.with("stats", statsJson);
    return ret;
  }

  public static enum Part {
    BAG, CHEST, FEET, HEAD, LEGS, NECK, RING, STAFF;
  }

}
