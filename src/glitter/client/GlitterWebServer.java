package glitter.client;

import java.io.File;
import com.google.common.base.Stopwatch;
import bowser.WebServer;
import glitter.client.home.HomePage;
import glitter.client.js.JSController;
import ox.Config;
import ox.Log;
import ox.OS;

public class GlitterWebServer {

  private static final Config config = Config.load("glitter");
  public static final boolean devMode = config.getBoolean("devMode", false);

  public void run() {
    Stopwatch watch = Stopwatch.createStarted();
    Log.info("Starting server...");

    int port = devMode ? 8080 : 80;

    WebServer server = new WebServer("Glitter", port, devMode)
        .controller(new HomePage())
        .controller(new JSController());

    server.start();

    Log.info("Glitter Server started on port %d (%s)", port, watch);
  }

  public static void main(String[] args) {
    Log.logToFolder(new File(OS.getHomeFolder(), "log"));

    new GlitterWebServer().run();
  }

}
