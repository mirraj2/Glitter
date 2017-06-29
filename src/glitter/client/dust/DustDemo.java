package glitter.client.dust;

import bowser.Controller;

public class DustDemo extends Controller {

  @Override
  public void init() {
    route("GET", "/dust").to("dust-demo.html");
  }

}
