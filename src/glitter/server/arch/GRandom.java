package glitter.server.arch;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.Random;
import com.google.common.collect.Iterables;

public class GRandom extends Random {

  private static final SecureRandom secureRandom = new SecureRandom();

  public GRandom() {
    this(secureRandom.nextLong());
  }

  public GRandom(long seed) {
    super(seed);
  }

  public double gauss() {
    return super.nextGaussian();
  }

  public int gaussInt(double average, double deviation) {
    return GMath.round(average + deviation * gauss());
  }

  public double gauss(double average, double deviation) {
    return average + deviation * gauss();
  }

  public <T> T random(Collection<T> c) {
    if (c.isEmpty()) {
      throw new RuntimeException("Can't get a random element from an empty collection.");
    }
    return Iterables.get(c, random(c.size()));
  }

  public int random(int n) {
    return (int) (super.nextDouble() * n);
  }

}
