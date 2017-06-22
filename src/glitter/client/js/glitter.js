function Glitter() {
  this.world = new World();
  this.connectToServer();
  this.initGame();
}

Glitter.prototype.connectToServer = function() {
  var self = this;
  var socket = new Socket("$$(websocketIP)", "$$(websocketPort)", false);
  socket.onMessage(function(msg) {
    var command = msg.command;
    if (command == "enterLobby") {
      self.world.terrain = new Terrain(msg.world.terrain);
    } else {
      console.log("Unknown command: " + command);
      console.log(msg);
    }
  });
  socket.open();
}

Glitter.prototype.initGame = function() {
  var canvas = new Canvas();
  var loop = new GameLoop(canvas);

  loop.start();
}

function main() {
  new Glitter();
}

$(main);
