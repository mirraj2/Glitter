package glitter.server.arch;

import com.google.common.base.Stopwatch;
import ox.Log;

public class ScratchPad {

  public static void main(String[] args) throws Exception {
    int n = 100000000;
    double d = 0;
    for (int trial = 0; trial < 10; trial++) {
      Log.debug("Trial " + trial);
      Stopwatch w = Stopwatch.createStarted();
      for (int i = 0; i < n; i++) {
        d += 91414 * 91414;
        // d += Math.pow(91414, 2);
      }
      Log.debug(w);
      d = 1;
      w = Stopwatch.createStarted();
      for (int i = 0; i < n; i++) {
        d += Math.pow(91414, 20);
      }
      Log.debug(w + "\n\n");
    }
  }

}
