package server.arch;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SwappingQueue<T> implements Collection<T> {

  private ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<>();
  private ConcurrentLinkedQueue<T> buffer = new ConcurrentLinkedQueue<>();

  @Override
  public boolean add(T item) {
    return queue.add(item);
  }

  @Override
  public boolean remove(Object item) {
    return queue.remove(item);
  }

  @Override
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

  @Override
  public void clear() {
    queue.clear();
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return queue.containsAll(c);
  }

  @Override
  public boolean isEmpty() {
    return queue.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return queue.contains(o);
  }

  @Override
  public boolean addAll(Collection<? extends T> c) {
    return queue.addAll(c);
  }

  @Override
  public Object[] toArray() {
    return queue.toArray();
  }

  @Override
  public <K> K[] toArray(K[] a) {
    return queue.toArray(a);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return queue.removeAll(c);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return queue.retainAll(c);
  }

}