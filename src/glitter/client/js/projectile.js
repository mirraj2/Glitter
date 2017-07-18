function Projectile(emitter) {
  this.emitter = emitter;
  this.id = null;
  this.vx = null;
  this.vy = null;
}

Projectile.prototype.update = function(millis) {
  var seconds = millis / 1000;

  if (this.vx && this.emitter.emitter.emit) {
    this.emitter.translate(this.vx * seconds, this.vy * seconds);
  } else if (this.target) {
    var p = this.emitter.emitter.spawnPos;

    var dx = this.target.centerX() - p.x;
    var dy = this.target.centerY() - p.y;

    var speed = this.speed * Tile.SIZE * millis / 1000;

    var norm = speed / Math.sqrt(dx * dx + dy * dy);
    dx *= norm;
    dy *= norm;

    this.emitter.translate(dx, dy);
  }

  return this.emitter.update(millis);
}

Projectile.prototype.homeInOn = function(target, speed) {
  this.target = target;
  this.speed = speed;
}

Projectile.prototype.destroy = function() {
  this.emitter.emitter.emit = false;
  return false;
}
