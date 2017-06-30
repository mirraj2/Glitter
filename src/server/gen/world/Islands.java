package server.gen.world;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import ox.Rect;

public class Islands implements Iterable<Island> {

  public final List<Island> islands = Lists.newArrayList();
  public final Map<Point, Double> noiseCache = Maps.newHashMap();
  public Rect bounds = new Rect();
  public double[][] noise;
  public Set<Point> bridges = Sets.newHashSet();
  public Set<Point> debug = Sets.newHashSet();

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

  public double getNoise(int i, int j) {
    if (i < 0 || j < 0 || i >= bounds.w || j >= bounds.h) {
      return 0;
    }
    return noise[i][j];
  }

}
