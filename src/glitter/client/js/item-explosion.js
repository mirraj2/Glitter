function ItemExplosion(msg) {
  this.loot = msg.loot;

  this.loot.forEach(function(bag) {
    var entity = createEntityForItem(bag.item, msg.centerX, msg.centerY);
    bag.sprite = entity.sprite;
    bag.toX = bag.x - entity.sprite.width / 2;
    bag.toY = bag.y - entity.sprite.height / 2;
  });

  this.animationTime = 3000;

  glitter.register(this);
}

/**
 * Adds a loot bag containing an item. Returns the entity.
 */
window.createEntityForItem = function(item, x, y) {
  var texture = PIXI.Texture.fromImage("/loot.png");

  var entity = {
    id : item.id,
    type : "loot",
    canInteract : true
  };

  var size = Tile.SIZE;
  var sprite = entity.sprite = new PIXI.Sprite(texture);
  sprite.displayGroup = world.entityDisplayGroup;
  sprite.width = size;
  sprite.height = size;
  sprite.x = x - size / 2;
  sprite.y = y - size / 2;

  entity.getHitBox = function(buf) {
    buf.x = sprite.x;
    buf.y = sprite.y;
    buf.width = sprite.width;
    buf.height = sprite.height;
  }

  var fill;
  if (item.rarity == "COMMON") {
    fill = 0xFFFFFF;
  } else if (item.rarity == "RARE") {
    fill = 0x3b93ff;
  } else if (item.rarity == "EPIC") {
    fill = 0xa757ff;
  } else if (item.rarity == "LEGENDARY") {
    fill = 0xf9802b;
  }

  var text = new PIXI.Text(item.name, new PIXI.TextStyle({
    fontSize : 44,
    fill : fill
  }));
  var margin = 8;
  text.x = margin;
  text.y = margin;

  var nameBox = new PIXI.Graphics();

  var rect = new PIXI.Rectangle();
  text.getLocalBounds(rect);
  nameBox.beginFill(0x222222, .7);
  nameBox.drawRect(0, 0, rect.width + margin * 2, rect.height + margin * 2);
  nameBox.x = sprite.width - rect.width / 2;
  nameBox.y = -rect.height - 4;

  sprite.addChild(nameBox);
  nameBox.addChild(text);

  world.addEntity(entity);

  return entity;
};

ItemExplosion.prototype.update = function(millis) {
  this.animationTime -= millis;
  this.loot.forEach(function(bag) {
    bag.sprite.x = interpolate(bag.sprite.x, bag.toX, millis / 200);
    bag.sprite.y = interpolate(bag.sprite.y, bag.toY, millis / 200);
  });
  return this.animationTime <= 0;
}