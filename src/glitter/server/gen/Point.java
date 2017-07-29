package glitter.server.gen;

public class Point {

  public int x, y;

  public Point() {
  }

  public Point(Point p) {
    this(p.x, p.y);
  }

  public Point(double x, double y) {
    this((int) x, (int) y);
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
