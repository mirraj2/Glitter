package glitter.server.arch;

import static java.lang.Integer.parseInt;

import java.util.Iterator;

import com.google.common.base.Splitter;

public class Rect {
  public double x, y, w, h;

  public Rect() {
  }

  public Rect(double x, double y, double w, double h) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Rect)) {
      return false;
    }
    Rect r = (Rect) obj;
    return x == r.x && y == r.y && w == r.w && h == r.h;
  }

  public boolean intersects(Rect other) {
    if ((x >= other.x + other.w) || (x + w <= other.x)
        || (y >= other.y + other.h) || (y + h <= other.y)) {
      return false;
    }
    return true;
  }

  public boolean contains(double x, double y) {
    if (x < this.x || y < this.y) {
      return false;
    }

    if (x >= this.x + w || y >= this.y + h) {
      return false;
    }

    return true;
  }

  public boolean contains(Rect other) {
    return other.x >= x && other.y >= y && other.maxX() <= maxX() && other.maxY() <= maxY();
  }

  public Rect centerOn(double xx, double yy) {
    x = xx - w / 2;
    y = yy - h / 2;
    return this;
  }

  public Rect location(double x, double y) {
    this.x = x;
    this.y = y;
    return this;
  }

  public Rect union(Rect r) {
    if (r == null) {
      return this;
    }
    return expandToInclude(r.x, r.y).expandToInclude(r.maxX(), r.maxY());
  }

  public int x() {
    return (int) x;
  }

  public int y() {
    return (int) y;
  }

  public int w() {
    return (int) w;
  }

  public int h() {
    return (int) h;
  }

  public double centerX() {
    return x + w / 2;
  }

  public double centerY() {
    return y + h / 2;
  }

  public double maxX() {
    return x + w;
  }

  public double maxY() {
    return y + h;
  }

  @Override
  public String toString() {
    return x + ", " + y + ", " + w + ", " + h;
  }

  public Rect resizeWithin(Rect r) {
    return moveWithin(r.x, r.y, r.w, r.h);
  }

  public Rect moveWithin(double bx, double by, double bw, double bh) {
    double newX = Math.max(x, bx);
    double newY = Math.max(y, by);
    newX = Math.min(newX, bx + bw - w);
    newY = Math.min(newY, by + bh - h);
    return new Rect(newX, newY, w, h);
  }

  public Rect expandToInclude(double x, double y) {
    double fromX = Math.min(this.x, x);
    double fromY = Math.min(this.y, y);
    double toX = Math.max(maxX(), x);
    double toY = Math.max(maxY(), y);
    return new Rect(fromX, fromY, toX - fromX, toY - fromY);
  }

  public String serialize() {
    return x() + " " + y() + " " + w() + " " + h();
  }

  public static Rect parse(String s) {
    Iterator<String> iter = Splitter.on(' ').split(s).iterator();
    return new Rect(parseInt(iter.next()), parseInt(iter.next()), parseInt(iter.next()),
        parseInt(iter.next()));
  }

  public static Rect create(double x1, double y1, double x2, double y2) {
    return new Rect(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
  }

}
