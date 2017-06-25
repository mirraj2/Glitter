package glitter.server.gen.terrain;

public class Point {

  public int x, y;

  public Point() {
  }

  public Point(int x, int y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public String toString() {
    return "(" + x + ", " + y + ")";
  }

  @Override
  public int hashCode() {
    return 31 * x + y;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Point)) {
      return false;
    }
    Point p = (Point) obj;
    return x == p.x && y == p.y;
  }

}
