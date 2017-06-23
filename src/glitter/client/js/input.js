/**
 * Listens for user input.
 */

function Input() {
  this.keys = {};
  this.rect = new PIXI.Rectangle();

  // whether the user has pressed any keys since the last update
  this.dirty = false;
}

Input.prototype.update = function(t) {
  if (window.me == null) {
    return;
  }

  if (this.dirty) {
    network.send({
      command : "keys",
      keys : this.keys
    });
    this.dirty = false;
  }

  var keys = this.keys;

  var speed = 3;
  var distance = speed * TILE_SIZE * t / 1000;

  var dx = 0, dy = 0;

  if (keys['w']) {
    dy -= distance;
  }
  if (keys['a']) {
    dx -= distance;
  }
  if (keys['s']) {
    dy += distance;
  }
  if (keys['d']) {
    dx += distance;
  }

  if (dx != 0) {
    this.move(me, dx, 0);
  }
  if (dy != 0) {
    this.move(me, 0, dy);
  }
}

Input.prototype.move = function(player, dx, dy) {
  var rect = this.rect;
  rect.x = player.x + dx + player.hitbox.x;
  rect.y = player.y + dy + player.hitbox.y;
  rect.width = player.hitbox.width;
  rect.height = player.hitbox.height;

  if (this.intersectsBadTerrain(rect)) {
    return;
  }

  player.setX(player.x + dx);
  player.setY(player.y + dy);
}

Input.prototype.intersectsBadTerrain = function(rect) {
  var minI = Math.floor(rect.x / TILE_SIZE);
  var minJ = Math.floor(rect.y / TILE_SIZE);
  var maxI = Math.floor((rect.x + rect.width) / TILE_SIZE);
  var maxJ = Math.floor((rect.y + rect.height) / TILE_SIZE);

  for (var i = minI; i <= maxI; i++) {
    for (var j = minJ; j <= maxJ; j++) {
      if (!world.terrain.isWalkable(i, j)) {
        return true;
      }
    }
  }
  return false;
}

Input.prototype.listen = function() {
  var self = this;
  var keys = self.keys;
  $(window).keydown(function(e) {
    keys[e.key] = true;
    self.dirty = true;
  });
  $(window).keyup(function(e) {
    delete keys[e.key];
    self.dirty = true;
  });
}