function Camera(world, canvas) {
}

Camera.prototype.update = function(t) {
  if (world.terrain) {
    world.container.x = Math.round(($(window).width() - world.terrain.getPixelWidth()) / 2);
    world.container.y = Math.round(($(window).height() - world.terrain.getPixelHeight()) / 2);
  }
}