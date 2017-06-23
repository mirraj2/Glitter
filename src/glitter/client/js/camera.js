function Camera() {
}

Camera.prototype.update = function(t) {
  var w = $(window).width();
  var h = $(window).height();
  // if (world.terrain) {
  // world.container.x = Math.round((w - world.terrain.getPixelWidth()) / 2);
  // world.container.y = Math.round((h - world.terrain.getPixelHeight()) / 2);
  // }

  if (window.me) {
    world.container.x = Math.round(w / 2 - (Math.round(me.x) + me.width / 2));
    world.container.y = Math.round(h / 2 - (Math.round(me.y) + me.height / 2));
  }
}