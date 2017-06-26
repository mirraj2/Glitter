function MiniMap() {
  this.container = new PIXI.Container();
  this.container.x = $(document).width() - 128;
  this.container.y = $(document).height() - 128;
//  this.container.width = 128;
//  this.container.height = 128;
  console.log(this.container.x);
  console.log(this.container.y);
  canvas.stage.addChild(this.container);
}

MiniMap.prototype.renderMap = function() {
  this.container.removeChildren();
  
  var graphics = new PIXI.Graphics();
  graphics.lineStyle(1, "gray", .5);
  graphics.beginFill("black", .8);
  graphics.drawRect(0, 0, 128, 128);
  this.container.addChild(graphics);

  var sheet = PIXI.loader.resources["tiles.png"].texture;

  var textures = [];
  for (var i = 0; i < 5; i++) {
    var texture = new PIXI.Texture(sheet.baseTexture);
    texture.frame = new PIXI.Rectangle(TILE_SIZE * i, 0, TILE_SIZE, TILE_SIZE);
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
        this.container.addChild(tile);
      }
    }
  }
}