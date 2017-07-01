function Camera() {
}

Camera.prototype.interpolate = function(from, to, percent) {
  percent = Math.min(percent, 1)
  return from + percent * (to - from)
}

Camera.prototype.update = function(millis) {
  var w = $(window).width();
  var h = $(window).height();

  if (window.me) {
    world.container.x = this.interpolate(world.container.x, w / 2 - (me.x + me.width / 2), millis / 200);
    world.container.y = this.interpolate(world.container.y, h / 2 - (me.y + me.height / 2), millis / 200);
  }
}

Camera.prototype.onPlayerSpawned = function() {
  this.update(200);
}