function Network(ip, port) {
  var socket = this.connectToServer(ip, port);

  $(window).on("beforeunload", function() {
    socket.close();
  });

  this.socket = socket;
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
  // console.log(msg);
  var command = msg.command;
  if (command == "enterWorld") {
    world.terrain = new Terrain(msg.world.terrain);
    world.renderTiles();
  } else if (command == "addPlayer") {
    world.addPlayer(new Player(msg.player));
  } else if (command == "removePlayer") {
    world.removePlayer(msg.id);
  } else if (command == "takeControl") {
    window.me = world.idPlayers[msg.id];
  } else {
    console.log("Unknown command: " + command);
    console.log(msg);
  }
}