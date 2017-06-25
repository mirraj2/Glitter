package glitter.server.gen.terrain.perlin;

public class ContinentsAndIslands extends NoiseFunction {

  private final IslandsGenerator continents = new IslandsGenerator(1, 8);
  private final IslandsGenerator islands = new IslandsGenerator(3, 10);

  public ContinentsAndIslands() {
    // continents.zoom = .7;

    continents.zoom = 2;
    islands.zoom = 3;
  }

  @Override
  public double getValue(double x, double y) {
    double aVal = continents.getValue(x, y);
    double bVal = islands.getValue(x + 10_000, y + 10_000);

    if (aVal < .64) {
      aVal = 0;
    }
    if (bVal < .6) {
      bVal = 0;
    }

    return Math.min(aVal + bVal, 1);
  }

}
