function ItemExplosion(msg) {
  this.loot = msg.loot;

  var texture = PIXI.Texture.fromImage("/loot.png");

  this.loot.forEach(function(bag) {
    bag.id = bag.item.id;
    bag.type = "loot";
    bag.canInteract = true;

    var size = bag.size = Tile.SIZE;
    var sprite = bag.sprite = new PIXI.Sprite(texture);
    sprite.displayGroup = world.entityDisplayGroup;
    sprite.width = size;
    sprite.height = size;
    sprite.x = msg.centerX - size / 2;
    sprite.y = msg.centerY - size / 2;

    bag.getHitBox = function(buf) {
      buf.x = sprite.x;
      buf.y = sprite.y;
      buf.width = sprite.width;
      buf.height = sprite.height;
    }

    world.addEntity(bag);
  });

  this.animationTime = 3000;

  glitter.register(this);
}

ItemExplosion.prototype.update = function(millis) {
  this.animationTime -= millis;
  this.loot.forEach(function(bag) {
    bag.sprite.x = interpolate(bag.sprite.x, bag.x - bag.size / 2, millis / 200);
    bag.sprite.y = interpolate(bag.sprite.y, bag.y - bag.size / 2, millis / 200);
  });
  return this.animationTime <= 0;
}