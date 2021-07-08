package glitter.server.model;

import static com.google.common.base.Preconditions.checkState;

import java.util.concurrent.atomic.AtomicBoolean;

public class TreasureChest extends Entity {

  public static final int WIDTH = Tile.SIZE, HEIGHT = Tile.SIZE;

  private final AtomicBoolean looted = new AtomicBoolean(false);

  public TreasureChest(double x, double y) {
    super(x, y, WIDTH, HEIGHT);
  }

  /**
   * Attempts to open this chest. A chest cannot be opened more than once.
   */
  public void open() {
    checkState(looted.compareAndSet(false, true), "This chest was already looted!");
  }

  @Override
  public boolean blocksWalking() {
    return true;
  }

}
