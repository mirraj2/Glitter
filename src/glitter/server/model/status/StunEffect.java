package glitter.server.model.status;

import glitter.server.model.Player;
import ox.Json;

public class StunEffect extends StatusEffect {

  public StunEffect(double durationMillis) {
    super("Stunned", durationMillis);
  }

  @Override
  public void onStart(Player player) {
    player.world.sendToAll(Json.object()
        .with("command", "addStatusEffect")
        .with("playerId", player.id)
        .with("name", name));
  }

  @Override
  public void onEnd(Player player) {
    player.world.sendToAll(Json.object()
        .with("command", "removeStatusEffect")
        .with("playerId", player.id)
        .with("name", name));
  }

}
