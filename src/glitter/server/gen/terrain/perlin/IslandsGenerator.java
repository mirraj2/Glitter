package glitter.server.gen.terrain.perlin;

public class IslandsGenerator extends NoiseFunction {

  private double totalWeights;
  private int minOctave, maxOctave;

  public IslandsGenerator() {
    this(0, 8);
  }

  public IslandsGenerator(int minOctave, int maxOctave) {
    setOctaves(minOctave, maxOctave);
  }

  public void setOctaves(int minOctave, int maxOctave) {
    this.minOctave = minOctave;
    this.maxOctave = maxOctave;
    totalWeights = Math.pow(2, maxOctave - minOctave + 1) - 1;
  }

  @Override
  public double getValue(double x, double y) {
    double totalNoise = 0;
    for (int octave = minOctave; octave <= maxOctave; octave++) {
      double scale = powersOfTwo[octave] / 128f * zoom;
      double noise = SimplexNoise.noise(x * scale, y * scale);


      noise = (noise + 1) / 2;

      double p = powersOfTwo[(maxOctave - minOctave + 1) - (octave - minOctave) + 1] / totalWeights;
      if (p < .01) {
        p = .01;
      }
      noise *= p;

      totalNoise += noise;
    }
    return totalNoise;
  }

}