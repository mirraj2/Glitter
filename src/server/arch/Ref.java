package server.arch;

/**
 * Used as a less verbose version of AtomicReference.
 * 
 * Useful for dealing with the fact that lambdas only let you reference final variables. Hope they fix this.
 */
public class Ref<T> {

  public T val = null;

  private Ref(T val) {
    this.val = val;
  }

  public static <T> Ref<T> ofNull() {
    return new Ref<T>(null);
  }

  public static <T> Ref<T> of(T val) {
    return new Ref<T>(val);
  }

}
