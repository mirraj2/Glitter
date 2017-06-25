package glitter.server.gen.terrain;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import ox.Rect;

public class Islands implements Iterable<Island> {

  public final List<Island> islands = Lists.newArrayList();
  public final Map<Point, Double> noiseCache = Maps.newHashMap();
  public Rect bounds = new Rect();
  public double[][] noise;

  public void add(Island island) {
    this.islands.add(island);

    if (bounds.w() == 0) {
      bounds = island.bounds;
    } else {
      bounds = bounds.union(island.bounds);
    }
  }

  public int size() {
    return islands.size();
  }

  @Override
  public Iterator<Island> iterator() {
    return islands.iterator();
  }

}
