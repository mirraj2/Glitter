package glitter.server.gen.perlin;

import java.awt.Color;

public class GradiantColoringFunction implements ColoringFunction {

  @Override
  public int getColor(double noise) {
    int value = (int) (noise * 256);

    int to = colorIndices.length - 1;
    for (int i = 0; i < colorIndices.length; i++) {
      if (colorIndices[i] >= value) {
        to = i;
        break;
      }
    }
    if (to == 0) {
      to++;
    }

    Color a = colors[to - 1];
    Color b = colors[to];
    double p = 1.0 * (value - colorIndices[to - 1]) / (colorIndices[to] - colorIndices[to - 1]);

    int red = interp(a.getRed(), b.getRed(), p);
    int green = interp(a.getGreen(), b.getGreen(), p);
    int blue = interp(a.getBlue(), b.getBlue(), p);

    return ((red & 0xFF) << 16) |
        ((green & 0xFF) << 8) |
        ((blue & 0xFF) << 0);
  }

  private int interp(int a, int b, double p) {
    return (int) (a + p * (b - a));
  }

  private static final Color[] colors = new Color[] {
      new Color(255, 255, 255),
      new Color(153, 153, 153),
      new Color(0, 128, 0),
      new Color(96, 176, 0),
      new Color(224, 224, 128),
      new Color(204, 204, 204),
      new Color(64, 64, 255),
      new Color(0, 0, 192)
  };
  private static final int[] colorIndices = new int[] { 0, 40, 64, 92, 99, 100, 128, 255 };

}