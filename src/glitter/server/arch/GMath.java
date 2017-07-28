package glitter.server.arch;

import glitter.server.gen.Point;

public class GMath {

  public static int round(double d) {
    if (d > 0) {
      return (int) (d + 0.5d);
    } else {
      return (int) (d - 0.5d);
    }
  }

  public static int floor(double d) {
    return (int) d;
  }

  public static double distSquared(Point a, Point b) {
    return distSquared(a.x, a.y, b.x, b.y);
  }

  public static double distSquared(double x, double y, double x2, double y2) {
    double xDiff = x - x2;
    double yDiff = y - y2;
    return xDiff * xDiff + yDiff * yDiff;
  }

  public static boolean intersects(double circleX, double circleY, double circleRadius, Rect rect) {
    double closestX = circleX < rect.x ? rect.x : (circleX > rect.maxX() ? rect.maxX() : circleX);
    double closestY = circleY < rect.y ? rect.y : (circleY > rect.maxY() ? rect.maxY() : circleY);

    double dx = closestX - circleX;
    double dy = closestY - circleY;

    return (dx * dx + dy * dy) <= circleRadius * circleRadius;
  }


}
