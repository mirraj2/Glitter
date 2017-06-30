package client;

import com.google.common.base.Stopwatch;
import bowser.WebServer;
import client.dust.DustDemo;
import client.home.HomePage;
import client.js.JSController;
import client.perlin.PerlinPage;
import ox.Config;
import ox.Log;

public class WebContentServer {

  private static final Config config = Config.load("glitter");
  public static final boolean devMode = config.getBoolean("devMode", false);

  public void run() {
    Stopwatch watch = Stopwatch.createStarted();
    Log.info("Starting server...");

    int port = devMode ? 8080 : 80;

    WebServer server = new WebServer("Glitter", port, devMode)
        .controller(new HomePage())
        .controller(new PerlinPage())
        .controller(new DustDemo())
        .controller(new JSController());

    server.start();

    Log.info("Glitter Server started on port %d (%s)", port, watch);
  }

}
