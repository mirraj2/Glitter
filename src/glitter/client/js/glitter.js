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
      self.world.render();
    } else {
      console.log("Unknown command: " + command);
      console.log(msg);
    }
  });
  socket.open();
}

Glitter.prototype.initGame = function() {
  var self = this;
  var canvas = new Canvas();
  var loop = new GameLoop(canvas);
  var camera = new Camera(this.world, this.canvas);

  canvas.stage.addChild(this.world.container);

  loop.start();

  this.canvas = canvas;
  this.loop = loop;

  PIXI.loader.add("/tiles.png").load(function() {
    console.log("done loading sprites.");
    self.world.render();
  });
}

function main() {
  new Glitter();
}

$(main);
