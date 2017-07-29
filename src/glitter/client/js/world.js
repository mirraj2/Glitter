function World() {
  this.terrain = null;
  this.idPlayers = {};
  this.idEntities = {}; // does NOT contain player entities right now
  this.container = new PIXI.Container();
  this.tiles = new PIXI.Container();
  this.entities = new PIXI.Container();
  this.players = new PIXI.Container();

  canvas.stage.displayList = new PIXI.DisplayList();

  this.entityDisplayGroup = new PIXI.DisplayGroup(5, true);
  this.entityDisplayGroup.on("add", function(sprite) {
    sprite.zOrder = -(sprite.y + sprite.height);
  });

  this.tiles.displayGroup = new PIXI.DisplayGroup(0);

  canvas.stage.addChild(this.container);
  this.container.addChild(this.tiles);
  this.container.addChild(this.entities);
  this.container.addChild(this.players);

  var sheet = PIXI.loader.resources["tiles.png"].texture;

  var textures = this.tileTextures = [];
  for (var i = 0; i < sheet.width / Tile.SIZE; i++) {
    var texture = new PIXI.Texture(sheet.baseTexture);
    texture.frame = new PIXI.Rectangle(Tile.SIZE * i, 0, Tile.SIZE, Tile.SIZE);
    textures.push(texture);
  }

  glitter.register(this);
}

World.prototype.update = function(millis) {
  var idsToDelete = [];
  Object.values(this.idEntities).forEach(function(entity) {
    if (entity.update) {
      if (entity.update(millis) == false) {
        idsToDelete.push(entity.id);
      }
    }
  });

  Object.values(this.idPlayers).forEach(function(player) {
    player.update(millis);
  });

  for (var i = 0; i < idsToDelete.length; i++) {
    var id = idsToDelete[i];
    delete this.idEntities[id];
  }
}

World.prototype.setChests = function(chests) {
  var self = this;

  var texture = PIXI.Texture.fromImage("/treasure-chest.png");

  // var texture = new PIXI.Texture(sheet.baseTexture);
  // texture.frame = new PIXI.Rectangle(Tile.SIZE * 5, 0, Tile.SIZE, Tile.SIZE);

  chests.forEach(function(chest) {
    chest.blocksWalking = true;
    chest.canInteract = true;
    chest.type = "chest";

    var sprite = chest.sprite = new PIXI.Sprite(texture);
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
    };

    self.addEntity(chest);
  });
}

World.prototype.addPlayer = function(player) {
  checkNotNull(player.id);
  this.idPlayers[player.id] = player;

  player.sprite.displayGroup = this.entityDisplayGroup;

  this.players.addChild(player.sprite);

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

World.prototype.addEntity = function(entity) {
  checkNotNull(entity.id);
  this.idEntities[entity.id] = entity;
  this.entities.addChild(entity.sprite);
}

World.prototype.removeEntity = function(entityId) {
  var entity = this.idEntities[entityId];

  if (entity == null) {
    console.log("could not find entity with id: " + entityId)
    return;
  }

  if (entity.destroy) {
    if (entity.destroy() == false) {
      // gives the entity a chance to tell us that it should not be destroyed immediately.
      // for example, a projectile will want to finish rendering its final particles
      return;
    }
  }

  delete this.idEntities[entityId];

  if (entity.sprite) {
    world.entities.removeChild(entity.sprite);
  }

  // we may have the spacebar interaction UI up and need to remove it.
  window.input.findInteraction();
}

World.prototype.getPlayersAt = function(x, y) {
  var ret = [];
  Object.values(this.idPlayers).forEach(function(player) {
    if (player.spriteIntersects(x, y)) {
      ret.push(player);
    }
  });
  return ret;
}

World.prototype.renderTiles = function() {
  this.tiles.removeChildren();

  var textures = this.tileTextures;

  var tiles = this.terrain.tiles;
  for (var i = 0; i < tiles.length; i++) {
    var col = tiles[i];
    for (var j = 0; j < col.length; j++) {
      var t = col[j];
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

World.prototype.updateTile = function(i, j, type) {
  var textures = this.tileTextures;
  var texture = textures[type];
  var tile = new PIXI.Sprite(texture);
  tile.x = i * Tile.SIZE;
  tile.y = j * Tile.SIZE;
  tile.width = Tile.SIZE;
  tile.height = Tile.SIZE;
  this.tiles.addChild(tile);

  this.terrain.tiles[i][j] = type;
}
