package client.perlin;

import static ox.util.Utils.random;
import java.awt.image.BufferedImage;
import java.util.List;
import com.google.common.collect.Lists;
import bowser.Controller;
import bowser.Handler;
import bowser.template.Data;
import ox.IO;
import server.gen.world.perlin.BinaryColoring;
import server.gen.world.perlin.ColoringFunction;
import server.gen.world.perlin.ContinentsAndIslands;

public class PerlinPage extends Controller {

  @Override
  public void init() {
    route("GET", "/perlin").to("perlin.html").data(data);
    route("GET", "/perlin.png").to(genPerlin);
  }

  private final Handler genPerlin = (request, response) -> {
    ContinentsAndIslands function = new ContinentsAndIslands();

    ColoringFunction coloring = new BinaryColoring(.6);

    int x = random(Short.MAX_VALUE);
    int y = random(Short.MAX_VALUE);
    int w = 1200, h = 800;

    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    for (int i = 0; i < w; i++) {
      for (int j = 0; j < h; j++) {
        int rgb = coloring.getColor(function.getValue(x + i, y + j));
        bi.setRGB(i, j, rgb);
      }
    }

    IO.from(bi).imageFormat("png").to(response.getOutputStream());
  };

  private final Data data = context -> {
    List<String> urls = Lists.newArrayList();
    for (int i = 0; i < 3; i++) {
      urls.add("/perlin.png?&t=" + System.nanoTime());
    }
    context.put("urls", urls);
  };

}
