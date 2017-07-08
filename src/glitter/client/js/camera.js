function Camera() {
  glitter.register(this);
}

Camera.prototype.update = function(millis) {
  var w = $(window).width();
  var h = $(window).height();

  if (window.me) {
    world.container.x = interpolate(world.container.x, w / 2 - (me.x + me.width / 2), millis / 200);
    world.container.y = interpolate(world.container.y, h / 2 - (me.y + me.height / 2), millis / 200);
  }
}

Camera.prototype.onPlayerSpawned = function() {
  this.update(200);
}