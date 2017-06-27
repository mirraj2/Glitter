function MiniMap() {
  var self = this;

  this.container = new PIXI.Container();
  this.tiles = new PIXI.Container();
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
}

MiniMap.prototype.onResize = function() {
  this.container.x = $(document).width() - this.width - 4;
  this.container.y = $(document).height() - this.height - 4;
}

MiniMap.prototype.update = function() {
  if (window.me) {
    this.tiles.x = Math.round(this.width / 2 - me.x / Tile.SIZE);
    this.tiles.y = Math.round(this.height / 2 - me.y / Tile.SIZE);
  }
}

MiniMap.prototype.renderMap = function() {
  this.tiles.removeChildren();

  var sheet = PIXI.loader.resources["tiles.png"].texture;

  var textures = [];
  for (var i = 0; i < 5; i++) {
    var texture = new PIXI.Texture(sheet.baseTexture);
    texture.frame = new PIXI.Rectangle(Tile.SIZE * i, 0, Tile.SIZE, Tile.SIZE);
    textures.push(texture);
  }

  var tiles = world.terrain.tiles;
  var tileSize = 1;
  for (var i = 0; i < tiles.length; i++) {
    var col = tiles[i];
    for (var j = 0; j < col.length; j++) {
      var t = tiles[i][j];
      if (t > 0) {
        var texture = textures[t];
        var tile = new PIXI.Sprite(texture);
        tile.x = i * tileSize;
        tile.y = j * tileSize;
        tile.width = tileSize;
        tile.height = tileSize;
        this.tiles.addChild(tile);
      }
    }
  }
}