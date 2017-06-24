package glitter.server.gen.terrain.perlin;

public class ContinentGenerator extends NoiseFunction {

  int numOctaves = 3;

  public ContinentGenerator() {
    zoom = 40;
  }

  @Override
  public double getValue(double x, double y) {
    double totalNoise = 0;
    for (int octave = 0; octave < numOctaves; octave++) {
      double scale = powersOfTwo[octave] / 128f * zoom;
      double noise = SimplexNoise.noise(x * scale, y * scale);


      noise = (noise + 1) / 2;

      double p = powersOfTwo[numOctaves - octave + 1] / 7d;
      noise *= p;

      totalNoise += noise;
    }
    return totalNoise;
  }

}
