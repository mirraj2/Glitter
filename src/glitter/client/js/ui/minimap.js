function MiniMap() {
  var self = this;

  this.container = new PIXI.Container();
  this.container.displayGroup = new PIXI.DisplayGroup(10);
  this.tiles = new PIXI.Graphics();
  this.width = 170;
  this.height = 170;

  var background = new PIXI.Graphics();
  background.lineStyle(2, 0xFFFFFF, .8);
  background.beginFill(0x444444, .8);
  background.drawCircle(this.width / 2, this.height / 2, this.width / 2);
  background.endFill();

  var redDot = new PIXI.Graphics();
  redDot.beginFill(0xFF0000, 1);
  redDot.drawCircle(this.width / 2, this.height / 2, 2);
  redDot.endFill();

  var mask = new PIXI.Graphics();
  mask.lineStyle(1, 0xFFFFFF, 1);
  mask.beginFill(0x444444, 1);
  mask.drawCircle(this.width / 2, this.height / 2, this.width / 2);
  mask.endFill();
  this.container.addChild(mask);
  this.tiles.mask = mask;

  this.container.addChild(background);
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
    this.tiles.x = this.width / 2 - me.x / Tile.SIZE;
    this.tiles.y = this.height / 2 - me.y / Tile.SIZE;
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
