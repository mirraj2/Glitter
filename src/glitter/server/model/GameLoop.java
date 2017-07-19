package glitter.server.model;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import ox.util.Utils;

public class GameLoop {

  private static final Executor executor = Executors.newCachedThreadPool();

  private final Consumer<Double> callback;
  private boolean running = false;

  public GameLoop(Consumer<Double> callback) {
    this.callback = callback;
  }

  public void start() {
    running = true;
    executor.execute(() -> {
      try {
        run(callback);
      } catch (Throwable t) {
        t.printStackTrace();
      }
    });
  }

  public void stop() {
    running = false;
  }

  private void run(Consumer<Double> callback) {
    long lastFPSUpdate = System.nanoTime();
    long MAX_UPDATE_TIME = 100;
    double t = 10;
    double timeSpentOnUpdate = 0;
    int numFrames = 0;
    while (running) {
      long now = System.nanoTime();

      double timeLeft = t;
      while (timeLeft > 0) {
        double tickTime = Math.min(timeLeft, MAX_UPDATE_TIME);
        try {
          callback.accept(tickTime);
          numFrames++;
        } catch (Exception e) {
          e.printStackTrace();
        }
        timeLeft -= MAX_UPDATE_TIME;
      }
      t = (System.nanoTime() - now) / 1_000_000d;
      timeSpentOnUpdate += t;

      if (now - lastFPSUpdate >= 1_000_000_000) {
        double averageFrameTime = timeSpentOnUpdate / numFrames;
        // Log.debug("Server FPS: " + Utils.format(1000.0 / averageFrameTime));
        timeSpentOnUpdate = numFrames = 0;
        lastFPSUpdate = now;
      }

      long sleepTime = (long) Math.floor(1000 / 40.0 - t);
      if (sleepTime > 0) {
        Utils.sleep(sleepTime);
      }
      t = (System.nanoTime() - now) / 1_000_000d;
      t = Math.min(t, 1000);
    }
  }

}
