package glitter.server.model;

import static java.lang.Integer.parseInt;
import java.util.List;
import com.google.common.base.Splitter;
import glitter.server.Lobby;
import ox.Json;
import ox.Log;

public class AdminConsole {

  public AdminConsole(World world) {
  }

  public void handle(Player from, String text) {
    Log.info("Got console command: " + text);

    List<String> m = Splitter.on(" ").splitToList(text);
    String command = m.get(0);

    if (command.equals("/start")) {
      Lobby.get().startGameIn(0);
      output(from, "Starting game now!");
    } else if (command.equals("/countdown")) {
      int seconds = parseInt(m.get(1));
      Lobby.get().startGameIn(seconds);
      output(from, "Setting countdown timer to " + seconds + " seconds.");
    } else {
      output(from, "Unrecognized command: " + command);
    }
  }

  private void output(Player p, String message) {
    p.send(Json.object()
        .with("command", "consoleOutput")
        .with("text", message));
  }

}
