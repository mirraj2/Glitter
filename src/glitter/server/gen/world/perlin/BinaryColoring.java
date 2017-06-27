package glitter.server.gen.world.perlin;

import java.awt.Color;

public class BinaryColoring implements ColoringFunction {

  private final double threshold;

  public BinaryColoring(double threshold) {
    this.threshold = threshold;
  }

  @Override
  public int getColor(double noise) {
    return noise >= threshold ? Color.green.getRGB() : Color.black.getRGB();
  }

}
