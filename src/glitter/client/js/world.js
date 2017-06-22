function World() {
  this.terrain = null;
  this.container = new PIXI.Container();
}

World.prototype.render = function() {
  this.container.removeChildren();

  var sheet = PIXI.loader.resources["/tiles.png"].texture;

  if (!sheet) {
    return;
  }

  var grass = new PIXI.Texture(sheet.baseTexture);
  grass.frame = new PIXI.Rectangle(32 * 2.5, 32 * 1.5, 32, 32);
  
  var stone = new PIXI.Texture(sheet.baseTexture);
  stone.frame = new PIXI.Rectangle(32 * 12.5, 32 * 1.5, 32, 32);

  var tiles = this.terrain.tiles;
  for (var i = 0; i < tiles.length; i++) {
    var col = tiles[i];
    for (var j = 0; j < col.length; j++) {
      var t = tiles[i][j];
      var texture;
      if (t == 1) {
        texture = grass;
      } else if (t == 2) {
        texture = stone;
      } else {
        continue;
      }
      var tile = new PIXI.Sprite(texture);
      tile.x = i * 32;
      tile.y = j * 32;
      this.container.addChild(tile);
    }
  }
}
