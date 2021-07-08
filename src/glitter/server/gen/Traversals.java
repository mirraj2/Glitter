package glitter.server.gen;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Traversals {

  public static List<Point> flood(Point startingPoint, Function<Point, TState> callback) {
    return flood(ImmutableList.of(startingPoint), callback);
  }

  public static List<Point> flood(Collection<Point> startingPoints, Function<Point, TState> callback) {
    Queue<Point> queue = Lists.newLinkedList(startingPoints);

    Point NULL = new Point();

    Map<Point, Point> lookback = Maps.newHashMap();
    for (Point p : startingPoints) {
      lookback.put(p, NULL);
    }

    Point p = null;

    while (!queue.isEmpty()) {
      p = queue.poll();
      TState state = callback.apply(p);
      if (state == TState.NO_FILL) {
        continue;
      } else if (state == TState.FILL) {
        for (Point dir : dirs) {
          Point next = new Point(p.x + dir.x, p.y + dir.y);
          if (lookback.putIfAbsent(next, p) == null) {
            queue.add(next);
          }
        }
      } else if (state == TState.RETURN_PATH) {
        List<Point> path = Lists.newArrayList();
        while (p != NULL) {
          path.add(p);
          p = lookback.get(p);
        }
        return Lists.reverse(path);
      } else if (state == TState.HALT) {
        return null;
      }
    }

    return null;
  }

  public static enum TState {
    FILL, NO_FILL, RETURN_PATH, HALT;
  }

  public static void spiral(Point startPoint, Predicate<Point> callback) {
    spiral(startPoint.x, startPoint.y, callback);
  }

  public static void spiral(int startX, int startY, Predicate<Point> callback) {
    int dirIndex = -1;
    int radius = 1;
    int c = 0;
    int segments = 0;
    Point p = new Point(startX, startY);

    while (callback.test(p)) {
      if (++c == radius) {
        c = 0;
        dirIndex = (dirIndex + 1) % 4;
        if (segments++ % 2 == 0) {
          radius++;
        }
      }
      Point dir = dirs[dirIndex];
      p.x += dir.x;
      p.y += dir.y;
    }
  }

  public static final Point[] dirs = new Point[] {
      new Point(0, -1), new Point(-1, 0),
      new Point(0, 1), new Point(1, 0) };

}
