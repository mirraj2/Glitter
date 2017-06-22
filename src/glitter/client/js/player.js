function Player(data) {
  this.id = data.id;
  this.x = data.x;
  this.y = data.y;
  this.width = 48;
  this.height = 64;
  this.sprite = null;
  this.hitbox = new PIXI.Rectangle(12, 48, 24, 16)
}

Player.prototype.setX = function(x) {
  this.x = x;
  this.sprite.x = x;
}

Player.prototype.setY = function(y) {
  this.y = y;
  this.sprite.y = y;
}