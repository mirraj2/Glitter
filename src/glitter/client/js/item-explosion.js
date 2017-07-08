function ItemExplosion(msg) {
  this.loot = msg.loot;

  var texture = PIXI.Texture.fromImage("/loot.png");

  this.loot.forEach(function(item) {
    var size = item.size = 48;
    var sprite = item.sprite = new PIXI.Sprite(texture);
    sprite.displayGroup = world.entityDisplayGroup;
    sprite.width = size;
    sprite.height = size;
    sprite.x = msg.centerX - size / 2;
    sprite.y = msg.centerY - size / 2;
    world.container.addChild(sprite);
  });

  this.animationTime = 3000;

  glitter.register(this);
}

ItemExplosion.prototype.update = function(millis) {
  this.animationTime -= millis;
  this.loot.forEach(function(item) {
    item.sprite.x = interpolate(item.sprite.x, item.x - item.size / 2, millis / 200);
    item.sprite.y = interpolate(item.sprite.y, item.y - item.size / 2, millis / 200);
  });
  return this.animationTime <= 0;
}