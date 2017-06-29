package glitter.server.model.item.spell;

import glitter.server.arch.GRandom;

public class Fireball extends Spell {

  private final int minDamage, maxDamage;

  public Fireball(GRandom rand) {
    super("Fireball");

    this.manaCost = 20;
    this.castTimeSeconds = 1;

    this.minDamage = 18;
    this.maxDamage = 30;
    this.description = String.format("Hurls a ball of fire, dealing %d to %d damage.", minDamage, maxDamage);
  }

}
