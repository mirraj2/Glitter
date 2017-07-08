window.Tile.SIZE = 48;

function Glitter() {
  this.callbacks = [];
}

Glitter.prototype.register = function(tickable) {
  this.callbacks.push(tickable);
}

Glitter.prototype.initGame = function() {
  window.canvas = new Canvas();
  window.world = new World();
  window.camera = new Camera();

  var spells = new Spells(world.container);
  window.minimap = new MiniMap();
  window.quickbar = new Quickbar();
  window.inventory = new Inventory(quickbar);
  window.input = new Input(spells);

  glitter.register(Dust);

  new GameLoop(canvas);

  PIXI.loader.add("tiles.png").add("wizard.png").load(function() {
    console.log("done loading sprites.");

    window.network = new Network("$$(websocketIP)", $$(websocketPort), spells);
  });
}

function main() {
  window.glitter = new Glitter();
  glitter.initGame();
}

$(main);
