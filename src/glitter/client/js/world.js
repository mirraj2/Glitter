function World() {
  this.terrain = null;
  this.idPlayers = {};
  this.container = new PIXI.Container();
  this.tiles = new PIXI.Container();
  this.players = new PIXI.Container();

  canvas.stage.addChild(this.container);
  this.container.addChild(this.tiles);
  this.container.addChild(this.players);
}

World.prototype.addPlayer = function(player) {
  this.idPlayers[player.id] = player;

  var texture = PIXI.loader.resources["wizard.png"].texture;
  var sprite = new PIXI.Sprite(texture);

  player.sprite = sprite;
  sprite.x = player.x;
  sprite.y = player.y;

  this.players.addChild(sprite);

  $(".numPlayers .count").text(Object.keys(this.idPlayers).length);
}

World.prototype.removePlayer = function(playerId) {
  var player = this.idPlayers[playerId];
  if (!player) {
    return;
  }

  delete this.idPlayers[playerId];

  this.players.removeChild(player.sprite);

  $(".numPlayers .count").text(Object.keys(this.idPlayers).length);
}

World.prototype.removeAllPlayers = function() {
  this.players.removeChildren();
}

World.prototype.renderTiles = function() {
  this.tiles.removeChildren();

  var sheet = PIXI.loader.resources["tiles.png"].texture;

  var textures = [];
  for (var i = 0; i < 5; i++) {
    var texture = new PIXI.Texture(sheet.baseTexture);
    texture.frame = new PIXI.Rectangle(TILE_SIZE * i, 0, TILE_SIZE, TILE_SIZE);
    textures.push(texture);
  }

  var tiles = this.terrain.tiles;
  for (var i = 0; i < tiles.length; i++) {
    var col = tiles[i];
    for (var j = 0; j < col.length; j++) {
      var t = tiles[i][j];
      var texture = textures[t];
      var tile = new PIXI.Sprite(texture);
      tile.x = i * TILE_SIZE;
      tile.y = j * TILE_SIZE;
      tile.width = TILE_SIZE;
      tile.height = TILE_SIZE;
      this.tiles.addChild(tile);
    }
  }
}
