package glitter.server.gen.terrain;

import java.util.List;
import ox.Rect;

public class Island {

  public final List<Point> points;
  public final Rect bounds;

  public Island(List<Point> points) {
    this.points = points;

    int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
    int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;

    for (Point p : points) {
      minX = Math.min(p.x, minX);
      minY = Math.min(p.y, minY);
      maxX = Math.max(p.x, maxX);
      maxY = Math.max(p.y, maxY);
    }

    bounds = new Rect(minX, minY, maxX - minX, maxY - minY);
  }

}
