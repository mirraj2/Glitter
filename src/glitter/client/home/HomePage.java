package glitter.client.home;

import bowser.Controller;
import bowser.template.Data;
import glitter.client.GlitterWebServer;

public class HomePage extends Controller {

  @Override
  public void init() {
    route("GET", "/").to("glitter.html").data(data);
  }

  private final Data data = context -> {
    boolean devMode = GlitterWebServer.devMode;
    context.put("websocketIP", devMode ? "localhost" : "localhost");
    context.put("websocketPort", 8081);
  };

}
