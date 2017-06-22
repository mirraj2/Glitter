/**
 * Listens for user input.
 */

function Input() {
  this.keys = {};
}

Input.prototype.update = function(t) {
  var keys = this.keys;

  var speed = 3;
  var distance = speed * TILE_SIZE * t / 1000;

  if (keys['w']) {
    me.setY(me.y - distance);
  }
  if (keys['a']) {
    me.setX(me.x - distance);
  }
  if (keys['s']) {
    me.setY(me.y + distance);
  }
  if (keys['d']) {
    me.setX(me.x + distance);
  }
}

Input.prototype.listen = function() {
  var keys = this.keys;
  $(window).keydown(function(e) {
    keys[e.key] = true;
  });
  $(window).keyup(function(e) {
    keys[e.key] = false;
  });
}