package glitter.server.model;

import static com.google.common.base.Preconditions.checkState;
import java.util.concurrent.atomic.AtomicBoolean;
import ox.Json;

public class TreasureChest extends Entity {

  public static final int WIDTH = Tile.SIZE, HEIGHT = Tile.SIZE;

  public final double x, y;
  public final int width = WIDTH, height = HEIGHT;

  private final AtomicBoolean looted = new AtomicBoolean(false);

  public TreasureChest(double x, double y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Attempts to open this chest. A chest cannot be opened more than once.
   */
  public void open() {
    checkState(looted.compareAndSet(false, true), "This chest was already looted!");
  }

  @Override
  public Json toJson() {
    return super.toJson()
        .with("x", x)
        .with("y", y)
        .with("width", width)
        .with("height", height);
  }

}
