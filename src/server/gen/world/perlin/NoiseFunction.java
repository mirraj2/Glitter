package server.gen.world.perlin;

public abstract class NoiseFunction {

  public static final double[] powersOfTwo = new double[100];

  static {
    for (int i = 0; i < powersOfTwo.length; i++) {
      powersOfTwo[i] = Math.pow(2, i - 2);
    }
  }
  
  public double zoom = 1.0;

  public abstract double getValue(double x, double y);

}