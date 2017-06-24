package glitter.client.perlin;

import static java.lang.Integer.parseInt;
import static ox.util.Utils.random;
import java.awt.image.BufferedImage;
import java.util.List;
import com.google.common.collect.Lists;
import bowser.Controller;
import bowser.Handler;
import bowser.template.Data;
import glitter.server.gen.terrain.perlin.ColoringFunction;
import glitter.server.gen.terrain.perlin.GradiantColoringFunction;
import glitter.server.gen.terrain.perlin.IslandsGenerator;
import ox.IO;

public class PerlinPage extends Controller {

  @Override
  public void init() {
    route("GET", "/perlin").to("perlin.html").data(data);
    route("GET", "/perlin.png").to(genPerlin);
  }

  private final Handler genPerlin = (request, response) -> {
    IslandsGenerator gen = new IslandsGenerator();
    gen.setOctaves(parseInt(request.param("min")), parseInt(request.param("max")));

    ColoringFunction coloring = new GradiantColoringFunction();

    int x = random(Short.MAX_VALUE);
    int y = random(Short.MAX_VALUE);
    int w = 1200, h = 800;

    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    for (int i = 0; i < w; i++) {
      for (int j = 0; j < h; j++) {
        int rgb = coloring.getColor(gen.getValue(x + i, y + j));
        bi.setRGB(i, j, rgb);
      }
    }

    IO.from(bi).imageFormat("png").to(response.getOutputStream());
  };

  private final Data data = context -> {
    List<String> urls = Lists.newArrayList();
    for (int min = 0; min < 10; min++) {
      for (int max = min; max < 10; max += 1) {
        urls.add("/perlin.png?min=" + min + "&max=" + max);
      }
    }
    context.put("urls", urls);
  };

}
