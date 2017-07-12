package glitter.client.particles;

import bowser.Controller;

public class ParticleEditor extends Controller {

  @Override
  public void init() {
    route("GET", "/particles").to("particle-editor.html");
  }

}
