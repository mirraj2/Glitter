package glitter.server.model.item.spell;

import glitter.server.arch.GRandom;
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
  public Json toJson() {
    return super.toJson()
        .with("speed", speed)
        .with("range", range);
  }

}
