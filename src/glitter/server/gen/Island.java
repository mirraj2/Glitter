package glitter.server.gen;

import java.util.Set;

import glitter.server.arch.Rect;

public class Island {

  public final Set<Point> points;
  public final Rect bounds;

  public Island(Set<Point> points) {
    this.points = points;

    int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
    int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;

    for (Point p : points) {
      minX = Math.min(p.x, minX);
      minY = Math.min(p.y, minY);
      maxX = Math.max(p.x, maxX);
      maxY = Math.max(p.y, maxY);
    }

    bounds = new Rect(minX, minY, maxX - minX + 1, maxY - minY + 1);
  }

  public int size() {
    return points.size();
  }

  @Override
  public String toString() {
    return points.toString();
  }

}
