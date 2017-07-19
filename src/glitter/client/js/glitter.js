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
  window.tooltips = new Tooltips();
  
  window.particleSystem = new ParticleSystem();
  
  new Background(particleSystem);
  
  window.spells = new Spells(world.container, particleSystem);
  window.minimap = new MiniMap();
  window.quickbar = new Quickbar();
  window.inventory = new Inventory(quickbar);
  window.input = new Input(spells);
  window.castIndicator = new CastIndicator();

  new GameLoop(canvas);

  PIXI.loader.add("tiles.png").add("wizard.png").add("stun.png").load(function() {
    console.log("done loading sprites.");

    window.network = new Network("$$(websocketIP)", $$(websocketPort), spells);
  });
}

function main() {
  window.glitter = new Glitter();
  glitter.initGame();
}

$(main);
