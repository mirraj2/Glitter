package glitter.client.home;

import bowser.model.Controller;
import bowser.template.Data;
import glitter.client.WebContentServer;
import glitter.server.GlitterServer;

public class HomePage extends Controller {

  @Override
  public void init() {
    route("GET", "/").to("glitter.html").data(data);

    mapFolder("png", "assets");
  }

  private final Data data = context -> {
    boolean devMode = WebContentServer.devMode;
    context.put("websocketIP", devMode ? "localhost" : "playglitter.com");
    context.put("websocketPort", GlitterServer.port);
  };

}
