package glitter.server.model.item.spell;

import java.util.List;
import com.google.common.collect.ImmutableList;
import glitter.server.arch.GRandom;
import glitter.server.model.Entity;
import glitter.server.model.Player;
import glitter.server.model.Projectile;
import glitter.server.model.Tile;
import ox.Json;

public class Fireball extends Spell {

  private final int minDamage, maxDamage;

  // the projectile's speed in tiles per second
  public final double speed = 15;

  // the number of tiles the projectile will travel
  public final double range = 50;

  public Fireball(GRandom rand) {
    super("Fireball");

    this.manaCost = 20;
    this.castTimeSeconds = 1;

    this.minDamage = 18;
    this.maxDamage = 30;
    this.description = String.format("Hurls a ball of fire, dealing %d to %d damage.", minDamage, maxDamage);
  }

  @Override
  public List<Entity> cast(Player caster, Json locs) {
    double speed = this.speed * Tile.SIZE;
    Projectile p = new Projectile(locs.getDouble("fromX"), locs.getDouble("fromY"), Tile.SIZE / 2)
        .velocity(locs.getDouble("dx") * speed, locs.getDouble("dy") * speed).life(this.range / this.speed * 1000);
    return ImmutableList.of(p);
  }

  @Override
  public Json toJson() {
    return super.toJson()
        .with("speed", speed)
        .with("range", range);
  }

}
