package glitter.server.model;

import java.util.concurrent.atomic.AtomicLong;
import ox.Json;

public class Entity {

  private static final AtomicLong idCounter = new AtomicLong();

  public final long id = idCounter.getAndIncrement();

  public Json toJson() {
    return Json.object()
        .with("id", id);
  }

}
