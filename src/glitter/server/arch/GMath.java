package glitter.server.arch;

import glitter.server.gen.terrain.Point;

public class GMath {

  public static int round(double d) {
    if (d > 0) {
      return (int) (d + 0.5d);
    } else {
      return (int) (d - 0.5d);
    }
  }

  public static double distSquared(Point a, Point b) {
    double xDiff = a.x - b.x;
    double yDiff = a.y - b.y;
    return xDiff * xDiff + yDiff * yDiff;
  }

}
