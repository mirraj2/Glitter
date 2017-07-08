function World() {
  this.terrain = null;
  this.idPlayers = {};
  this.idEntities = {};
  this.container = new PIXI.Container();
  this.tiles = new PIXI.Container();
  this.entities = new PIXI.Container();
  this.players = new PIXI.Container();

  canvas.stage.displayList = new PIXI.DisplayList();

  this.entityDisplayGroup = new PIXI.DisplayGroup(0, true);
  this.entityDisplayGroup.on("add", function(sprite) {
    sprite.zOrder = -(sprite.y + sprite.height);
  });

  canvas.stage.addChild(this.container);
  this.container.addChild(this.tiles);
  this.container.addChild(this.entities);
  this.container.addChild(this.players);
}

World.prototype.setChests = function(chests) {
  var self = this;

  var sheet = PIXI.loader.resources["tiles.png"].texture;

  var texture = new PIXI.Texture(sheet.baseTexture);
  texture.frame = new PIXI.Rectangle(Tile.SIZE * 5, 0, Tile.SIZE, Tile.SIZE);

  chests.forEach(function(chest) {
    chest.blocksWalking = true;
    chest.canInteract = true;
    chest.type = "chest";
    world.idEntities[chest.id] = chest;

    var sprite = new PIXI.Sprite(texture);
    sprite.displayGroup = self.entityDisplayGroup;
    sprite.x = chest.x;
    sprite.y = chest.y;
    sprite.width = chest.width;
    sprite.height = chest.height;
    chest.getHitBox = function(buf) {
      buf.x = sprite.x;
      buf.y = sprite.y + sprite.height / 2;
      buf.width = sprite.width;
      buf.height = sprite.height / 2;
      return buf;
    };
    world.entities.addChild(sprite);

    chest.destroy = function() {
      world.entities.removeChild(sprite);
    };
  });
}

World.prototype.addPlayer = function(player) {
  this.idPlayers[player.id] = player;

  var texture = PIXI.loader.resources["wizard.png"].texture;
  var sprite = new PIXI.Sprite(texture);
  sprite.displayGroup = this.entityDisplayGroup;

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

World.prototype.removeEntity = function(entityId) {
  var entity = this.idEntities[entityId];

  if (entity == null) {
    return;
  }

  delete this.idEntities[entityId];

  entity.destroy();

  // we may have the spacebar interaction UI up and need to remove it.
  window.input.findInteraction();
}

World.prototype.renderTiles = function() {
  this.tiles.removeChildren();

  var sheet = PIXI.loader.resources["tiles.png"].texture;

  var textures = [];
  for (var i = 0; i < sheet.width / Tile.SIZE; i++) {
    var texture = new PIXI.Texture(sheet.baseTexture);
    texture.frame = new PIXI.Rectangle(Tile.SIZE * i, 0, Tile.SIZE, Tile.SIZE);
    textures.push(texture);
  }

  var tiles = this.terrain.tiles;
  for (var i = 0; i < tiles.length; i++) {
    var col = tiles[i];
    for (var j = 0; j < col.length; j++) {
      var t = tiles[i][j];
      if (t > 0) {
        var texture = textures[t];
        var tile = new PIXI.Sprite(texture);
        tile.x = i * Tile.SIZE;
        tile.y = j * Tile.SIZE;
        tile.width = Tile.SIZE;
        tile.height = Tile.SIZE;
        this.tiles.addChild(tile);
      }
    }
  }
}
