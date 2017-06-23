window.TILE_SIZE = 48;

function Glitter() {
  this.initGame();
}

Glitter.prototype.initGame = function() {
  window.canvas = new Canvas();
  window.world = new World();
  window.camera = new Camera();
  window.input = new Input();
  
  input.listen();

  var loop = new GameLoop(canvas);
  loop.start();

  PIXI.loader.add("tiles.png").add("wizard.png").load(function() {
    console.log("done loading sprites.");

    window.network = new Network("$$(websocketIP)", $$(websocketPort));
  });
}

function main() {
  new Glitter();
}

$(main);
