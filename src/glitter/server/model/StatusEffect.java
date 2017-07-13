package glitter.server.model;

import glitter.server.model.Player.Stat;

/**
 * A buff or a debuff that is applied to a player for a time.
 * 
 * An example buff might be: "+20% movement speed"
 * 
 * An example debuff might be: "you take double fire damage for 8 seconds"
 */
public abstract class StatusEffect {

  public final String name;
  public final double durationMillis;
  public double millisLeft;

  public StatusEffect(String name, double durationMillis) {
    this.name = name;
    this.durationMillis = durationMillis;
    this.millisLeft = durationMillis;
  }

  public boolean update(double millis) {
    this.millisLeft -= millis;
    return this.millisLeft > 0;
  }

  /**
   * Applies this effect to a player.
   */
  public void onStart(Player player) {
    // subclasses can override
  }

  /**
   * Removes this effect from a player.
   */
  public void onEnd(Player player) {
    // subclasses can override
  }

  /**
   * Refreshes this effect, resetting the time left.
   */
  public void refresh() {
    this.millisLeft = durationMillis;
  }

  /**
   * The maximum number of times this effect can exist on the same entity at the same time.
   * 
   * By default, effects cannot stack.
   */
  public int getMaxStacks() {
    return 1;
  }

  @Override
  public String toString() {
    return name;
  }

  public static class Chilled extends StatusEffect {
    private static final double SPEED_REDUCTTION = 20;

    public Chilled(double duration) {
      super("Chilled", duration);
    }

    @Override
    public void onStart(Player player) {
      player.stats.compute(Stat.MOVEMENT, (stat, val) -> val - SPEED_REDUCTTION);
      player.broadcastStats();
    }

    @Override
    public void onEnd(Player player) {
      player.stats.compute(Stat.MOVEMENT, (stat, val) -> val + SPEED_REDUCTTION);
      player.broadcastStats();
    }
  }

}
