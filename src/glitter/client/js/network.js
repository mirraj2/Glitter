function Network(ip, port) {
  var socket = this.connectToServer(ip, port);

  $(window).on("beforeunload", function() {
    socket.close();
  });

  this.socket = socket;
  this.lootChooser = new LootChooser();
}

Network.prototype.send = function(msg) {
  this.socket.send(msg);
}

Network.prototype.connectToServer = function(ip, port) {
  var socket = new Socket(ip, port, false);
  socket.onMessage(this.handleMessage);
  socket.open();
  return socket;
}

Network.prototype.handleMessage = function(msg) {
  console.log(msg);
  var command = msg.command;
  if (command == "playerState") {
    var player = world.idPlayers[msg.id];
    player.setX(msg.x);
    player.setY(msg.y);
    player.setKeys(msg.keys);
  } else if (command == "removeEntity") {
    world.removeEntity(msg.id);
  } else if (command == "choose") {
    network.lootChooser.show(msg.choices);
  } else if (command == "enterWorld") {
    if ($(".countdown").is(":visible")) {
      $(".numPlayers label").text("alive");
      $(".countdown").hide();
    }
    world.removeAllPlayers();
    world.terrain = new Terrain(msg.world.terrain);
    world.renderTiles();
    world.setChests(msg.world.chests);
    minimap.renderMap();
    window.me = null;
  } else if (command == "addPlayer") {
    world.addPlayer(new Player(msg.player));
  } else if (command == "removePlayer") {
    world.removePlayer(msg.id);
  } else if (command == "takeControl") {
    window.me = world.idPlayers[msg.id];
    $(".numPlayers").show();
  } else if (command == "countdown") {
    window.gameStartTime = Date.now() + msg.millisLeft;
    $(".countdown").fadeIn();
  } else if (command == "consoleOutput") {
    $("<div>").text(msg.text).appendTo(".console .output");
  } else {
    console.log("Unknown command: " + command);
    console.log(msg);
  }
}