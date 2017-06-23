function Player(data) {
  this.id = data.id;
  this.x = data.x;
  this.y = data.y;
  this.width = 48;
  this.height = 64;
  this.sprite = null;
  this.hitbox = new PIXI.Rectangle(12, 48, 24, 16);

  // the keys this player has pressed down
  this.keys = {};
}

Player.prototype.setX = function(x) {
  this.x = x;
  this.sprite.x = Math.round(x);
}

Player.prototype.setY = function(y) {
  this.y = y;
  this.sprite.y = Math.round(y);
}

Player.prototype.setKeys = function(keys) {
  this.keys = {};
  for(var i = 0; i < keys.length; i++){
    this.keys[keys[i]] = true;
  }
}