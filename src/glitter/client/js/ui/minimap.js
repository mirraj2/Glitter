function MiniMap() {
  var self = this;

  this.container = new PIXI.Container();
  this.container.displayGroup = new PIXI.DisplayGroup(10);
  this.tiles = new PIXI.Graphics();
  this.width = 170;
  this.height = 170;

  var background = new PIXI.Graphics();
  background.lineStyle(1, 0xFFFFFF, .8);
  background.beginFill(0x000000, 1);
  background.drawRect(0, 0, this.width, this.height);
  background.endFill();

  this.forcefield = new PIXI.Graphics();

  var redDot = new PIXI.Graphics();
  redDot.beginFill(0xFF0000, 1);
  redDot.drawCircle(this.width / 2, this.height / 2, 2);
  redDot.endFill();

  var mask = new PIXI.Graphics();
  // mask.lineStyle(1, 0xFFFFFF, 1);
  mask.beginFill(0x444444, 1);
  mask.drawRect(0, 0, this.width, this.height);
  mask.endFill();
  this.container.addChild(mask);
  this.container.mask = mask;

  this.container.addChild(background);
  this.container.addChild(this.forcefield);
  this.container.addChild(this.tiles);
  this.container.addChild(redDot);
  canvas.stage.addChild(this.container);

  $(window).resize(function() {
    self.onResize();
  });
  self.onResize();

  glitter.register(this);
}

MiniMap.prototype.onResize = function() {
  this.container.x = $(document).width() - this.width - 4;
  this.container.y = $(document).height() - this.height - 4;
}

MiniMap.prototype.update = function() {
  if (window.me) {
    this.tiles.x = this.forcefield.x = Math.round(this.width / 2 - me.x / Tile.SIZE);
    this.tiles.y = this.forcefield.y = Math.round(this.height / 2 - me.y / Tile.SIZE);
  }
  if (forcefield.isActive()) {
    var g = this.forcefield;
    g.clear();
    g.beginFill(0x444444, 1);
    var x = forcefield.x / Tile.SIZE;
    var y = forcefield.y / Tile.SIZE;
    var r = forcefield.radius / Tile.SIZE;
    g.drawCircle(x, y, r);
    g.endFill();
  }
}

MiniMap.prototype.onEnterWorld = function(world) {
  var tiles = world.terrain.tiles;

  this.tiles.clear();
  for (var i = 0; i < tiles.length; i++) {
    var col = tiles[i];
    for (var j = 0; j < col.length; j++) {
      var t = col[j];
      if (t > 0) {
        if (t == Tile.GRASS) {
          this.tiles.beginFill(0x431D01, 1);
        } else if (t == Tile.BRIDGE) {
          this.tiles.beginFill(0x514F4A, 1);
        } else {
          this.tiles.beginFill(0xFFFFFF, 1);
        }
        this.tiles.drawRect(i, j, 1, 1);
        this.tiles.endFill();
      }
    }
  }
}
