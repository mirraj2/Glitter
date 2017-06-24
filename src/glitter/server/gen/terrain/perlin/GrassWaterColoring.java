package glitter.server.gen.terrain.perlin;

import java.awt.Color;

public class GrassWaterColoring implements ColoringFunction {

  @Override
  public int getColor(double noise) {
    return noise > .5 ? Color.green.getRGB() : Color.blue.getRGB();
  }

}
