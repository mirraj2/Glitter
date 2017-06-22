package glitter.server.arch;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SwappingQueue<T> implements Iterable<T> {

  private ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<>();
  private ConcurrentLinkedQueue<T> buffer = new ConcurrentLinkedQueue<>();

  public void add(T item) {
    queue.add(item);
  }

  public void remove(T item) {
    queue.remove(item);
  }

  public int size() {
    return queue.size();
  }

  @Override
  public Iterator<T> iterator() {
    return queue.iterator();
  }

  /**
   * Returns all elements that have accumulated since the last call to swap.
   */
  public Collection<T> swap() {
    ConcurrentLinkedQueue<T> ret = queue;
    buffer.clear();
    queue = buffer;
    buffer = ret;
    return ret;
  }

}